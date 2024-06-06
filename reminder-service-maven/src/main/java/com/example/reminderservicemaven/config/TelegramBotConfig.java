package com.example.reminderservicemaven.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("/application.properties")
public class TelegramBotConfig {
    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String token;
}
