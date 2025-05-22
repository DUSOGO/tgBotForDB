package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



import java.util.Random;

public class Bot extends TelegramLongPollingBot {
    private final String botToken = "7414579672:AAEtjoHeKAkb21FA6kpbamMopcy_X9xeHqk";
    private final String botUsername = "@testsForYourFriendsBot";
    //private Long chatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Distribution distribution = new Distribution();

        // Проверяем наличие текстового сообщения
        if (update.hasMessage()) {
            Message infoFromMessage = update.getMessage();
            // Передаем update с текстовым сообщением в обработчик
            distribution.messageProcessing(infoFromMessage, update);
        } else if (update.hasCallbackQuery()) {
            // Обрабатываем обратные вызовы в другом методе
            distribution.handleCallback(update.getCallbackQuery());
        }
    }

    public void send(Long chatId, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                    //Actually sending the message
        } catch (TelegramApiException e) {
            System.out.println("in method send give you error");
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
