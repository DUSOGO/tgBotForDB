package org.example;

import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Distribution extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    FSM fsm = new FSM();
    private Long chatId;


    public void messageProcessing(Message infoFromMessage) {
        String msg = infoFromMessage.getText();
        chatId = infoFromMessage.getChatId();

        var state = fsm.getState(chatId);

        if (state == FSM.UserState.IDLE) {
            switch (msg) {
                case "/start" -> start(infoFromMessage);
                case "/help" -> listOfCommands();
                case "/create_test" -> createTest(infoFromMessage, false);
                case "/take_the_test" -> beginTakeTheTest();
                case "/gamble" -> gamble(chatId);
                case "/about_bot" -> sendMessageAboutBot();
                default -> defaultMessageOrCodeTest(infoFromMessage, msg);
            }
        } else if (state == FSM.UserState.WAITING_QUESTION || state == FSM.UserState.WAITING_ANSWER_FOR_PERSONAL_TEST) {
            createTest(infoFromMessage, true);
        }
    }

    private void sendMessageAboutBot() {
        send(chatId, """
                В этом боте вы сможете создать свой тест с неограниченным количеством вопросов, а также пройти уже созданный тест.
        Немного о том как он работает: Вы пользуетесь командой дя создания теста, и бот запросит у вас сам вопрос, а после правильный вариант ответа, затем и остальные, неверные (всего вариантов ответа 4). После он повторит процедуру. Если не хотите создать новый вопрос и желаете завершить тест -- используйте /done. По завершению создания теста бот отправит вам код, с помощью которого другие люди смогут пройти ваш тест.
        Что бы пройти тест, вам следует использовать (необязательно) соответствующую команду и ввести код теста. Он есть у его владельца.
        По прохождению теста вам и создателю теста будут отправлены результаты (количество верных ответов)
        Вы можете создать только один тест и перезаписать его(поменять текущий на новый) при надобности, воспользовавшись командой для создания теста. Ему будет присвоен отдельный код теста, а предыдущий удалится и не будет работать
        
        Этот бот находится в разработке, возможны ошибки или недоработки.
        Если что вы всегда можете обратиться ко мне в телеграм: @propolis3""");
    }

    private void defaultMessageOrCodeTest(Message infoFromMessage, String msg) {
        if (isInteger(msg))
            takeTheTest(infoFromMessage);
        else
            send(chatId, "Чё умный? Бесполезно. На меня только команды действуют. Посмотреть список команд /help <-(кликабельно(можно нажать))");
    }

    private void beginTakeTheTest() {
        send(chatId, "Отлично! Что бы начать прохождение теста введи код, который должен был тебе скинуть друг.");
        fsm.setState(chatId, FSM.UserState.IDLE);
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        TakeTheTest answer = new TakeTheTest(callbackQuery);
        String callbackData = callbackQuery.getData();
        answer.checkAnswer(callbackData);
    }

    private void start(Message infoFromMessage) {
        String name = infoFromMessage.getFrom().getFirstName();
        send(chatId, """
                Салют, %s!
                
                В этом боте вы сможете создать свой тест с неограниченным количеством вопросов, а также пройти уже созданный тест.
                Немного о том как он работает: Вы пользуетесь командой дя создания теста, и бот запросит у вас сам вопрос, а после правильный вариант ответа, затем и остальные, неверные (всего вариантов ответа 4). После он повторит процедуру. Если не хотите создать новый вопрос и желаете завершить тест -- используйте /done. По завершению создания теста бот отправит вам код, с помощью которого другие люди смогут пройти ваш тест.
                Что бы пройти тест, вам следует использовать (необязательно) соответствующую команду и ввести код теста. Он есть у его владельца.
                По прохождению теста вам и создателю теста будут отправлены результаты (количество верных ответов)
                Вы можете создать только один тест и перезаписать его(поменять текущий на новый) при надобности, воспользовавшись командой для создания теста. Ему будет присвоен отдельный код теста, а предыдущий удалится и не будет работать
                
                Этот бот находится в разработке, возможны ошибки или недоработки.
                Если что вы всегда можете обратиться ко мне в телеграм: @propolis3
                """.formatted(name));
        saveUser(infoFromMessage);
        listOfCommands();
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
                
                                /about_bot
                Отправляет сообщение о боте, как он работает, инструкции, контакт создателя
                
                                /help
                Показывает все доступные команды (этой командой и вызвано данное сообщение)
                                
                                /gamble
                Используйте что бы отправить три анимированных эмодзи со случайным шансом выпадения. Кубик (кость), слоты и боулинг. Развлечение.
                
                                /create_test
                Используйте что бы начать создание теста
                
                                /take_the_test
                Используйте что бы начать прохождение теста
                """);
    }

    private void gamble(long chatId) {
        Gamble gamble = new Gamble(chatId);
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