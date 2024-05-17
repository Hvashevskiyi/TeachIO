package com.example.teachio;

import java.io.Serializable;

public class Question implements Serializable {
    private String text;
    private String correctAnswer;
    private int cost;

    public Question(String text, String correctAnswer, int cost) {
        this.text = text;
        this.correctAnswer = correctAnswer;
        this.cost = cost;
    }

    public String getText() {
        return text;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public int getCost() {
        return cost;
    }
}
