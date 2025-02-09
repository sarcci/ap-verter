package com.example.verter;

public class Request {
    String reqId, friendUsername, friendId;
    public Request() {}
    public Request(String reqId, String friendUsername, String friendId) {
        this.reqId = reqId;
        this.friendUsername = friendUsername;
        this.friendId = friendId;
    }
    public String getReqId() {
        return reqId;
    }
    public void setReqId(String reqId) {
        this.reqId = reqId;
    }
    public String getFriendUsername() {
        return friendUsername;
    }
    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    @Override
    public String toString() {
        return "Request{" +
                "reqId='" + reqId + '\'' +
                ", friendUsername='" + friendUsername + '\'' +
                '}';
    }
}
