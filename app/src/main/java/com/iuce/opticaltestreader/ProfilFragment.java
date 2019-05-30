package com.iuce.opticaltestreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ProfilFragment extends Fragment {

    public FirebaseAuth firebaseAuth;
    public Button btnSignout;
    public TextView textName;
    private ListView profilListview;
    public String[] links = {"Tutorial","Answer Sheets","Open Source Libraries","Privacy Policy","Contact Us","About"};

    public ProfilFragment() {
        // Required empty public constructor
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profil, container, false);

        textName = view.findViewById(R.id.textName);
        btnSignout = view.findViewById(R.id.btnSignout);
        profilListview = view.findViewById(R.id.profilListview);
        firebaseAuth = FirebaseAuth.getInstance();

        // Firebase Signout Click
        btnSignout.setOnClickListener(v -> logOut());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,android.R.id.text1,links);
        profilListview.setAdapter(arrayAdapter);

        profilListview.setOnItemClickListener((parent, view1, position, id) -> {

            if (position == 0){
                Toast.makeText(getActivity(),position+". Click",Toast.LENGTH_SHORT).show();
            }
        });


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    User user = dataSnapshot1.getValue(User.class);
                    textName.setText("Welcome " +user.getName() + ":)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(),"Failed to read value",Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
    public void logOut(){
        firebaseAuth.signOut();
        startActivity(new Intent(getActivity(), Login.class));
        getActivity().finish();
    }
}
