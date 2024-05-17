package com.example.teachio;

import java.util.ArrayList;

public class SubjectItem {
    private String subject;
    private double averageMark;
    private ArrayList<TestItem> tests;

    public SubjectItem(String subject, double averageMark, ArrayList<TestItem> tests) {
        this.subject = subject;
        this.averageMark = averageMark;
        this.tests = tests;
    }
    public String getSubject() {
        return subject;
    }

    public double getAverageMark() {
        return averageMark;
    }

    public ArrayList<TestItem> getTests() {
        return tests;
    }
    // Геттеры для subject, averageMark и tests
}