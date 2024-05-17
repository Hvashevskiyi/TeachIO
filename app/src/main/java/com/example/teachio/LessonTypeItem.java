package com.example.teachio;

import java.util.ArrayList;

// LessonTypeItem.java
public class LessonTypeItem {
    private String lessonType;
    private double totalAverageMark;
    private ArrayList<SubjectItem> subjects;

    public LessonTypeItem(String lessonType, double totalAverageMark, ArrayList<SubjectItem> subjects) {
        this.lessonType = lessonType;
        this.totalAverageMark = totalAverageMark;
        this.subjects = subjects;
    }

    public String getLessonType() {
        return lessonType;
    }

    public double getTotalAverageMark() {
        return totalAverageMark;
    }

    public ArrayList<SubjectItem> getSubjects() {
        return subjects;
    }
    // Геттеры для lessonType, totalAverageMark и subjects
}