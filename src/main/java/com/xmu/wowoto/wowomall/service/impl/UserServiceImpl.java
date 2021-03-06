package com.xmu.wowoto.wowomall.service.impl;

import com.xmu.wowoto.wowomall.domain.User;
import com.xmu.wowoto.wowomall.service.RemoteUserService;
import com.xmu.wowoto.wowomall.service.UserService;
import com.xmu.wowoto.wowomall.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 20:28
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    RemoteUserService remoteUserService;

    /**
     * 根据id拿到user
     * @param userId
     * @return
     */
    @Override
            public User getUserById(Integer userId) {
                return JacksonUtil.parseObject(remoteUserService.getUserById(userId), User.class);
            }

    /**
     * 添加用户返点
     * @param userId
     * @param rebate
     * @return
     */
    @Override
    public Integer updateUserRebate(Integer userId,Integer rebate){
        String json = remoteUserService.updateUserRebate(userId, rebate);
        Integer errNo = JacksonUtil.parseInteger(json,"errno");
        return errNo;
    }


}
