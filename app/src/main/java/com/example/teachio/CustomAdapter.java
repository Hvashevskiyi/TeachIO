package com.example.teachio;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
// CustomAdapter.java
public class CustomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<LessonTypeItem> lessonTypeItems;

    public CustomAdapter(Context context, ArrayList<LessonTypeItem> lessonTypeItems) {
        this.context = context;
        this.lessonTypeItems = lessonTypeItems;
    }

    @Override
    public int getCount() {
        return lessonTypeItems.size();
    }

    @Override
    public Object getItem(int position) {
        return lessonTypeItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LessonTypeItem lessonTypeItem = (LessonTypeItem) getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView lessonTypeTextView = convertView.findViewById(R.id.lessonTypeTextView);
        TextView subjectTextView = convertView.findViewById(R.id.subjectTextView);
        TextView testsTextView = convertView.findViewById(R.id.testsTextView);

        lessonTypeTextView.setText("Lesson Type: " + lessonTypeItem.getLessonType());
        subjectTextView.setText("Subject: " + lessonTypeItem.getSubjects().get(0).getSubject());
        testsTextView.setText("Tests: " + lessonTypeItem.getSubjects().get(0).getTests().get(0).getTest());

        return convertView;
    }

}

