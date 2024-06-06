package com.example.reminderservicemaven.services;

import com.example.reminderservicemaven.models.Reminder;
import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.repositories.ReminderRepository;
import com.example.reminderservicemaven.util.QuartzJobsBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private Scheduler scheduler;
    @Mock
    private QuartzJobsBuilder quartzJobsBuilder;
    @InjectMocks
    private ReminderService reminderService;

    @Test
    void saveReminder_CallsRepositorySaveAndSchedulesTasks() throws SchedulerException {
        User user = new User(1, "Test Name", "test@mail.com", "123456", null);

        Reminder reminder = new Reminder(1, "test title", "test description", LocalDateTime.now(), user);

        JobDetail emailJobDetail = mock(JobDetail.class);
        JobDetail telegramJobDetail = mock(JobDetail.class);

        Trigger emailTrigger = mock(Trigger.class);
        Trigger telegramTrigger = mock(Trigger.class);

        when(quartzJobsBuilder.buildEmailJobDetail(reminder)).thenReturn(emailJobDetail);
        when(quartzJobsBuilder.buildTelegramJobDetail(anyString(), anyString())).thenReturn(telegramJobDetail);

        when(quartzJobsBuilder.buildEmailTrigger(emailJobDetail, reminder)).thenReturn(emailTrigger);
        when(quartzJobsBuilder.buildTelegramTrigger(telegramJobDetail, reminder)).thenReturn(telegramTrigger);

        reminderService.saveReminder(reminder);

        verify(reminderRepository, times(1)).save(reminder);
        verify(scheduler, times(1)).scheduleJob(emailJobDetail, emailTrigger);
        verify(scheduler, times(1)).scheduleJob(telegramJobDetail, telegramTrigger);
    }

    @Test
    void saveReminder_WhenSchedulingJob_ThrowsSchedulerException() throws SchedulerException {
        User user = new User(1, "Test Name", "test@mail.com", "123456", null);
        Reminder reminder = new Reminder(1, "test title", "test description", LocalDateTime.now(), user);

        JobDetail emailJobDetail = mock(JobDetail.class);
        JobDetail telegramJobDetail = mock(JobDetail.class);

        Trigger emailTrigger = mock(Trigger.class);
        Trigger telegramTrigger = mock(Trigger.class);

        when(quartzJobsBuilder.buildEmailJobDetail(reminder)).thenReturn(emailJobDetail);
        when(quartzJobsBuilder.buildTelegramJobDetail(anyString(), anyString())).thenReturn(telegramJobDetail);

        when(quartzJobsBuilder.buildEmailTrigger(emailJobDetail, reminder)).thenReturn(emailTrigger);
        when(quartzJobsBuilder.buildTelegramTrigger(telegramJobDetail, reminder)).thenReturn(telegramTrigger);

        doThrow(new SchedulerException("Test SchedulerException")).when(scheduler).scheduleJob(emailJobDetail, emailTrigger);

        SchedulerException thrownException =  assertThrows(SchedulerException.class, () -> reminderService.saveReminder(reminder));

        assertNotNull(thrownException);
        verify(reminderRepository, never()).save(reminder);
        verify(scheduler, times(1)).scheduleJob(emailJobDetail, emailTrigger);
        verify(scheduler, never()).scheduleJob(telegramJobDetail, telegramTrigger);
    }
}

