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

        if (isInteger(msg)){
            takeTheTest(infoFromMessage);
        }
        if (state == FSM.UserState.IDLE) {
            switch (msg) {
                case "/start" -> start();
                case "/start_using" -> saveUser(infoFromMessage);
                case "/help" -> listOfCommands();
                case "/create_test" -> createTest(infoFromMessage, false);
                case "/take_the_test" -> send(chatId, "Отлично! Что бы начать прохождение теста введи код теста(который должен был тебе скинуть друг)");
                case "/gamble" -> gamble(chatId);
                default ->
                        send(chatId, "Чё умный? Бесполезно. На меня только команды действуют. Посмотреть список команд /help <-(кликабельно(можно нажать))");
            }
        } else if (state == FSM.UserState.WAITING_QUESTION || state == FSM.UserState.WAITING_ANSWERS){
            createTest(infoFromMessage, true);
        }
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

    }

    private void start() {
        send(chatId, "я это сообщение позже сформулирую, если вы уже проходите тест, значит я забыл это сделать, тогда маякните пжпжпж @propolis3");
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
        TakeTheTest f = new TakeTheTest(infoFromMessage);
        f.sendQuestions();
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
                /gamble ()
                /start_using (типа регистрация)
                /create_test
                
                """);
    }

    private void gamble(long chatId) {
        Gamble gamba = new Gamble(chatId);
    }

    public boolean isInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}