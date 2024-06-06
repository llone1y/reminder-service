package com.example.reminderservicemaven.util;

import com.example.reminderservicemaven.models.Reminder;
import com.example.reminderservicemaven.quartzJobs.EmailJob;
import com.example.reminderservicemaven.quartzJobs.TelegramJob;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class QuartzJobsBuilder {

    public JobDetail buildEmailJobDetail(Reminder reminder) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("username", reminder.getUser().getUsername());
        jobDataMap.put("content", reminder.getDescription());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send email job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    public Trigger buildEmailTrigger(JobDetail jobDetail, Reminder reminder) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send email trigger")
                .startAt(Date.from(reminder.getRemind().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    public JobDetail buildTelegramJobDetail(String chatId, String messageText) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("chatId", chatId);
        jobDataMap.put("messageText", messageText);

        return JobBuilder.newJob(TelegramJob.class)
                .withIdentity(UUID.randomUUID().toString(), "telegram-jobs")
                .withDescription("Send Telegram message job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    public Trigger buildTelegramTrigger(JobDetail jobDetail, Reminder reminder) {
        return TriggerBuilder.newTrigger().forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "telegram-triggers")
                .withDescription("Send Telegram message trigger")
                .startAt(Date.from(reminder.getRemind().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

}
