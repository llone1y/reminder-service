package com.example.reminderservicemaven.quartzJobs;

import com.example.reminderservicemaven.services.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
public class TelegramJob extends QuartzJobBean {

    private final TelegramBotService bot;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String chatId = context.getMergedJobDataMap().getString("chatId");
        String messageText = context.getMergedJobDataMap().getString("messageText");
        SendMessage message = new SendMessage();

        message.setText(messageText);
        message.setChatId(chatId);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }
}
