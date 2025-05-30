package org.example;

import java.util.*;
import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CreatingTests extends Bot {
    static String[] questionsAndAnswer = new String[5];
    static int i;


    FSM state = new FSM();
    JdbcRunner jdbc = new JdbcRunner();

    public void startCreating(Message infoFromMessage) {
        if (jdbc.addTest(infoFromMessage)) {
            Long chatId = infoFromMessage.getChatId();
            send(chatId, "Готово, теперь напиши вопрос (максимум 128 символов)");
            state.setState(chatId, FSM.UserState.WAITING_QUESTION);
        }
    }

    public void continueCreating(Message inf) {
        Long chatId = inf.getChatId();
        var stateNow = state.getState(chatId);

        if (inf.getText().equals("/done")){
            state.reset(chatId);
            String codeTests = String.valueOf(jdbc.getTestId(chatId));
            send(chatId, "Успешно! Пройти твой тест могут по коду: " + codeTests);
            return;
        }
        switch (stateNow) {
            case WAITING_QUESTION -> {
                jdbc.addQuestion(inf);
                questionsAndAnswer[0] = inf.getText();
                send(chatId, "Теперь отправь мне правильный ответ на вопрос (максимум 64 символа)");
                state.setState(chatId, FSM.UserState.WAITING_ANSWERS);
                i++;
            }
            case WAITING_ANSWERS -> {
                switch (i) {
                    case 1 -> {
                        i++;
                        questionsAndAnswer[1] = inf.getText();
                        send(chatId,"Супер, теперь отправь мне второй вариант ответа");
                    }
                    case 2 -> {
                        i++;
                        questionsAndAnswer[2] = inf.getText();
                        send(chatId,"Супер, теперь отправь мне третий вариант ответа");
                    }
                    case 3 -> {
                        i++;
                        questionsAndAnswer[3] = inf.getText();
                        send(chatId,"Супер, теперь отправь мне четвертый вариант ответа");
                    }
                    case 4 -> {
                        questionsAndAnswer[4] = inf.getText();
                        jdbc.addAnswers(questionsAndAnswer[0], questionsAndAnswer[1], questionsAndAnswer[2], questionsAndAnswer[3], questionsAndAnswer[4]);
                        state.setState(chatId, FSM.UserState.WAITING_QUESTION);
                        i = 0;
                        Arrays.fill(questionsAndAnswer, null);
                        send(chatId, "(если хочешь завершить создание теста отправь /done)");
                        send(chatId, "Отправь следующий вопрос:");
                    }
                }
            }
        }
    }

}