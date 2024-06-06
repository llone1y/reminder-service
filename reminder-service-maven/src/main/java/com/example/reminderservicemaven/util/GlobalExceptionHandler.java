package com.example.reminderservicemaven.util;

import com.example.reminderservicemaven.util.exceptions.ReminderNotFoundException;
import com.example.reminderservicemaven.util.exceptions.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SchedulerException.class)
    public ResponseEntity<String> handleSchedulerException(SchedulerException e) {
        return new ResponseEntity<>("Ошибка при планировании задачи: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        return new ResponseEntity<>("Сущность не найдена: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>("Пользователь не найден: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    public ResponseEntity<String> handleReminderNotFoundException(ReminderNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}


