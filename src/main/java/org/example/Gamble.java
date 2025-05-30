package org.example;


import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Gamble extends Bot {

    public Gamble(Long chatId) {
        SendDice dice = new SendDice(String.valueOf(chatId));

        SendDice slots = new SendDice(String.valueOf(chatId));
        slots.setEmoji("\uD83C\uDFB0");

        SendDice bowling = new SendDice(String.valueOf(chatId));
        bowling.setEmoji("\uD83C\uDFB3");

        sendAnimoji(dice);
        sendAnimoji(slots);
        sendAnimoji(bowling);
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
