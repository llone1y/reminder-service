package com.example.reminderservicemaven.quartzJobs;

import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.services.EmailService;
import com.example.reminderservicemaven.services.UserService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@RequiredArgsConstructor
public class EmailJob extends QuartzJobBean {


    private final EmailService emailService;
    private final UserService userService;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String username = context.getMergedJobDataMap().getString("username");
        String content = context.getMergedJobDataMap().getString("content");
        User user = userService.findUserByUsernameOrThrowException(username);
        String email = user.getEmail();

        emailService.sendEmail(email, "reminder", content);
    }
}
