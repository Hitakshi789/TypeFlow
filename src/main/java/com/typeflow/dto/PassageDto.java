package com.typeflow.dto;

public class PassageDto {
    private String id;
    private String text;
    private String title;
    private String author;
    private String category;
    private int wordCount;

    public PassageDto() {}

    public PassageDto(String id, String text, String title, String author, String category) {
        this.id = id;
        this.text = text;
        this.title = title;
        this.author = author;
        this.category = category;
        this.wordCount = text != null ? text.trim().split("\\s+").length : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.wordCount = text != null ? text.trim().split("\\s+").length : 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }
}
