package com.iuce.opticaltestreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomAdapter extends BaseAdapter {
    Context context;
    String[] questionsList;
    LayoutInflater inflter;
    public static ArrayList<String> selectedAnswers;


    public CustomAdapter(Context applicationContext, String[] questionsList) {
        this.context = context;
        this.questionsList = questionsList;
        // initialize arraylist and add static string for all the questions
        selectedAnswers = new ArrayList<>();
        for (int i = 0; i < questionsList.length; i++) {
            selectedAnswers.add("Not Attempted");
        }
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return questionsList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.list_items, null);
    // get the reference of TextView and Button's
        TextView question = view.findViewById(R.id.txtQuestion);
        final RadioButton opt1 = view.findViewById(R.id.optA);
        final RadioButton opt2 = view.findViewById(R.id.optB);
        final RadioButton opt3 = view.findViewById(R.id.optC);
        final RadioButton opt4 = view.findViewById(R.id.optD);

        if(selectedAnswers.get(i).equals("Not Attempted")){

        }
        else  if(selectedAnswers.get(i).equals("A")){
            opt1.setChecked(true);
            opt2.setChecked(false);
            opt3.setChecked(false);
            opt4.setChecked(false);
        }
        else if(selectedAnswers.get(i).equals("B")){
            opt2.setChecked(true);
            opt1.setChecked(false);
            opt3.setChecked(false);
            opt4.setChecked(false);
        }
        else if(selectedAnswers.get(i).equals("C")){
            opt3.setChecked(true);
            opt2.setChecked(false);
            opt1.setChecked(false);
            opt4.setChecked(false);
        }
        else if(selectedAnswers.get(i).equals("D")){
            opt4.setChecked(true);
            opt2.setChecked(false);
            opt3.setChecked(false);
            opt1.setChecked(false);
        }



        // perform setOnCheckedChangeListener event on yes button
        opt1.setOnCheckedChangeListener((buttonView, isChecked) -> {
        // set Yes values in ArrayList if RadioButton is checked
            if (isChecked) {
                selectedAnswers.set(i, "A");
                opt1.setChecked(true);
            }
        });

        // perform setOnCheckedChangeListener event on no button
        opt2.setOnCheckedChangeListener((buttonView, isChecked) -> {
        // set No values in ArrayList if RadioButton is checked
            if (isChecked) {
                selectedAnswers.set(i, "B");
                opt2.setChecked(true);
            }
        });

        // perform setOnCheckedChangeListener event on no button
        opt3.setOnCheckedChangeListener((buttonView, isChecked) -> {
        // set No values in ArrayList if RadioButton is checked
            if (isChecked) {
                selectedAnswers.set(i, "C");
                opt3.setChecked(true);
            }
        });

        // perform setOnCheckedChangeListener event on no button
        opt4.setOnCheckedChangeListener((buttonView, isChecked) -> {
        // set No values in ArrayList if RadioButton is checked
            if (isChecked) {
                selectedAnswers.set(i, "D");
                opt4.setChecked(true);
            }
        });

        // set the value in TextView
        question.setText(questionsList[i]);
        return view;
    }
}