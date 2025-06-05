package org.example;

import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Distribution extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    FSM fsm = new FSM();
    private Long chatId;


    public void messageProcessing(Message infoFromMessage, Update update) {
        String msg = infoFromMessage.getText();
        chatId = infoFromMessage.getChatId();

        var state = fsm.getState(chatId);

        if (state == FSM.UserState.IDLE) {
            switch (msg) {
                case "/start" -> start();
                case "/start_using" -> saveUser(infoFromMessage);
                case "/help" -> listOfCommands();
                case "/create_test" -> createTest(infoFromMessage, false);
                case "/take_the_test" ->
                        send(chatId, "Отлично! Что бы начать прохождение теста введи код, который должен был тебе скинуть друг.\n (Можно не писать эту команду, а сразу скидывать код)");
                case "/gamble" -> gamble(chatId);
                //case "/cancel" -> cancel(); //reset state to idle
                default -> {
                    if (isInteger(msg)) // && (state != FSM.UserState.WAITING_QUESTION
                        takeTheTest(infoFromMessage);
                    else
                        send(chatId, "Чё умный? Бесполезно. На меня только команды действуют. Посмотреть список команд /help <-(кликабельно(можно нажать))");}
            }
        } else if (state == FSM.UserState.WAITING_QUESTION || state == FSM.UserState.WAITING_ANSWER_FOR_PERSONAL_TEST) {
            createTest(infoFromMessage, true);
        }
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        TakeTheTest answer = new TakeTheTest(callbackQuery);
        String callbackData = callbackQuery.getData();
        answer.checkAnswer(callbackData);
    }

    private void start() {
        send(chatId, """
                Салют! Это бот, который позволяет создать своё тест и также пройти сторонний.
                Что бы продолжить - зарегистрируйтесь с помощью команды /start_using <-(можно на неё нажать и она отправится автоматически)
                """);
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
                
                /cancel
                    Отменить действие. Используйте эту команду если вы хотите прервать процесс прохождения или создания.
                
                /gamble
                    отправляет три анимированных эмодзи бла-бла 
                    
                /start_using
                регистрация, 
                
                /create_test
                описание
                
                /take_the_test
                Буквально отправляет сообщение, что бы вы ввели код. (Вы можете сразу отправить код, эта команда не обязательна)
                """);
    }

    private void gamble(long chatId) {
        Gamble gamble = new Gamble(chatId);
    }

    private void cancel(){
        fsm.setState(chatId, FSM.UserState.IDLE);
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