package com.atguigu.gmall.item.controller.pool;

import com.atguigu.gmall.common.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
public class ThreadPoolController {
    @Autowired
    private ThreadPoolExecutor executor;
    @ApiOperation("关闭线程池")
    @GetMapping("/close/pool")
    public Result closeThreadPool() {
        executor.shutdown();
        return Result.ok();
    }

    @GetMapping("/monitor/pool")
    public Result monitorThreadPool() {
        HashMap<String, String> map = new HashMap<>();
        int corePoolSize = executor.getCorePoolSize();
        int maximumPoolSize = executor.getMaximumPoolSize();
        map.put("corePoolSize", "" + corePoolSize);
        map.put("maximumPoolSize", "" + maximumPoolSize);
        return Result.ok(map);
    }

    /**
     * actuator监控springboot应用
     */


}
