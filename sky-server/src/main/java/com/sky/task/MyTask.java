package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义定时任务类
 * @author ajin
 * @create 2024-07-22 14:09
 */
@Component
@Slf4j
public class MyTask {

    /**
     * 每5秒执行一次
     */
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void executeTask(){
//        log.info("定时任务执行了:{}",new Date());
//    }
}
