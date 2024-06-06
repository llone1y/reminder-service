package com.example.reminderservicemaven.controllers;

import com.example.reminderservicemaven.dto.ReminderDTO;
import com.example.reminderservicemaven.models.Reminder;
import com.example.reminderservicemaven.services.ReminderService;
import com.example.reminderservicemaven.services.UserService;
import com.example.reminderservicemaven.util.FilterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reminder")
public class ReminderController {

    private final ReminderService reminderService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<HttpStatus> createReminder(@RequestBody ReminderDTO reminderDTO,
                                                     @AuthenticationPrincipal OAuth2User authentication) throws SchedulerException {
        Reminder reminder = reminderService.convertToReminder(reminderDTO);
        String username = authentication.getAttribute("name");
        reminder.setUser(userService.findUserByUsernameOrThrowException(username));
        reminderService.saveReminder(reminder);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReminder(@PathVariable int id,
                                                     @AuthenticationPrincipal OAuth2User authentication) {
        reminderService.deleteReminder(id, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/sort")
    public ResponseEntity<List<ReminderDTO>> sortReminders(@AuthenticationPrincipal OAuth2User authentication,
                                                           @RequestParam(value = "name", required = false) boolean sortByName,
                                                           @RequestParam(value = "date", required = false) boolean sortByDate,
                                                           @RequestParam(value = "time", required = false) boolean sortByTime) {
        if(sortByName) {
            return ResponseEntity.ok(reminderService.findAllSortedByName(authentication));
        } else if(sortByDate) {
            return ResponseEntity.ok(reminderService.findAllSortedByDate(authentication));
        } else if(sortByTime) {
            return ResponseEntity.ok(reminderService.findAllSortedByTime(authentication));
        } else {
            return ResponseEntity.ok(reminderService.findAll(authentication));
        }
    }

    @GetMapping("/filtr")
    public ResponseEntity<List<ReminderDTO>> filterReminder(@AuthenticationPrincipal OAuth2User authentication,
                                                            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy")LocalDate date,
                                                            @RequestParam(value = "time", required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time) {
        FilterRequest filterRequest = new FilterRequest(date, time);
        return ResponseEntity.ok(reminderService.findFiltered(authentication, filterRequest));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ReminderDTO>> getAllRemindersWithPagination(@AuthenticationPrincipal OAuth2User authentication,
                                                                             @RequestParam(value = "total", required = false) @Valid @Min(value = 1, message = "Parameter 'total' must be greater than zero") Integer total,
                                                                             @RequestParam(value = "current", required = false) @Valid @Min(value = 1, message = "Parameter 'current' must be greater than zero") Integer current) {

        if(total != null && current != null) {
            return ResponseEntity.ok(reminderService.findAllWithPagination(authentication, total, current));
        } else {
            return ResponseEntity.ok(reminderService.findAll(authentication));
        }
    }
}