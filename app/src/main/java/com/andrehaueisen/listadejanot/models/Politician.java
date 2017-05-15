package com.andrehaueisen.listadejanot.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre on 4/15/2017.
 */

public class Politician implements Parcelable {

    @Exclude
    private Post post;
    @Exclude
    private String imageUrl;
    @Exclude
    private String email;
    private String name;
    private long votesNumber;
    private ArrayList<String> condemnedBy = new ArrayList<>();
    @Exclude
    private byte[] image;

    public enum Post implements Parcelable {
        DEPUTADO, SENADOR;

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Post> CREATOR = new Creator<Post>() {
            @Override
            public Post createFromParcel(Parcel in) {
                return Post.values()[in.readInt()];
            }

            @Override
            public Post[] newArray(int size) {
                return new Post[size];
            }
        };
    }

    public Politician() {

    }

    public Politician(Post post) {
        this.post = post;
    }

    public Politician(String name, long votesNumber) {
        this.name = name;
        this.votesNumber = votesNumber;
    }

    public Politician(Post post, String name, String email) {
        this.post = post;
        this.name = name;
        this.email = email;
    }

    public Politician(String name, long votesNumber, ArrayList<String> condemnedBy) {
        this.name = name;
        this.votesNumber = votesNumber;
        this.condemnedBy = condemnedBy;
    }

    public Politician(Post post, String name, long votesNumber, ArrayList<String> condemnedBy, @Nullable String email, byte[] image) {
        this.post = post;
        this.name = name;
        this.votesNumber = votesNumber;
        this.condemnedBy.addAll(condemnedBy);
        this.email = email;
        this.image = image;
    }

    public Politician(Post post, String imageUrl, String name, @Nullable String email, byte[] image) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    protected Politician(Parcel in) {
        post = in.readParcelable(Post.class.getClassLoader());
        imageUrl = in.readString();
        email = in.readString();
        name = in.readString();
        votesNumber = in.readLong();
        condemnedBy = in.createStringArrayList();
        image = in.createByteArray();
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getVotesNumber() {
        return votesNumber;
    }

    public void setVotesNumber(long votesNumber) {
        this.votesNumber = votesNumber;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public ArrayList<String> getCondemnedBy() {
        return condemnedBy;
    }

    public void setCondemnedBy(ArrayList<String> condemnedBy) {
        this.condemnedBy = condemnedBy;
    }

    @Override
    public String toString() {
        return name;
    }

    public Map<String, Object> toSimpleMap() {

        Map<String, Object> simplePoliticianMap = new HashMap<>();
        simplePoliticianMap.put("name", name);
        simplePoliticianMap.put("votesNumber", votesNumber);

        return simplePoliticianMap;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(post, flags);
        dest.writeString(imageUrl);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeLong(votesNumber);
        dest.writeStringList(condemnedBy);
        dest.writeByteArray(image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Politician> CREATOR = new Creator<Politician>() {
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