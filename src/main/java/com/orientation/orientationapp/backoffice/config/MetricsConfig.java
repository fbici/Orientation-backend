package com.orientation.orientationapp.backoffice.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter recommendationCounter(MeterRegistry registry) {
        return Counter.builder("recommendations.total")
                .description("Total number of recommendations generated")
                .register(registry);
    }

    @Bean
    public Counter importCounter(MeterRegistry registry) {
        return Counter.builder("imports.total")
                .description("Total number of imports")
                .register(registry);
    }

    @Bean
    public Counter documentCounter(MeterRegistry registry) {
        return Counter.builder("documents.total")
                .description("Total number of documents processed")
                .register(registry);
    }

    @Bean
    public Timer recommendationTimer(MeterRegistry registry) {
        return Timer.builder("recommendations.execution.time")
                .description("Time to generate recommendations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Gauge heapGauge(MeterRegistry registry) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return Gauge.builder("jvm.heap.used", memoryBean, mb -> mb.getHeapMemoryUsage().getUsed())
                .description("JVM Heap Memory Used")
                .register(registry);
    }

    @Bean
    public Gauge threadGauge(MeterRegistry registry) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        AtomicInteger threadCount = new AtomicInteger();
        Gauge.builder("jvm.threads.count", threadBean, tb -> tb.getThreadCount())
                .description("JVM Thread Count")
                .register(registry);
        return null;
    }
}
