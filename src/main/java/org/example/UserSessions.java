package org.example;

public class UserSessions {
    private int testCode;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private Integer messageId; // Новое поле для хранения id сообщения

    public int getTestCode() {return testCode;}
    public void setTestCode(int testCode) {this.testCode = testCode;}

    public int getCurrentQuestionIndex() {return currentQuestionIndex;}
    public void incrementCurrentQuestionIndex() {this.currentQuestionIndex++;}

    public int getCorrectAnswers() {return correctAnswers;}
    public void incrementCorrectAnswers() {this.correctAnswers++;}

    public Integer getMessageId() {return messageId;}
    public void setMessageId(Integer messageId) {this.messageId = messageId;}
}
