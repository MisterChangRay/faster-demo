package com.github.misterchangray.oss.files.controler;

import com.github.misterchangray.common.base.BaseEnum;
import com.github.misterchangray.common.base.BaseResponse;
import com.github.misterchangray.oss.files.v1.intf.OSSFileService;
import com.github.misterchangray.oss.files.v1.pojo.request.FileUploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
     @Autowired
    private OSSFileService ossFileService;

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
    public BaseResponse token(@RequestBody FileUploadInfo fileUploadInfo) {
        if(!StringUtils.hasLength(fileUploadInfo.getFilename())) {
            return BaseResponse.ofFail(BaseEnum.INVALID_PARAM);
        }
        return ossFileService.getToken();
    }



}
