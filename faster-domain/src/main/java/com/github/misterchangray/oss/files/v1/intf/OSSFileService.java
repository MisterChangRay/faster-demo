package com.github.misterchangray.oss.files.v1.intf;

import com.github.misterchangray.common.base.BaseResponse;
import com.github.misterchangray.oss.files.v1.pojo.request.OSSFileMove;

import java.io.File;

/**
 * @description:
 * @author: Ray.chang
 * @create: 2022-01-06 15:47
 **/
public interface OSSFileService {

    /**
     * 获取文件上传TOKEN
     * @return
     */
    BaseResponse getToken();


    /**
     * 文件上传
     * @param file
     * @param fullPathName
     * @return
     */
    BaseResponse<String> upload(File file, String fullPathName);


    /**
     * oss 文件移动
     * 文件上传后在临时文件夹只会保存半个小时
     *
     * @param ossFileMove
     * @return
     */
    BaseResponse<String> fileMove(OSSFileMove ossFileMove);

}
