package com.example.reminderservicemaven.config;

import com.example.reminderservicemaven.quartzJobs.EmailJob;
import com.example.reminderservicemaven.quartzJobs.TelegramJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public JobDetail emailJobDetail() {
        return JobBuilder.newJob(EmailJob.class)
                .withIdentity("emailJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger emailJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(60)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(emailJobDetail())
                .withIdentity("emailJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail telegramJobDetail() {
        return JobBuilder.newJob(TelegramJob.class)
                .withIdentity("telegramJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger telegramJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(60)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(telegramJobDetail())
                .withIdentity("telegramJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
