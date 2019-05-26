package com.iuce.opticaltestreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class FabFragment extends Fragment {

    public Button btnNext;

    public FabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_fab, container, false);

        btnNext= rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            OpticSheetFragment newFragment = new OpticSheetFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return rootView;
    }
}
