package com.example.reminderservicemaven.controllers;

import com.example.reminderservicemaven.dto.ReminderDTO;
import com.example.reminderservicemaven.models.Reminder;
import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.services.ReminderService;
import com.example.reminderservicemaven.services.UserService;
import com.example.reminderservicemaven.util.FilterRequest;
import com.example.reminderservicemaven.util.exceptions.ReminderNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ReminderControllerTest {

    @Mock
    private ReminderService reminderService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReminderController reminderController;

    private OAuth2User createPrincipal() {
        Map<String, Object> attributes = Map.of(
                "id", 1,
                "name", "Test User",
                "email", "test@mail.com",
                "chatId", "123456"
        );
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "name");
    }

    private ResponseEntity<List<ReminderDTO>> getAllReminders(OAuth2User principal) {
        List<ReminderDTO> reminders = List.of(
                new ReminderDTO("test title 1", "test description 1", LocalDateTime.now(), 1),
                new ReminderDTO("test title 2", "test description 2", LocalDateTime.now().plusDays(1), 1)
        );
        when(reminderService.findAll(principal)).thenReturn(reminders);
        return reminderController.getAllRemindersWithPagination(principal, null, null);
    }

    @Test
    void getAllRemindersWithPagination_withoutParams_ReturnsValidResponseEntity() {
        OAuth2User principal = createPrincipal();
        ResponseEntity<List<ReminderDTO>> response = getAllReminders(principal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(reminderService, times(1)).findAll(principal);
    }

    @Test
    void getAllRemindersWithPagination_withParams_ReturnsValidResponseEntity() {

        OAuth2User principal = createPrincipal();

        List<ReminderDTO> expectedRemindersPage1 = List.of(
                new ReminderDTO("test title 1", "test description 1", LocalDateTime.now(), 1),
                new ReminderDTO("test title 2", "test description 2", LocalDateTime.now().plusDays(1), 1)
        );

        when(reminderService.findAllWithPagination(principal, 2, 1)).thenReturn(expectedRemindersPage1);

        ResponseEntity<List<ReminderDTO>> response1 = reminderController.getAllRemindersWithPagination(principal, 2, 1);

        verify(reminderService, times(1)).findAllWithPagination(principal, 2, 1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(expectedRemindersPage1, response1.getBody());

        List<ReminderDTO> expectedRemindersPage2 = List.of(
                new ReminderDTO("test title 3", "test description 3", LocalDateTime.now().plusDays(2), 1),
                new ReminderDTO("test title 4", "test description 4", LocalDateTime.now().plusDays(3), 1)
        );

        when(reminderService.findAllWithPagination(principal, 2, 2)).thenReturn(expectedRemindersPage2);

        ResponseEntity<List<ReminderDTO>> response2 = reminderController.getAllRemindersWithPagination(principal, 2, 2);

        verify(reminderService, times(1)).findAllWithPagination(principal, 2, 2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(expectedRemindersPage2, response2.getBody());
    }

    @Test
    void createReminder_ReturnsCreatedStatus() throws SchedulerException {

        OAuth2User principal = createPrincipal();

        ReminderDTO reminderDTO = new ReminderDTO("test title", "test description", LocalDateTime.now(), 1);

        Reminder reminder = new Reminder();

        reminder.setTitle(reminderDTO.getTitle());
        reminder.setDescription(reminderDTO.getDescription());
        reminder.setRemind(reminderDTO.getRemind());
        reminder.setId(reminderDTO.getUserId());

        when(userService.findUserByUsernameOrThrowException("Test User")).thenReturn(new User());

        when(reminderService.convertToReminder(reminderDTO)).thenReturn(reminder);

        doNothing().when(reminderService).saveReminder(reminder);

        ResponseEntity<HttpStatus> response = reminderController.createReminder(reminderDTO, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reminderService, times(1)).saveReminder(reminder);
    }

    @Test
    void createReminder_WhenSchedulerException_throwsException() throws SchedulerException{

        OAuth2User principal = createPrincipal();

        ReminderDTO reminderDTO = new ReminderDTO("test title", "test description", LocalDateTime.now(), 1);

        when(reminderService.convertToReminder(reminderDTO)).thenReturn(new Reminder());

        doThrow(new SchedulerException()).when(reminderService).saveReminder(any(Reminder.class));

        SchedulerException thrownException = assertThrows(SchedulerException.class, () -> {
            reminderController.createReminder(reminderDTO, principal);
        });

        assertNotNull(thrownException);
        verify(reminderService, times(1)).convertToReminder(reminderDTO);
        verify(reminderService, times(1)).saveReminder(any(Reminder.class));
    }

    @Test
    void deleteReminder_ReturnsNoContentStatus() {

        OAuth2User principal = createPrincipal();

        doNothing().when(reminderService).deleteReminder(1, principal);

        ResponseEntity<HttpStatus> response = reminderController.deleteReminder(1, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reminderService, times(1)).deleteReminder(1, principal);
    }

    @Test
    void deleteReminder_WhenReminderNotFoundException_ThrowsException() {

        OAuth2User principal = createPrincipal();

        doThrow(new ReminderNotFoundException()).when(reminderService).deleteReminder(1, principal);

        ReminderNotFoundException thrownException = assertThrows(ReminderNotFoundException.class,() -> {
            reminderController.deleteReminder(1, principal);
        });

        assertNotNull(thrownException);
        verify(reminderService, times(1)).deleteReminder(1, principal);
    }

    @Test
    void sortReminders_WhenSortByName_ReturnsSortedByName() {

        OAuth2User principal = createPrincipal();

        List<ReminderDTO> sortedByNameReminder = List.of(new ReminderDTO("test title", "test description", LocalDateTime.now(), 1));

        when(reminderService.findAllSortedByName(principal)).thenReturn(sortedByNameReminder);

        ResponseEntity<List<ReminderDTO>> response = reminderController.sortReminders(principal, true, false, false);

        verify(reminderService, times(1)).findAllSortedByName(principal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sortedByNameReminder, response.getBody());
    }

    @Test
    void sortReminders_WhenSortByDate_ReturnsSortedByDate() {

        OAuth2User principal = createPrincipal();

        List<ReminderDTO> sortedByDateReminder = List.of(new ReminderDTO("test title", "test description", LocalDateTime.now(), 1));

        when(reminderService.findAllSortedByDate(principal)).thenReturn(sortedByDateReminder);

        ResponseEntity<List<ReminderDTO>> response = reminderController.sortReminders(principal, false, true, false);

        verify(reminderService, times(1)).findAllSortedByDate(principal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sortedByDateReminder, response.getBody());
    }

    @Test
    void sortReminders_WhenSortByTime_ReturnsSortedByTime() {

        OAuth2User principal = createPrincipal();

        List<ReminderDTO> sortedByTimeReminder = List.of(new ReminderDTO("test title", "test description", LocalDateTime.now(), 1));

        when(reminderService.findAllSortedByTime(principal)).thenReturn(sortedByTimeReminder);

        ResponseEntity<List<ReminderDTO>> response = reminderController.sortReminders(principal, false, false, true);

        verify(reminderService, times(1)).findAllSortedByTime(principal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sortedByTimeReminder, response.getBody());

    }

    @Test
    void sortReminders_WithoutParams_ReturnsAllReminders() {

        OAuth2User principal = createPrincipal();

        List<ReminderDTO> allReminders = List.of(new ReminderDTO("test title", "test description", LocalDateTime.now(), 1));

        when(reminderService.findAll(principal)).thenReturn(allReminders);

        ResponseEntity<List<ReminderDTO>> response = reminderController.sortReminders(principal, false, false, false);

        verify(reminderService, times(1)).findAll(principal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(allReminders, response.getBody());

    }

    @Test
    void filterReminder_WhenDate_ReturnsFilteredByDate() {

        OAuth2User principal = createPrincipal();

        FilterRequest testFilterRequest = new FilterRequest(LocalDate.now(), null);

        List<ReminderDTO> filteredByDateReminders = List.of(new ReminderDTO("test title", "test description", LocalDateTime.now(), 1));

        when(reminderService.findFiltered(principal, testFilterRequest)).thenReturn(filteredByDateReminders);

        ResponseEntity<List<ReminderDTO>> response = reminderController.filterReminder(principal, LocalDate.now(), null);

        verify(reminderService, times(1)).findFiltered(principal, testFilterRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filteredByDateReminders, response.getBody());
    }

    @Test
    void filterReminder_WhenTime_ReturnsFilteredByTime() {

        OAuth2User principal = createPrincipal();

        LocalDateTime testDateTime = LocalDateTime.now();
        LocalTime testTime = testDateTime.toLocalTime();

        FilterRequest testFilterRequest = new FilterRequest(null, testTime);

        List<ReminderDTO> filteredByTimeReminders = List.of(new ReminderDTO("test title", "test description", testDateTime, 1));

        when(reminderService.findFiltered(principal, testFilterRequest)).thenReturn(filteredByTimeReminders);

        ResponseEntity<List<ReminderDTO>> response = reminderController.filterReminder(principal, null, testTime);

        verify(reminderService, times(1)).findFiltered(principal, testFilterRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filteredByTimeReminders, response.getBody());
    }

    @Test
    void filterReminder_WithoutParams_ReturnsAllReminders() {

        OAuth2User principal = createPrincipal();

        FilterRequest testFilterRequest = new FilterRequest(null, null);

        List<ReminderDTO> allReminders = List.of(new ReminderDTO("test title", "test description", LocalDateTime.now(), 1));

        when(reminderService.findFiltered(principal, testFilterRequest)).thenReturn(allReminders);

        ResponseEntity<List<ReminderDTO>> response = reminderController.filterReminder(principal, null, null);

        verify(reminderService, times(1)).findFiltered(principal, testFilterRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(allReminders, response.getBody());
    }


}
