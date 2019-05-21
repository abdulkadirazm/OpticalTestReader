package com.iuce.opticaltestreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class AnswerKeyActivity extends AppCompatActivity {

    private ListView simpleList;
    private String[] questions;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_key);

        // get the string array from string.xml file
        questions = getResources().getStringArray(R.array.questions);
        // get the reference of ListView and Button
        simpleList = (ListView) findViewById(R.id.simpleListView);
        submit = (Button) findViewById(R.id.submit);
        // set the adapter to fill the data in the ListView
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), questions);
        simpleList.setAdapter(customAdapter);
        // perform setOnClickListerner event on Button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "";
                // get the value of selected answers from custom adapter
                for (int i = 0; i < CustomAdapter.selectedAnswers.size(); i++) {
                    message = message + "\n" + CustomAdapter.selectedAnswers.get(i);
                }
                // display the message on screen with the help of Toast.
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
