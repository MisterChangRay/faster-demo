package com.github.misterchangray.user.service;

import com.github.misterchangray.common.base.BaseResponse;
import com.github.misterchangray.messages.v1.intf.MessagesService;
import com.github.misterchangray.messages.v1.pojo.request.CustomMessage;
import com.github.misterchangray.messages.v1.pojo.request.Message;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: Ray.chang
 * @create: 2022-01-05 21:22
 **/
@Service
public class MessagesServiceImpl implements MessagesService {
    @Autowired
    RedissonClient redissonClient;


    @Override
    public BaseResponse<Boolean> checkAndDestroy(String code) {
        return null;
    }

    @Override
    public BaseResponse<Boolean> send(Message messageInfo) {
        return null;
    }

    @Override
    public BaseResponse<Boolean> send(CustomMessage customMessage) {
        return null;
    }


    public BaseResponse<Boolean> doSendEmail(CustomMessage customMessage) {
        return null;
    }

    public BaseResponse<Boolean> doSendSMS(CustomMessage customMessage) {
        return null;
    }


}
