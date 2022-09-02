package com.atguigu.gmall.feign.retry;

import feign.RetryableException;
import feign.Retryer;

/**
 * select/update/delete 具备天然幂等性
 * insert不具备幂等性
 * 所以需要永不重试的重试器
 */
public class MyRetryer implements Retryer {
    //目前重试次数
    int current = 0;
    //最大重试次数
    int maxCount = 2;

    @Override
    public void continueOrPropagate(RetryableException e) {
        //代表不进行重试
//        throw e;
        if (current++ > 2) {
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return this;
    }

}
