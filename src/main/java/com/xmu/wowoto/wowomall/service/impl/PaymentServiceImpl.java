package com.xmu.wowoto.wowomall.service.impl;

import com.xmu.wowoto.wowomall.domain.Payment;
import com.xmu.wowoto.wowomall.service.PaymentService;
import com.xmu.wowoto.wowomall.service.RemotePaymentService;
import com.xmu.wowoto.wowomall.util.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Tens
 * @Description:
 * @Date: 2019/12/15 20:05
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    RemotePaymentService remotePaymentService;

    @Override
    public Payment createPayment(Payment payment) {
        String json = remotePaymentService.createPayment(payment);
        return JacksonUtil.parseObject(json, "data", Payment.class);
    }
}