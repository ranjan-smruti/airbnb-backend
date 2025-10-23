package com.codingshuttle.projects.airBnbApp.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class ThreadConfig {
    private final Integer delaySeconds = 5;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor()
    {
        ThreadPoolTaskExecutor poolExecutor = new ThreadPoolTaskExecutor();
        poolExecutor.setCorePoolSize(3);
        poolExecutor.setMaxPoolSize(5);
        poolExecutor.setQueueCapacity(4);
        poolExecutor.setThreadNamePrefix("Scheduler-TaskExec-");
        poolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.warn("{} rejected. Retrying after {} seconds...", Thread.currentThread().getName(), delaySeconds);
                try {
                    TimeUnit.SECONDS.sleep(delaySeconds);
                    executor.submit(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); //it breaks proper thread cancellation and graceful shutdown.
                    log.warn("Retry interrupter for rejected task");
                } catch (Exception ex) {
                    log.warn("Failed to resubmit rejected task, error: {}", ex.getMessage());
                }
            }
        });

        return poolExecutor;
    }
}
