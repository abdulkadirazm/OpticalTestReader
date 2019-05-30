package com.iuce.opticaltestreader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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


public class ProfilFragment extends Fragment {

    public FirebaseAuth firebaseAuth;
    public Button btnSignout;
    public TextView textName;
    private ListView profilListview;
    public String[] links = {"Tutorial","Answer Sheets","Open Source Libraries","Privacy Policy","Contact Us","Share App","About"};

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

            }
            else if (position == 4){
                aboutUs();
            }else if (position == 5){
                shareApp();
            } else if (position == 6){
                showAbout();
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle("Do you want to exit?");
        builder1.setIcon(R.drawable.warning);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    firebaseAuth.signOut();
                    startActivity(new Intent(getActivity(), Login.class));
                    getActivity().finish();
                });

        builder1.setNegativeButton(
                "No",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    protected void showAbout() {
        // Inflate the about_app message contents
        View messageView = getLayoutInflater().inflate(R.layout.about_app, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView =  messageView.findViewById(R.id.txtAbout);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
    public void shareApp() {
        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");
        String shareBody = "";
        String shareSub = "https://play.google.com/store/apps/details?id=com.opticaltestreader=tr";
        myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
        myIntent.putExtra(Intent.EXTRA_TEXT,shareSub);
        startActivity(Intent.createChooser(myIntent,"Share using"));
    }
    public void aboutUs(){
        // Inflate the about_app message contents
        View messageView = getLayoutInflater().inflate(R.layout.about_us, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
}
