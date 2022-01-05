package com.github.misterchangray.files.controler;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.github.misterchangray.common.base.BaseEnum;
import com.github.misterchangray.common.base.BaseResponse;
import com.github.misterchangray.files.v001.pojo.request.FileUploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description: management file upload
 * @author: Ray.chang
 * @create: 2022-01-05 17:16
 **/
@Component
@RequestMapping("/oss")
public class FilesOSSController {
     private static Logger logger = LoggerFactory.getLogger(FilesOSSController.class);

    // private key
    @Value("${com.github.faster.files.oss.accessKeyId:''}")
    private String accessKeyId;
    // secret key
    @Value("${com.github.faster.files.oss.accessKeySecret:''}")
    String accessKeySecret;
    //	角色ARN。
    @Value("${com.github.faster.files.oss.roleArn:''}")
    String roleArn;
    // STS接入地址，例如sts.cn-hangzhou.aliyuncs.com。
    @Value("${com.github.faster.files.oss.endpoint:'sts.aliyuncs.com'}")
    private String endpoint;
    // regionId表示RAM的地域ID。以华东1（杭州）地域为例，regionID填写为cn-hangzhou。也可以保留默认值，默认值为空字符串（""）。
    @Value("${com.github.faster.files.oss.regionId:'cn-hangzhou'}")
    private String regionId;

    /**
     * get oss file upload token
     * 此种方式使用阿里云oss token 方式进行上传
     * 前端首先使用此接口获取上传TOKEN, 此 token 半小时内有效,  前端直接使用token上传文件到阿里云oss, 然后将链接发送给后台即可
     *
     * 前端上传代码参考 resource目录下的html压缩包
     *
     * 详情参见:使用STS临时访问凭证访问OSS
     * <ref>https://help.aliyun.com/document_detail/100624.html</ref>
     *
     *
     * @return
     */
    @PostMapping("/token")
    public BaseResponse<AssumeRoleResponse.Credentials> token(@RequestBody FileUploadInfo fileUploadInfo) {
        if(!StringUtils.hasLength(fileUploadInfo.getFilename())) {
            return BaseResponse.ofFail(BaseEnum.INVALID_PARAM);
        }

        // 自定义角色会话名称，用来区分不同的令牌，例如可填写为SessionTest。
        String roleSessionName = "fileUploadTmpToken";
        try {
            // 添加endpoint。适用于Java SDK 3.12.0及以上版本。
            DefaultProfile.addEndpoint(regionId, "Sts", endpoint);
            // 构造default profile。
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            // 构造client。
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            // 适用于Java SDK 3.12.0及以上版本。
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setDurationSeconds(3600L); // 设置临时访问凭证的有效时间为3600秒。
            final AssumeRoleResponse response = client.getAcsResponse(request);

            return BaseResponse.ofSuccess(response.getCredentials());
        } catch (Exception ae) {
            logger.error("FileUploadError", ae);
        }

        return BaseResponse.<AssumeRoleResponse.Credentials>ofFail(BaseEnum.SYSTEM_ERROR)
                .setMsg("system error, please try again!");
    }
}
