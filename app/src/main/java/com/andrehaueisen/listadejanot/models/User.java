package com.andrehaueisen.listadejanot.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre on 6/20/2017.
 */

public class User implements Parcelable {

    private HashMap<String, Object> condemnations = new HashMap<>();

    public User(){}

    public User(HashMap<String, Object> condemnations) {
        this.condemnations = condemnations;
    }

    public HashMap<String, Object> getCondemnations() {
        return condemnations;
    }

    public void setCondemnations(HashMap<String, Object> condemnations) {
        this.condemnations = condemnations;
    }

    public Map<String, Object> toSimpleMap() {

        Map<String, Object> simpleUserMap = new HashMap<>();
        simpleUserMap.put("condemnations", condemnations);

        return simpleUserMap;
    }


    protected User(Parcel in) {
        condemnations = (HashMap) in.readValue(HashMap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(condemnations);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
