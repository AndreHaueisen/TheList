package com.andrehaueisen.listadejanot.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import com.google.firebase.database.Exclude;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre on 4/15/2017.
 */

public class Politician implements Parcelable, Comparable<Politician> {

    @Exclude
    private Post post;
    @Exclude
    private String email;
    @Exclude
    private byte[] image;

    private String name;
    private long votesNumber;
    private HashMap<String, Object> condemnedBy = new HashMap<>();
    private boolean isOnMainList;


    public enum Post implements Parcelable {
        DEPUTADO, DEPUTADA, SENADOR, SENADORA, GOVERNADOR, GOVERNADORA;

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

    public Politician(String name, long votesNumber, HashMap<String, Object> condemnedBy) {
        this.name = name;
        this.votesNumber = votesNumber;
        this.condemnedBy = condemnedBy;
    }

    public Politician(Post post, String name, long votesNumber, String email, byte[] image) {
        this.post = post;
        this.name = name;
        this.votesNumber = votesNumber;
        this.email = email;
        this.image = image;
    }

    public Politician(Post post, String name, long votesNumber, ArrayMap<String, Object> condemnedBy, @Nullable String email, byte[] image) {
        this.post = post;
        this.name = name;
        this.votesNumber = votesNumber;
        this.condemnedBy.putAll(condemnedBy);
        this.email = email;
        this.image = image;
    }

    public Politician(Post post, String name, @Nullable String email, byte[] image) {
        this.post = post;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    protected Politician(Parcel in) {
        post = in.readParcelable(Post.class.getClassLoader());
        email = in.readString();
        name = in.readString();
        votesNumber = in.readLong();
        image = in.createByteArray();
        isOnMainList = in.readByte() != 0x00;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
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

    public HashMap<String, Object> getCondemnedBy() {
        return condemnedBy;
    }

    public void setCondemnedBy(HashMap<String, Object> condemnedBy) {
        this.condemnedBy = condemnedBy;
    }

    public boolean getIsOnMainList() {
        return isOnMainList;
    }

    public void setIsOnMainList(boolean onMainList) {
        isOnMainList = onMainList;
    }

    @Override
    public int compareTo(@NonNull Politician politician) {
        return Comparators.NAME.compare(this, politician);
    }

    public static class Comparators {

        public static final Comparator<Politician> NAME = new Comparator<Politician>() {
            @Override
            public int compare(Politician politician1, Politician politician2) {
                return politician1.name.compareTo(politician2.name);
            }
        };

        public static Comparator<Politician> VOTE_NUMBER = new Comparator<Politician>() {
            @Override
            public int compare(Politician politician1, Politician politician2) {
                return (int) (politician2.votesNumber - politician1.votesNumber) ;
            }
        };

    }

    @Override
    public String toString() {
        return name;
    }

    public Map<String, Object> toSimpleMap(Boolean isDataGoingToPreList) {

        Map<String, Object> simplePoliticianMap = new ArrayMap<>();
        simplePoliticianMap.put("name", name);
        simplePoliticianMap.put("votesNumber", votesNumber);
        simplePoliticianMap.put("condemnedBy", condemnedBy);
        if(isDataGoingToPreList) {
            simplePoliticianMap.put("isOnMainList", isOnMainList);
        }

        return simplePoliticianMap;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(post, flags);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeLong(votesNumber);
        dest.writeByteArray(image);
        dest.writeByte((byte) (isOnMainList ? 0x01 : 0x00));
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