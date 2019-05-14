package com.iuce.opticaltestreader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.MyHolder> {

    ArrayList<UserKey> myAnswerList;
    LayoutInflater inflater;

    public AnswerAdapter(Context context, ArrayList<UserKey> answers){
        inflater = LayoutInflater.from(context);
        this.myAnswerList = answers;
    }

    @NonNull
    @Override
    public AnswerAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = inflater.inflate(R.layout.answer_card,viewGroup,false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerAdapter.MyHolder myHolder, int position) {

        UserKey selectedAnswer = myAnswerList.get(position);
        myHolder.setData(selectedAnswer,position);
    }

    @Override
    public int getItemCount() {
        return myAnswerList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView answerNumber;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            answerNumber = itemView.findViewById(R.id.txtQuestion);
        }

        @Override
        public void onClick(View v) {

        }

        public void setData(UserKey selectedAnswer, int position) {
            this.answerNumber.setText(selectedAnswer.getQuestionNumber());
        }
    }
}

