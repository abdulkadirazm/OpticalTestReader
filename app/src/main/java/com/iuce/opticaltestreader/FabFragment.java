package com.iuce.opticaltestreader;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;


public class FabFragment extends Fragment {

    public Button btnNext;
    public AutoCompleteTextView txtExamName;

    public FabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_fab, container, false);

        txtExamName = rootView.findViewById(R.id.txtExamName);
        btnNext= rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            String examName = txtExamName.getText().toString().trim();
            if (examName.isEmpty()){
                Toast.makeText(getActivity(),"Exam name should not be empty",Toast.LENGTH_SHORT).show();
            }
            else
                PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit().putString("ExamName", examName).apply();

                fragmentIntent();
        });

        return rootView;
    }
    public void fragmentIntent(){
        OpticSheetFragment newFragment = new OpticSheetFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, newFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
