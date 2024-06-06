package com.example.reminderservicemaven.util.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReminderNotFoundException extends RuntimeException{
    public ReminderNotFoundException(String message) {
        super(message);
    }
}
