package org.example;

import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TakeTheTest extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    FSM state = new FSM();

    private Long chatId;
    // Используем ConcurrentHashMap для многопоточности
    private static final Map<Long, UserSessions> userSessions = new ConcurrentHashMap<>();

    public TakeTheTest(Message infoFromMessage) {
        this.chatId = infoFromMessage.getChatId();
        int code = Integer.parseInt(infoFromMessage.getText()); // получаем код теста
        UserSessions session = new UserSessions();
        session.setTestCode(code); // сохраняем код теста в сессию
        userSessions.put(chatId, session);
        state.setState(chatId, FSM.UserState.WAITING_ANSWER_CALLBACK);
    }

    public TakeTheTest(CallbackQuery callback) {
        this.chatId = callback.getMessage().getChatId();
    }

    // begin the logic
    public void sendQuestions() {
        UserSessions session = userSessions.get(chatId);
        if (session == null) {
            send(chatId, "Ошибка: не найдена сессия пользователя. Начните тест заново.");
            return;
        }
        String[][] test = jdbc.getTest(session.getTestCode());
        if (isEmpty(test)) {
            send(chatId, "тест с таким кодом не найден, он, наверное, не правильный");
            state.setState(chatId, FSM.UserState.IDLE);
            return;
        }
        int idx = session.getCurrentQuestionIndex();
        String questionText = test[idx][0];
        ReplyKeyboard keyboard = buildKeyboard(test, idx);

        Integer lastMsgId = session.getMessageId();
        if (lastMsgId == null) {
            // Первый вопрос — отправляем обычное сообщение и сохраняем его id
            SendMessage msg = new SendMessage(String.valueOf(chatId), questionText);
            msg.setReplyMarkup(keyboard);
            Integer sentMsgId = sendMessageAndReturnId(msg);
            session.setMessageId(sentMsgId);
        } else {
            // Следующий вопрос — редактируем прошлое сообщение
            EditMessageText editMsg = new EditMessageText();
            editMsg.setChatId(chatId.toString());
            editMsg.setMessageId(lastMsgId);
            editMsg.setText(questionText);
            editMsg.setReplyMarkup((InlineKeyboardMarkup) keyboard);
            Integer editedMsgId = editMessageAndReturnId(editMsg);
            session.setMessageId(editedMsgId); // Обычно messageId не меняется
        }
        session.incrementCurrentQuestionIndex();
        state.setState(chatId, FSM.UserState.WAITING_ANSWER_CALLBACK);
    }

    public void checkAnswer(String isTrue) {
        UserSessions session = userSessions.get(chatId);
        if (session == null) {
            send(chatId, "Ошибка: не найдена сессия пользователя. Начните тест заново.");
            return;
        }
        if (Boolean.parseBoolean(isTrue)) {
            session.incrementCorrectAnswers();
        }
        if (isLast(session)) {
            String[][] test = jdbc.getTest(session.getTestCode());
            int totalQuestions = test.length;
            int correct = session.getCorrectAnswers();
            long ownerChatId = jdbc.getChatIdOfTestOwner(session.getTestCode());
            send(ownerChatId, "ваш тест только что был пройден @" + jdbc.getUsernameByChatId(chatId) +", И он(а) набрал(а) " + correct + " из " + totalQuestions + " правильных ответов");
            send(chatId, "вы прошли тест на " + correct + " из " + totalQuestions + " правильных ответов");
            state.setState(chatId, FSM.UserState.IDLE);
            userSessions.remove(chatId); // очищаем сессию пользователя
        } else {
            sendQuestions();
        }
    }

    private boolean isLast(UserSessions session) {
        String[][] test = jdbc.getTest(session.getTestCode());
        return session.getCurrentQuestionIndex() == test.length;
    }

    // Новый метод для отправки сообщения и возврата его id
    private Integer sendMessageAndReturnId(SendMessage msg) {
        try {
            Message sentMsg = execute(msg);
            return sentMsg.getMessageId();
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Новый метод для редактирования сообщения и возврата его id
    private Integer editMessageAndReturnId(EditMessageText editMsg) {
        try {
            Serializable response = execute(editMsg);
            if (response instanceof Message) {
                return ((Message) response).getMessageId();
            } else {
                return null; // или обработка ошибки
            }
        } catch (TelegramApiException e) {
            System.out.println("Ошибка редактирования сообщения: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Перемешиваем варианты ответов
    private InlineKeyboardMarkup buildKeyboard(String[][] test, int idx) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        // Первый вариант (правильный)
        InlineKeyboardButton correctBtn = new InlineKeyboardButton(test[idx][1]);
        correctBtn.setCallbackData("true");
        buttons.add(correctBtn);
        // Остальные варианты (неправильные)
        for (int i = 2; i <= 4; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton(test[idx][i].trim());
            btn.setCallbackData("false");
            buttons.add(btn);
        }
        // Перемешиваем кнопки
        Collections.shuffle(buttons);

        // Каждая кнопка в своей строке
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (InlineKeyboardButton btn : buttons) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(btn);
            rows.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private boolean isEmpty(String[][] test) {
        return test == null || test.length == 0 || test[0][0] == null;
    }
}