package com.iuce.opticaltestreader;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


public class OpticSheetFragment extends Fragment {

    public ListView simpleList;
    public String[] questions;
    public Button submit;

    public OpticSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_optic_sheet, container, false);

        // get the string array from string.xml file
        questions = getResources().getStringArray(R.array.questions);
        // get the reference of ListView and Button
        simpleList = rootView.findViewById(R.id.simpleListView);
        submit =  rootView.findViewById(R.id.submit);
        // set the adapter to fill the data in the ListView
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), questions);
        simpleList.setAdapter(customAdapter);
        // perform setOnClickListerner event on Button
        submit.setOnClickListener(v -> {
            String message = "";
            // get the value of selected answers from custom adapter
            for (int i = 0; i < CustomAdapter.selectedAnswers.size(); i++) {
                message = message + "\n" + CustomAdapter.selectedAnswers.get(i);
            }
            // display the message on screen with the help of Toast.
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        });

        return rootView;
    }


}
