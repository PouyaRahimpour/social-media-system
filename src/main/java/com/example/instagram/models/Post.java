package com.example.instagram.models;

import java.util.ArrayList;

public class Post {
    User user;
    String content;
    int score;
    String imagePath;
    private void firstSetUP() {
        imagePath = "C:/Users/Asus/IdeaProjects/instagram/src/main/resources/com/example/instagram/images/insta_post.png";
        score = 0;
    }
    public Post() {
        firstSetUP();
    }
    public Post(String content, int score, String imagePath) {
        firstSetUP();
        this.imagePath = imagePath;
        this.content = content;
        this.score = score;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
