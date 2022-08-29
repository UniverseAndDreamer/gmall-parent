package com.atguigu.gmall.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
public class ThreadPoolTest {

    @Autowired
    ThreadPoolExecutor executor;

    @Test
    public void test1(){
        Future<Integer> submit = executor.submit(() -> {

            System.out.println(Thread.currentThread().getName() + "=" + "heihei");
            return 3;
        });
        try {
            System.out.println(submit.get());
            System.out.println(Thread.currentThread().getName() + "=" +submit.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

}
