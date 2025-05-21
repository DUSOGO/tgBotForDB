package org.example;

import jdbc.JdbcRunner;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CreatingTests extends Bot {
    JdbcRunner jdbc = new JdbcRunner();
    Message infoFromMessage;
    Long chatId = infoFromMessage.getChatId();

    public void startCreating(Message infoFromMessage) {
        this.infoFromMessage = infoFromMessage;
        if (jdbc.addTest(infoFromMessage)) {
            send(chatId, "Готово, теперь следует добавить вопросы");
        }
    }
}