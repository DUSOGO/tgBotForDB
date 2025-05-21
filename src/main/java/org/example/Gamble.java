package org.example;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Gamble extends Bot {
    private Long chatId;
    private Update update;

    public Gamble(Long chatId, Update update) {
        this.chatId = chatId;
        this.update = update;
    }

    public Gamble(Long chatId) {
        this.chatId = chatId;
    }

    public void gamba1(String callBackText) {
        System.out.println("gamba is worked");
        SendDice dice = new SendDice(String.valueOf(chatId));
        switch (callBackText) {
            case "dice" -> sendAnimoji(dice);
            case "slots" -> {
                dice.setEmoji("\uD83C\uDFB0");
                sendAnimoji(dice);}
            case "dart" -> {
                dice.setEmoji("\uD83C\uDFAF");
                sendAnimoji(dice);}
        }
    }

    public void selectGameMessage() {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Выберите игру:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        rowList.add(Arrays.asList(
                InlineKeyboardButton.builder().text("\uD83C\uDFB2 бросить кубик").callbackData("dice").build(),
                InlineKeyboardButton.builder().text("\uD83C\uDFB0 крутануть слот").callbackData("slots").build(),
                InlineKeyboardButton.builder().text("\uD83C\uDFAF кинуть дротик").callbackData("dart").build()
        ));

        keyboardMarkup.setKeyboard(rowList);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendAnimoji(SendDice emoji) {
        try {
            execute(emoji);                    //Actually sending the message
        } catch (TelegramApiException e) {
            System.out.println("in method send give you error");
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
