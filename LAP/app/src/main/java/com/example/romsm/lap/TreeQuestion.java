package com.example.romsm.lap;

public class TreeQuestion {
    int trueID, falseID, questionID;
    String question;
    Boolean isSelected;

    public TreeQuestion(int trueID, int falseID, int questionID, String question){
        this.trueID = trueID;
        this.falseID = falseID;
        this.questionID = questionID;
        this.question = question;
        this.isSelected = false;
    }

    public int getTrueID() {
        return trueID;
    }

    public int getFalseID() {
        return falseID;
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getQuestion() {
        return question;
    }

    public boolean getIsSelected(){ return isSelected; }

    public void setIsSelected(boolean isSelected){
        this.isSelected = isSelected;
    }

    public void toggleSelected(){
        isSelected = !isSelected;
    }
}
