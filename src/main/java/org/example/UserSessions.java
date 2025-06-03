package org.example;

public class UserSessions {
    private int testCode;
    private int currentQuestionIndex;
    private int correctAnswers;

    public UserSessions() {
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
    }

    // Геттеры и сеттеры
    public int getTestCode() { return testCode; }
    public void setTestCode(int testCode) { this.testCode = testCode; }

    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int idx) { this.currentQuestionIndex = idx; }
    public void incrementCurrentQuestionIndex() { this.currentQuestionIndex++; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void incrementCorrectAnswers() { this.correctAnswers++; }
}
