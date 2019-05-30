package com.iuce.opticaltestreader;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;


public class AnswerKeyFragment extends Fragment {

    public FloatingActionButton fab;
    public TextView examName;
    public TextView txtEmpty;

    public AnswerKeyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_answer_key, container, false);

        txtEmpty = view.findViewById(R.id.txtEmpty);
        examName = view.findViewById(R.id.examName);

        Gson gson = new Gson();
        String exam =  PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("ExamName", null);

        if (exam!=null){
            examName.setText(exam + " Exam");
        }
        else
            txtEmpty.setText(R.string.emptyText);


        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            FabFragment newFragment = new FabFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }
}
