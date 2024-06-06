package com.example.reminderservicemaven.dto;

import com.example.reminderservicemaven.models.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderDTO {
    private String title;
    private String description;
    private LocalDateTime remind;
    private int userId;
}
