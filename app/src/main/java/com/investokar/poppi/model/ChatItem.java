package com.investokar.poppi.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import com.investokar.poppi.constants.Constants;

public class ChatItem extends Application implements Constants, Parcelable {

    private long fromUserId;
    private int id, fromUserState, createAt, listId = 0, seenAt;
    private String fromUserUsername, fromUserFullname, fromUserPhotoUrl, message, imageUrl, timeAgo, date;

    public ChatItem() {

    }

    public ChatItem(JSONObject jsonData) {

        try {

            this.setId(jsonData.getInt("id"));
            this.setFromUserId(jsonData.getLong("fromUserId"));
            this.setFromUserState(jsonData.getInt("fromUserState"));
            this.setFromUserUsername(jsonData.getString("fromUserUsername"));
            this.setFromUserFullname(jsonData.getString("fromUserFullname"));
            this.setFromUserPhotoUrl(jsonData.getString("fromUserPhotoUrl"));
            this.setMessage(jsonData.getString("message"));
            this.setImageUrl(jsonData.getString("imageUrl"));
            this.setCreateAt(jsonData.getInt("createAt"));
            this.setSeenAt(jsonData.getInt("seenAt"));
            this.setDate(jsonData.getString("date"));
            this.setTimeAgo(jsonData.getString("timeAgo"));

        } catch (Throwable t) {

            Log.e("ChatItem", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("ChatItem", jsonData.toString());
        }
    }

    public void setListId(int listId) {

        this.listId = listId;
    }

    public int getListId() {

        return this.listId;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getId() {

        return this.id;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public long getFromUserId() {

        return this.fromUserId;
    }

    public void setFromUserState(int fromUserState) {

        this.fromUserState = fromUserState;
    }

    public int getFromUserState() {

        return this.fromUserState;
    }

    public void setFromUserUsername(String fromUserUsername) {

        this.fromUserUsername = fromUserUsername;
    }

    public String getFromUserUsername() {

        return this.fromUserUsername;
    }

    public void setFromUserFullname(String fromUserFullname) {

        this.fromUserFullname = fromUserFullname;
    }

    public String getFromUserFullname() {

        return this.fromUserFullname;
    }

    public void setFromUserPhotoUrl(String fromUserPhotoUrl) {

        this.fromUserPhotoUrl = fromUserPhotoUrl;
    }

    public String getFromUserPhotoUrl() {

        return this.fromUserPhotoUrl;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getMessage() {

        return this.message;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {

        return this.imageUrl;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public String getDate() {

        return this.date;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }

    public String getTimeAgo() {

        return this.timeAgo;
    }

    public void setCreateAt(int createAt) {

        this.createAt = createAt;
    }

    public int getCreateAt() {

        return this.createAt;
    }

    public void setSeenAt(int seenAt) {

        this.seenAt = seenAt;
    }

    public int getSeenAt() {

        return this.seenAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.fromUserId);
        dest.writeInt(this.id);
        dest.writeInt(this.fromUserState);
        dest.writeInt(this.createAt);
        dest.writeInt(this.listId);
        dest.writeInt(this.seenAt);
        dest.writeString(this.fromUserUsername);
        dest.writeString(this.fromUserFullname);
        dest.writeString(this.fromUserPhotoUrl);
        dest.writeString(this.message);
        dest.writeString(this.imageUrl);
        dest.writeString(this.timeAgo);
        dest.writeString(this.date);
    }

    protected ChatItem(Parcel in) {
        this.fromUserId = in.readLong();
        this.id = in.readInt();
        this.fromUserState = in.readInt();
        this.createAt = in.readInt();
        this.listId = in.readInt();
        this.seenAt = in.readInt();
        this.fromUserUsername = in.readString();
        this.fromUserFullname = in.readString();
        this.fromUserPhotoUrl = in.readString();
        this.message = in.readString();
        this.imageUrl = in.readString();
        this.timeAgo = in.readString();
        this.date = in.readString();
    }

    public static final Creator<ChatItem> CREATOR = new Creator<ChatItem>() {
        @Override
        public ChatItem createFromParcel(Parcel source) {
            return new ChatItem(source);
        }

        @Override
        public ChatItem[] newArray(int size) {
            return new ChatItem[size];
        }
    };
}
