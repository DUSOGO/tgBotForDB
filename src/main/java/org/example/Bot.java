package org.example;

import jdbc.utils.PropertiesUtil;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.Arrays;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    private final String botToken = PropertiesUtil.get("bot.token");
    private final String botUsername = PropertiesUtil.get("bot.username");

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
            distribution.messageProcessing(infoFromMessage);
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

    public Bot() {
        // Добавляем команды при создании бота
        List<BotCommand> commands = Arrays.asList(
                new BotCommand("/help", "детальный список доступных команд"),
                new BotCommand("/gamble", "кубик, казик, боулинг"),
                new BotCommand("/start_using", "начать использовать (регистрация типа)"),
                new BotCommand("/create_test", "Создать тест"),
                new BotCommand("/take_the_test", "Пройти тест")
        );
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        setMyCommands.setScope(new BotCommandScopeDefault());
        try {
            execute(setMyCommands); // Вызов метода для установки команд
        } catch (TelegramApiException e) {
            System.out.println("Ошибка при установке команд меню");
            System.out.println(e.getMessage());
        }
    }
}
