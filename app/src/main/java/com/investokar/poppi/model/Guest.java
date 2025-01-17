package com.investokar.poppi.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.investokar.poppi.constants.Constants;

import org.json.JSONObject;


public class Guest extends Application implements Constants, Parcelable {

    private long id, guestTo, guestUserId;

    private int pro;

    private String guestUserUsername, guestUserFullname, guestUserPhoto, timeAgo;

    private Boolean online = false;

    private int photoModerateAt = 0;

    public Guest() {


    }

    public Guest(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setGuestUserId(jsonData.getLong("guestUserId"));
                this.setGuestUserUsername(jsonData.getString("guestUserUsername"));
                this.setGuestUserFullname(jsonData.getString("guestUserFullname"));
                this.setGuestUserPhotoUrl(jsonData.getString("guestUserPhoto"));
                this.setGuestTo(jsonData.getLong("guestTo"));
                this.setTimeAgo(jsonData.getString("timeAgo"));
                this.setOnline(jsonData.getBoolean("guestUserOnline"));

                if (jsonData.has("guestUserPro")) {

                    this.setGuestUserPro(jsonData.getInt("guestUserPro"));

                } else {

                    this.setGuestUserPro(0);
                }

                if (jsonData.has("photoModerateAt")) {

                    this.setPhotoModerateAt(jsonData.getInt("photoModerateAt"));
                }
            }

        } catch (Throwable t) {

            Log.e("Guest", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Guest", jsonData.toString());
        }
    }

    public void setId(long id) {

        this.id = id;
    }

    public long getId() {

        return this.id;
    }

    public void setGuestTo(long guestTo) {

        this.guestTo = guestTo;
    }

    public long getGuestTo() {

        return this.guestTo;
    }

    public void setGuestUserId(long guestUserId) {

        this.guestUserId = guestUserId;
    }

    public long getGuestUserId() {

        return this.guestUserId;
    }

    public void setGuestUserPro(int guestUserPro) {

        this.pro = guestUserPro;
    }

    public int getGuestUserPro() {

        return this.pro;
    }

    public Boolean isProMode() {

        return this.pro > 0;

    }

    public void setGuestUserUsername(String guestUserUsername) {

        this.guestUserUsername = guestUserUsername;
    }

    public String getGuestUserUsername() {

        return this.guestUserUsername;
    }

    public void setGuestUserFullname(String guestUserFullname) {

        this.guestUserFullname = guestUserFullname;
    }

    public String getGuestUserFullname() {

        return this.guestUserFullname;
    }

    public void setGuestUserPhotoUrl(String guestUserPhoto) {

        this.guestUserPhoto = guestUserPhoto;
    }

    public String getGuestUserPhotoUrl() {

        return this.guestUserPhoto;
    }

    public void setTimeAgo(String ago) {

        this.timeAgo = ago;
    }

    public String getTimeAgo() {

        return this.timeAgo;
    }

    public void setOnline(Boolean online) {

        this.online = online;
    }

    public Boolean isOnline() {

        return this.online;
    }

    public void setPhotoModerateAt(int photoModerateAt) {

        this.photoModerateAt = photoModerateAt;
    }

    public int getPhotoModerateAt() {

        return this.photoModerateAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.guestTo);
        dest.writeLong(this.guestUserId);
        dest.writeString(this.guestUserUsername);
        dest.writeString(this.guestUserFullname);
        dest.writeString(this.guestUserPhoto);
        dest.writeString(this.timeAgo);
        dest.writeValue(this.online);
        dest.writeInt(this.photoModerateAt);
    }

    protected Guest(Parcel in) {
        this.id = in.readLong();
        this.guestTo = in.readLong();
        this.guestUserId = in.readLong();
        this.guestUserUsername = in.readString();
        this.guestUserFullname = in.readString();
        this.guestUserPhoto = in.readString();
        this.timeAgo = in.readString();
        this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.photoModerateAt = in.readInt();
    }

    public static final Creator<Guest> CREATOR = new Creator<Guest>() {
        @Override
        public Guest createFromParcel(Parcel source) {
            return new Guest(source);
        }

        @Override
        public Guest[] newArray(int size) {
            return new Guest[size];
        }
    };
}
