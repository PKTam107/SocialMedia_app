package com.example.socialmediaapp.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {

    String id, notification, uid, postId, type;

    @ServerTimestamp
    Date time;

    public NotificationModel() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NotificationModel(String id, String notification, String postId, Date time) {
        this.id = id;
        this.notification = notification;
        this.postId = postId;
        this.time = time;
    }

    public NotificationModel(String id, String notification, String uid, String postId, Date time) {
        this.id = id;
        this.notification = notification;
        this.uid = uid;
        this.postId = postId;
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
