package com.orientation.orientationapp.backoffice.config;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
@Configuration
public class AdministrativeEmailConfiguration {
 @Bean(name="administrativeEmailExecutor")
 public Executor administrativeEmailExecutor() {
  ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
  executor.setCorePoolSize(1);executor.setMaxPoolSize(2);executor.setQueueCapacity(100);
  executor.setThreadNamePrefix("admin-email-");
  executor.setWaitForTasksToCompleteOnShutdown(true);executor.setAwaitTerminationSeconds(20);
  executor.initialize();return executor;
 }
}
