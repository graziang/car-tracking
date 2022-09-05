package it.peps.cartracking.bot;

import it.peps.cartracking.service.TrackingService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Component
public class TrackingBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TrackingBot.class);

    private final String token;
    private final String username;

    private Map<String, String> ids = new HashMap<>();

    @Lazy
    @Autowired
    public TrackingService trackingService;

    TrackingBot(@Value("${bot.token}") String token, @Value("${bot.username}") String username) {
        this.token = token;
        this.username = username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }



    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(String.valueOf(chatId));
            String text = message.getText();

            if(text.startsWith("/vim_")) {
                String vim = text.substring(5);
                ids.put(String.valueOf(chatId), vim);
                trackingService.getVimStatus().remove(vim);
                trackingService.initClient();
                response.setText("Vim "+ vim + " added for: " + chatId);
                try {
                    execute(response);
                    logger.info("Sent message \"{}\" to {}", text, chatId);
                } catch (TelegramApiException e) {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
                }

            }
        }
    }

    @PostConstruct
    public void start() {
        logger.info("username: {}, token: {}", username, token);
    }

}