package com.shtrih.tinyjavapostester.task.message;

public class Message {

    private String title;
    private String text;

    public Message(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public Message(String title) {
        this.title = title;
        this.text = "Пожалуйста, подождите...";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
