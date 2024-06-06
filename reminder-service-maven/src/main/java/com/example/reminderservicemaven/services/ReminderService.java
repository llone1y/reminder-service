package com.example.reminderservicemaven.services;

import com.example.reminderservicemaven.dto.ReminderDTO;
import com.example.reminderservicemaven.models.Reminder;
import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.repositories.ReminderRepository;
import com.example.reminderservicemaven.util.FilterRequest;
import com.example.reminderservicemaven.util.QuartzJobsBuilder;
import com.example.reminderservicemaven.util.exceptions.ReminderNotFoundException;
import com.example.reminderservicemaven.util.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final Scheduler scheduler;
    private final QuartzJobsBuilder quartzJobsBuilder;

    public void saveReminder(Reminder reminder) throws SchedulerException {
        scheduleReminderTasks(reminder);
        reminderRepository.save(reminder);
    }

    private void scheduleReminderTasks(Reminder reminder) throws SchedulerException {
        JobDetail emailJobDetail = quartzJobsBuilder.buildEmailJobDetail(reminder);
        Trigger emailTrigger = quartzJobsBuilder.buildEmailTrigger(emailJobDetail, reminder);

        JobDetail telegramJobDetail = quartzJobsBuilder.buildTelegramJobDetail(reminder.getUser().getChatId(), reminder.getDescription());
        Trigger telegramTrigger = quartzJobsBuilder.buildTelegramTrigger(telegramJobDetail, reminder);

        scheduler.scheduleJob(emailJobDetail, emailTrigger);
        scheduler.scheduleJob(telegramJobDetail, telegramTrigger);
    }

    @Transactional
    public void deleteReminder(int id, OAuth2User authentication) {
        Reminder reminder = reminderRepository.findById(id).orElseThrow(() -> new ReminderNotFoundException("Напоминание с идентификатором " + id + " не найдено"));

        User user = userService.findUserByUsernameOrThrowException(authentication.getAttribute("name"));

        if(reminder.getUser().getId() == user.getId()) {
            reminderRepository.delete(reminder);
        }
    }

    private List<ReminderDTO> findAllSorted(OAuth2User authentication, Comparator<Reminder> comparator) {
        User user = userService.findUserByUsernameOrThrowException(authentication.getAttribute("name"));

        return reminderRepository.findAllByUserId(user.getId()).stream()
                .sorted(comparator)
                .map(this::convertToReminderDTO)
                .collect(Collectors.toList());
    }

    public List<ReminderDTO> findAll(OAuth2User authentication) {
        User user = userService.findUserByUsernameOrThrowException(authentication.getAttribute("name"));
        List<Reminder> reminders = reminderRepository.findAllByUserId(user.getId());

        return reminders.stream().map(this::convertToReminderDTO).collect(Collectors.toList());
    }
    public List<ReminderDTO> findAllSortedByName(OAuth2User authentication) {
        return findAllSorted(authentication, Comparator.comparing(Reminder::getTitle));
    }

    public List<ReminderDTO> findAllSortedByDate(OAuth2User authentication) {
        return findAllSorted(authentication, Comparator.comparing(r -> r.getRemind().toLocalDate()));
    }

    public List<ReminderDTO> findAllSortedByTime(OAuth2User authentication) {
        return findAllSorted(authentication, Comparator.comparing(r -> r.getRemind().toLocalTime()));
    }

    public List<ReminderDTO> findFiltered(OAuth2User authentication, FilterRequest filterRequest) {
        User user = userService.findUserByUsernameOrThrowException(authentication.getAttribute("name"));
        List<Reminder> reminders = reminderRepository.findAllByUserId(user.getId());

        Stream<Reminder> filteredStream = reminders.stream();

        if(filterRequest.getDate() != null && filterRequest.getTime() != null) {
            filteredStream = filteredStream.filter(reminder ->
                    reminder.getRemind().toLocalDate().equals(filterRequest.getDate()) &&
                    reminder.getRemind().toLocalTime().equals(filterRequest.getTime()));
        } else if(filterRequest.getDate() != null) {
            filteredStream = filteredStream.filter(reminder ->
                    reminder.getRemind().toLocalDate().equals(filterRequest.getDate()));
        } else if (filterRequest.getTime() != null){
            filteredStream = filteredStream.filter(reminder ->
                    reminder.getRemind().toLocalTime().equals(filterRequest.getTime()));
        }

        return filteredStream.map(this::convertToReminderDTO).collect(Collectors.toList());
    }

    public List<ReminderDTO> findAllWithPagination(OAuth2User authentication, Integer total, Integer current) {
        User user = userService.findUserByUsernameOrThrowException(authentication.getAttribute("name"));
        List<Reminder> reminders = reminderRepository.findAllByUserId(user.getId());

        return reminders.stream()
                .skip((long) (current - 1) * total)
                .limit(total)
                .map(this::convertToReminderDTO)
                .collect(Collectors.toList());
    }

    public Reminder convertToReminder(ReminderDTO reminderDTO) {
        return modelMapper.map(reminderDTO, Reminder.class);
    }

    public ReminderDTO convertToReminderDTO(Reminder reminder) {
        return modelMapper.map(reminder, ReminderDTO.class);
    }
}