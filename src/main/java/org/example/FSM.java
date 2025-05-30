package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 2. FSM-класс для хранения состояний пользователей FROM COPILOT
public class FSM {
    // 1. Описываем состояния пользователя
    public enum UserState {
        IDLE,                        // ничего не делает
        WAITING_QUESTION,            // вводит вопрос
        WAITING_ANSWERS,
        WAITING_CALLBACK
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