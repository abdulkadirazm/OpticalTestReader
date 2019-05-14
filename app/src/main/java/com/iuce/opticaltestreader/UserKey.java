package com.iuce.opticaltestreader;

import java.util.ArrayList;

public class UserKey {

    private String questionNumber;

    public UserKey(){

    }

    public UserKey(String questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(String questionNumber) {
        this.questionNumber = questionNumber;
    }

    public static ArrayList<UserKey> getData(){

        ArrayList<UserKey> userKeyArrayList = new ArrayList<>();

        String[] answerNumbers = {"1)","2)","3)","4)","5)","6)","7)","8)","9)","10",
                                "11)","12)","13)","14)","15)","16)","17)","18)","19)","20)"};

        for (int i=0; i<answerNumbers.length; i++){
            UserKey temp = new UserKey();
            temp.setQuestionNumber(answerNumbers[i]);

            userKeyArrayList.add(temp);
        }
        return userKeyArrayList;
    }
}
