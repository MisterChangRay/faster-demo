package com.github.misterchangray.user.controler;

import com.github.misterchangray.common.base.BaseResponse;
import com.github.misterchangray.messages.v1.intf.MessagesService;
import com.github.misterchangray.messages.v1.pojo.request.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description:
 * @author: Ray.chang
 * @create: 2022-01-05 20:59
 **/
@Component
@RequestMapping("/messages")
public class MessagesController {
    @Autowired
    private MessagesService messagesService;




    /**
     *
     * 前端 clientID 默认为一个UUID
     * 实际后端储存时使用接收方信息
     *
     * @param messageInfo
     * @return
     */
    public BaseResponse<String> verificationCode(@RequestBody Message messageInfo) {
        return BaseResponse.ofSuccess(null);
    }



}
