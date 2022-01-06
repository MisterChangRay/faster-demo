package com.github.misterchangray.oss.files.service;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.NoRetryStrategy;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.github.misterchangray.common.base.BaseEnum;
import com.github.misterchangray.common.base.BaseResponse;
import com.github.misterchangray.common.base.ResEnum;
import com.github.misterchangray.oss.files.autoconfig.AliyunOssConfig;
import com.github.misterchangray.oss.files.v1.intf.OSSFileService;
import com.github.misterchangray.oss.files.v1.pojo.request.OSSFileMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @description:
 * @author: Ray.chang
 * @create: 2022-01-06 15:50
 **/
@Component
public class OSSFileServiceImpl implements OSSFileService {
    private static Logger logger = LoggerFactory.getLogger(OSSFileService.class);

    private static final String SLASH_CAHR = "/";
    private static final String DOT_CHAR = ".";
    private static final String PROTOCOL_SPLIT_CHAR = "://";
    private static final String URL_REGEX = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    private static final String policy = "{\n" +
            "    \"Version\": \"1\", \n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Action\": [\n" +
            "                \"oss:PutObject\"\n" +
            "            ], \n" +
            "            \"Resource\": [\n" +
            "                \"acs:oss:*:*:%s*\" \n" +
            "            ], \n" +
            "            \"Effect\": \"Allow\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"Condition\":{\n" +
            "                \"IpAddress\": {\n" +
            "                    \"acs:SourceIp\": [\"0.0.0.0\"]\n" +
            "                }\n" +
            "            }\n" +
            "}";

    @Autowired
    AliyunOssConfig aliyunOssConfig;

    @Override
    public BaseResponse getToken() {
        if(aliyunOssConfig.getDefaultDir().startsWith("/") || aliyunOssConfig.getDefaultDir().endsWith("/")) {
            throw new RuntimeException("oss basePath can't start or end with /");
        }

        String tmpPath = aliyunOssConfig.getDefaultDir() + "/" + UUID.randomUUID().toString();
        String tmpPolicy = String.format(policy, tmpPath);

        String roleSessionName = "fileUploadTmpToken";
        try {
            // 添加endpoint。适用于Java SDK 3.12.0及以上版本。
            DefaultProfile.addEndpoint(aliyunOssConfig.getRegionId(), "Sts", "sts.aliyuncs.com");
            // 构造default profile。
            IClientProfile profile = DefaultProfile.getProfile(aliyunOssConfig.getRegionId(), aliyunOssConfig.getAccessKeyId(), aliyunOssConfig.getSecretAccessKey());
            // 构造client。
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            // 适用于Java SDK 3.12.0及以上版本。
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(aliyunOssConfig.getRoleArn());
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(tmpPolicy);
            request.setDurationSeconds(3600L); // 设置临时访问凭证的有效时间为3600秒。
            final AssumeRoleResponse response = client.getAcsResponse(request);

            return BaseResponse.ofSuccess(response.getCredentials());
        } catch (Exception ae) {
            logger.error("FileUploadError", ae);
        }
        return BaseResponse.<AssumeRoleResponse.Credentials>ofFail(BaseEnum.SYSTEM_ERROR)
                .setMsg("system error, please try again!");
    }

    @Override
    public BaseResponse<String> upload(File file, String fullPathName) {
        ArrayList<File> arrayList = new ArrayList();
        arrayList.add(file);

        List<String> strings = this.transferToOss(arrayList, fullPathName);
        if(strings.size() == 0) {
            return BaseResponse.ofFail(BaseEnum.FAIL);
        }

        return BaseResponse.ofSuccess(strings.get(0));
    }

    private List<String> transferToOss(List<File> files, String dir) {
        List<String> urls = new ArrayList<String>();
        ClientBuilderConfiguration builderConfiguration = new ClientBuilderConfiguration();
        builderConfiguration.setConnectionRequestTimeout(this.aliyunOssConfig.getConnectionRequestTimeout());
        builderConfiguration.setConnectionTimeout(this.aliyunOssConfig.getConnectionTimeout());
        builderConfiguration.setRetryStrategy(new NoRetryStrategy());
        OSSClient client = (OSSClient) new OSSClientBuilder().build(aliyunOssConfig.getEndpoint(), aliyunOssConfig.getAccessKeyId(),
                aliyunOssConfig.getSecretAccessKey(), builderConfiguration);
        try {
            String bucketName = aliyunOssConfig.getBucketName();
            boolean bucketExist = checkAndCreateBucket(client, bucketName);
            String prefixDir = "";
            String defaultDir = this.aliyunOssConfig.getDefaultDir();
            if (null != defaultDir && !defaultDir.isEmpty()) {
                prefixDir = appendDirSeparator(defaultDir);
            }
            if (null != dir && !dir.isEmpty()) {
                prefixDir = appendDirSeparator(prefixDir + dir);
            }
            if (bucketExist) {
                for (File file : files) {
                    String key = prefixDir + file.getName();
                    client.putObject(bucketName, key, file);
                    urls.add(getAccessUrl(key));
                }
                return urls;
            }
            throw new IllegalArgumentException("[BucketName = " + bucketName + "]不存在");
        } finally {
            client.shutdown();
        }
    }


    private String getAccessUrl(String key) {
        String endpoint = this.aliyunOssConfig.getEndpoint();
        String bucketName = this.aliyunOssConfig.getBucketName();
        String baseUrl = null;
        String definedEndpoint = this.aliyunOssConfig.getAccessEndpoint();
        String protocol = this.aliyunOssConfig.getProtocol();
        if (null == definedEndpoint || definedEndpoint.isEmpty()) {
            if (endpoint.matches(URL_REGEX)) {
                int length = endpoint.indexOf(PROTOCOL_SPLIT_CHAR);
                int len = PROTOCOL_SPLIT_CHAR.length();
                baseUrl = endpoint.substring(0, length + len) + bucketName + DOT_CHAR
                        + endpoint.substring(length + len);
            } else {
                baseUrl = protocol + bucketName + DOT_CHAR + endpoint;
            }
        } else {
            baseUrl = definedEndpoint.matches(URL_REGEX) ? definedEndpoint : protocol + definedEndpoint;
        }
        return key.startsWith(SLASH_CAHR) ? baseUrl + key : baseUrl + SLASH_CAHR + key;
    }


    private String appendDirSeparator(String prefixDir) {
        return prefixDir.endsWith(SLASH_CAHR) ? prefixDir : prefixDir + SLASH_CAHR;
    }


    private boolean checkAndCreateBucket(OSSClient ossClient, String bucketName) {
        return ossClient.doesBucketExist(bucketName);
    }


    @Override
    public BaseResponse<String> fileMove(OSSFileMove ossFileMove) {
        OSSClient client = new OSSClient(aliyunOssConfig.getEndpoint(), aliyunOssConfig.getAccessKeyId(), aliyunOssConfig.getSecretAccessKey());
        CopyObjectResult res = client.copyObject(ossFileMove.getSourceBucketName(), ossFileMove.getSourcePath(),
                ossFileMove.getDistBucketName(), ossFileMove.getDistPath());

        if(res.getResponse().isSuccessful()) {
            return BaseResponse.<String>ofSuccess("success");
        } else {
            return BaseResponse.<String>ofFail(BaseEnum.FAIL).setMsg(res.getResponse().getErrorResponseAsString());
        }
    }


}
