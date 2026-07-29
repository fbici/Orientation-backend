package com.orientation.orientationapp.dataplat_import.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ImportJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting import job: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
        log.info("Import job completed with status: {} in {}ms",
                jobExecution.getStatus(), duration);
    }
}
