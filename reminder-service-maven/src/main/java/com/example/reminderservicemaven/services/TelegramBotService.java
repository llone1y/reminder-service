package com.example.reminderservicemaven.services;

import com.example.reminderservicemaven.config.TelegramBotConfig;
import com.example.reminderservicemaven.models.User;
import com.example.reminderservicemaven.util.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig config;
    private final UserService userService;
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            handleCommand(chatId, messageText);
        }
    }

    private void handleCommand(long chatId, String command) {
        switch (command) {
            case "/start":
                sendStartMessage(chatId);
                break;
            case "/yes":
                sendEmailPrompt(chatId);
                break;
            default:
                handleEmailInput(chatId, command);
                break;
        }
    }

    private void sendStartMessage(long chatId) {
        try {
            User user = userService.findUserByChatIdOrThrowException(String.valueOf(chatId));
            sendMessage(chatId, "Hi " + user.getUsername());
        } catch (UserNotFoundException e) {
            sendMessage(chatId, "Sorry, you don't have a chatId. Want to create one?");
        }
    }

    private void sendEmailPrompt(long chatId) {
        sendMessage(chatId, "Please enter your email");
    }

    private void handleEmailInput(long chatId, String email) {
        try {
            User user = userService.findUserByEmailOrThrowException(email);
            userService.addChatId(String.valueOf(chatId), user);
            sendMessage(chatId, "You added chatId successfully: " + chatId);
        } catch (UserNotFoundException e) {
            sendMessage(chatId, "No user with such email");
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
