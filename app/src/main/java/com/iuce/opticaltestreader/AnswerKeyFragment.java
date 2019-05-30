package com.iuce.opticaltestreader;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;


public class AnswerKeyFragment extends Fragment {

    public FloatingActionButton fab;
    public TextView examName;
    public TextView txtEmpty;
    public TextView lastKey;

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
        lastKey = view.findViewById(R.id.lastKey);

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

    @Override
    public void onResume() {
        super.onResume();

        Gson gson = new Gson();

        String answers =  PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("exam1", null);
        if(answers!=null){
            List<String> answerArray = gson.fromJson(answers,new TypeToken<List<String>>(){}.getType());
            String message = "";
            for(int i = 0 ; i < answerArray.size(); i++){
                String oneRow = (i + 1) + (i < 9 ? "-  " : "- " ) + answerArray.get(i) + "\n";
                message = message + oneRow;
            }
            lastKey.setText(message);

        }
    }
}
