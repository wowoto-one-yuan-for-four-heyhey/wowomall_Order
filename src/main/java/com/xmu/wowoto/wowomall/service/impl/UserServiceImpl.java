package com.xmu.wowoto.wowomall.service.impl;

import com.xmu.wowoto.wowomall.domain.User;
import com.xmu.wowoto.wowomall.service.RemoteUserService;
import com.xmu.wowoto.wowomall.service.UserService;
import com.xmu.wowoto.wowomall.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 20:28
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    RemoteUserService remoteUserService;

    @Override
    public User getUserById(Integer userId) {
        String json = remoteUserService.getUserById(userId);
        return JacksonUtil.parseObject(json, "data", User.class);
    }
}
