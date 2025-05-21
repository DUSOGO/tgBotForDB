package org.example;

import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Distribution extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    private Long chatId;


    public void messageProcessing(Message infoFromMessage, Update update) {
        System.out.println("messageProcessing has been called");
        String msg = infoFromMessage.getText();
        chatId = infoFromMessage.getChatId();

        switch (msg) {
            case "/start" -> start();
            case "/start_using" -> saveUser(infoFromMessage);
            case "/help" -> listOfCommands();
            case "/create" -> createTest(infoFromMessage);
            case "/take" -> takeTheTest(infoFromMessage);
            case "/gamble" -> gamble(chatId, update);
            default ->
                    send(chatId, "Я не понял. Мне нужна команда. Посмотреть список комманд /help <-(кликабельно(можно нажать))");
        }
    }

    private void start() {
        System.out.println("start is worked");
        send(chatId, "Приветствую, тут в дальнейшем будет что-то, а пока можешь покрутить слоты /gamble.");
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
        System.out.println("Handling callback query");

        long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        System.out.println("Callback data: " + callbackData);

        gamble(chatId, callbackData);
    }


    private void createTest(Message infoFromMessage) {
        CreatingTests c = new CreatingTests();
        c.startCreating(infoFromMessage);
    }

    private void listOfCommands() {
        send(chatId, """
                Список доступных команд:
                /help (этой ты меня и вызвал)
                /gamble
                /start_using (типа регистрация)
                """);
    }


    private void gamble(Long chatId, Update update) {//my edited 2 years old code
        System.out.println("gamble in distribution is worked");
        Gamble gamba = new Gamble(chatId, update);
        gamba.selectGameMessage();
    }

    private void gamble(long chatId, String callbackData) {
        System.out.println("gamble in distribution is worked");
        Gamble gamba = new Gamble(chatId);
        gamba.gamba1(callbackData);
    }

}
