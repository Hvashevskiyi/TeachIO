package com.example.teachio;

public class TestQuestion {
    private int testId;
    private String questionName;
    private String correctAnswer;
    private int cost;

    public TestQuestion(String questionName, String correctAnswer, int cost) {
        this.questionName = questionName;
        this.correctAnswer = correctAnswer;
        this.cost = cost;
    }
    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }
    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
