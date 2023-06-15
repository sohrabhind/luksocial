package com.hindbyte.dating.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.hindbyte.dating.constants.Constants;

import org.json.JSONObject;


public class Image extends Application implements Constants, Parcelable {

    private long id;
    private int createAt, accessMode;
    private String timeAgo, date, imgUrl;
    private int itemType;

    private int removeAt = 0, moderateAt = 0;

    private Profile owner;

    public Image() {

    }

    public Image(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setAccessMode(jsonData.getInt("accessMode"));
                this.setItemType(jsonData.getInt("itemType"));
                this.setImgUrl(jsonData.getString("imgUrl"));
                this.setCreateAt(jsonData.getInt("createAt"));
                this.setDate(jsonData.getString("date"));
                this.setTimeAgo(jsonData.getString("timeAgo"));

                if (jsonData.has("removeAt")) {

                    this.setRemoveAt(jsonData.getInt("removeAt"));
                }

                if (jsonData.has("owner")) {

                    JSONObject ownerObj = (JSONObject) jsonData.getJSONObject("owner");

                    this.setOwner(new Profile(ownerObj));
                }
            }

        } catch (Throwable t) {

            Log.e("Gallery Item", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Gallery Item", jsonData.toString());
        }
    }

    public Profile getOwner() {

        if (this.owner == null) {

            this.owner = new Profile();
        }

        return this.owner;
    }

    public void setOwner(Profile owner) {

        this.owner = owner;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAccessMode() {

        return accessMode;
    }

    public void setAccessMode(int accessMode) {

        this.accessMode = accessMode;
    }

    public int getItemType() {

        return itemType;
    }

    public void setItemType(int itemType) {

        this.itemType = itemType;
    }


    public int getCreateAt() {

        return createAt;
    }

    public void setCreateAt(int createAt) {

        this.createAt = createAt;
    }

    public int getRemoveAt() {

        return removeAt;
    }

    public void setRemoveAt(int removeAt) {

        this.removeAt = removeAt;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {

        if (API_DOMAIN.equals("http://10.0.2.2/")) {

            this.imgUrl = imgUrl.replace("http://localhost/","http://10.0.2.2/");

        } else {

            this.imgUrl = imgUrl;
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLink() {

        return WEB_SITE + this.owner.getUsername() + "/gallery/" + this.getId();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.createAt);
        dest.writeInt(this.accessMode);
        dest.writeString(this.timeAgo);
        dest.writeString(this.date);
        dest.writeString(this.imgUrl);
        dest.writeInt(this.itemType);
        dest.writeInt(this.removeAt);
        dest.writeInt(this.moderateAt);
        dest.writeParcelable(this.owner, flags);
    }

    protected Image(Parcel in) {
        this.id = in.readLong();
        this.createAt = in.readInt();
        this.accessMode = in.readInt();
        this.timeAgo = in.readString();
        this.date = in.readString();
        this.itemType = in.readInt();
        this.removeAt = in.readInt();
        this.moderateAt = in.readInt();
        this.owner = (Profile) in.readParcelable(Profile.class.getClassLoader());
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
