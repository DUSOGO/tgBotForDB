package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 2. FSM-класс для хранения состояний пользователей FROM COPILOT
public class FSM {
    // 1. Описываем состояния пользователя
    public enum UserState {
        IDLE,                        // wait any command
        WAITING_QUESTION,            // when the test is creating
        WAITING_ANSWER_FOR_PERSONAL_TEST, // when the test is creating
        WAITING_ANSWER_CALLBACK  // WHEN the test is in progress
    }

    // Хранит состояния пользователей по chatId
    private static final Map<Long, UserState> userStates = new ConcurrentHashMap<>();


    // Получить текущее состояние пользователя
    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.IDLE);
    }

    // Установить новое состояние пользователя
    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    // Сбросить состояние пользователя
    public void reset(Long chatId) {
        userStates.remove(chatId);
    }
}