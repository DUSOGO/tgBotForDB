package org.example;


import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.List;

public class TakeTheTest extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    FSM state = new FSM();
    private int code;
    private Long chatId;

    String[][] test = jdbc.getTest(code);
    private static int counterOfCorrectAnswers;
    private static int currentQuestionsIndex = 0;

    public void sendQuestions(){
        System.out.println("sendQuestions ис воркед");
        //int[] qn234 = {2,3,4}; // for random select false questions
        SendMessage msg = null;
        try {
            msg = new SendMessage(String.valueOf(chatId), test[currentQuestionsIndex][0]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        var button1True = new InlineKeyboardButton(test[currentQuestionsIndex][1]);
        button1True.setCallbackData("true");
        List<InlineKeyboardButton> rowBtn1True = new ArrayList<>();
        rowBtn1True.add(button1True);
        rowsInLine.add(rowBtn1True);
        var button2 = new InlineKeyboardButton(test[currentQuestionsIndex][2]);
        button2.setCallbackData("false");
        List<InlineKeyboardButton> rowBtn2 = new ArrayList<>();
        rowBtn1True.add(button2);
        rowsInLine.add(rowBtn2);
        var button3 = new InlineKeyboardButton(test[currentQuestionsIndex][3]);
        button3.setCallbackData("false");
        List<InlineKeyboardButton> rowBtn3 = new ArrayList<>();
        rowBtn1True.add(button3);
        rowsInLine.add(rowBtn3);
        var button4 = new InlineKeyboardButton(test[currentQuestionsIndex][4]);
        button4.setCallbackData("false");
        List<InlineKeyboardButton> rowBtn4 = new ArrayList<>();
        rowBtn1True.add(button4);
        rowsInLine.add(rowBtn4);

        markupInLine.setKeyboard(rowsInLine);
        msg.setReplyMarkup(markupInLine);
        sendMessage(msg);

        currentQuestionsIndex++;
        state.setState(chatId, FSM.UserState.WAITING_CALLBACK);
    }

    public TakeTheTest(Message infoFromMessage) {
        this.code = Integer.parseInt(infoFromMessage.getText());
        this.chatId = infoFromMessage.getChatId();
    }

    private void sendMessage(SendMessage msg) {
        System.out.println("отправка сообщений с вопросами сработала");
        try {
            execute(msg);                    //Actually sending the message
        } catch (TelegramApiException e) {
            System.out.println("in method send give you error");
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}