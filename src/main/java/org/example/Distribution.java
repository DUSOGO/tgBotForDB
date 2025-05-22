package org.example;

import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Distribution extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    private Long chatId;


    public void messageProcessing(Message infoFromMessage, Update update) {
        String msg = infoFromMessage.getText();
        chatId = infoFromMessage.getChatId();
        FSM fsm = new FSM();
        var state = fsm.getState(chatId);

        if (state == FSM.UserState.IDLE) {
            switch (msg) {
                case "/start" -> start();
                case "/start_using" -> saveUser(infoFromMessage);
                case "/help" -> listOfCommands();
                case "/create" -> createTest(infoFromMessage, false);
                case "/take" -> takeTheTest(infoFromMessage);
                case "/gamble" -> gamble(chatId, update);
                default ->
                        send(chatId, "Я не понял. Мне нужна команда. Посмотреть список команд /help <-(кликабельно(можно нажать))");
            }
        } else if (state == FSM.UserState.WAITING_QUESTION || state == FSM.UserState.WAITING_ANSWERS){
            createTest(infoFromMessage, true);
        }
    }

    private void start() {
        send(chatId, "");
        send(chatId, "Вообще по задумке тут можно будет создать тест и делиться ими, и узнавать, сколько правильных ответов");
    }

    private void saveUser(Message infoFromMessage) { // save in database
        boolean isDone = jdbc.insertUser(infoFromMessage);
        if (isDone) {
            send(chatId, "Можешь начинать, приятного пользования!");
        }
        send(chatId, "либо я что-то путаю(возможно ошибка произошла), либо ты уже начал использование и уже есть в наших базах(вероятнее всего)");
    }

    private void takeTheTest(Message infoFromMessage) {
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        gamble(chatId, callbackData);
    }


    private void createTest(Message infoFromMessage, boolean isBegin) {
        CreatingTests c = new CreatingTests();
        if (!isBegin) {
            c.startCreating(infoFromMessage);
        } else {
            c.continueCreating(infoFromMessage);
        }
    }

    private void listOfCommands() {
        send(chatId, """
                Список доступных команд:
                /help (этой ты меня и вызвал)
                /gamble
                /start_using (типа регистрация)
                /create
                """);
    }


    private void gamble(Long chatId, Update update) {//my edited 2 years old code
        Gamble gamba = new Gamble(chatId, update);
        gamba.selectGameMessage();
    }

    private void gamble(long chatId, String callbackData) {
        Gamble gamba = new Gamble(chatId);
        gamba.gamba1(callbackData);
    }

}
