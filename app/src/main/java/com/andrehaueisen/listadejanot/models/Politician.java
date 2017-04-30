package com.andrehaueisen.listadejanot.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by andre on 4/15/2017.
 */

public class Politician implements Parcelable {

    private Post mPost;
    private String mImageUrl;
    private String mName;
    private String mEmail;
    private byte[] mImage;
    private boolean mHasPersonVote;

    public enum Post{
        DEPUTADO, SENADOR
    }

    public Politician(Post post){
        mPost = post;
    }

    public Politician(Post post, String imageUrl, String name) {
        mPost = post;
        mImageUrl = imageUrl;
        mName = name;
    }

    public Politician(Post post, String imageUrl, String name, @Nullable String email) {
        mPost = post;
        mImageUrl = imageUrl;
        mName = name;
        mEmail = email;
    }

    public Politician(Post post, String imageUrl, String name, @Nullable String email, byte[] image) {
        mPost = post;
        mImageUrl = imageUrl;
        mName = name;
        mEmail = email;
        mImage = image;
    }

    public Post getPost() {
        return mPost;
    }

    public void setPost(Post post) {
        mPost = post;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public byte[] getImage() {
        return mImage;
    }

    public void setImage(byte[] image) {
        mImage = image;
    }

    public boolean getHasPersonVote() {
        return mHasPersonVote;
    }

    public void setHasPersonVote(boolean hasPersonVote) {
        mHasPersonVote = hasPersonVote;
    }

    protected Politician(Parcel in) {
        mPost = (Post) in.readValue(Post.class.getClassLoader());
        mImageUrl = in.readString();
        mName = in.readString();
        mEmail = in.readString();
        mHasPersonVote = in.readByte() != 0x00;
        mImage = new byte[in.readInt()];
        in.readByteArray(mImage);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mPost);
        dest.writeString(mImageUrl);
        dest.writeString(mName);
        dest.writeString(mEmail);
        dest.writeByte((byte) (mHasPersonVote ? 0x01 : 0x00));
        dest.writeInt(mImage.length);
        dest.writeByteArray(mImage);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Politician> CREATOR = new Parcelable.Creator<Politician>() {
        @Override
        public Politician createFromParcel(Parcel in) {
            return new Politician(in);
        }

        @Override
        public Politician[] newArray(int size) {
            return new Politician[size];
        }
    };
}