package com.andrehaueisen.listadejanot.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Created by andre on 5/3/2017.
 */

public class User{

    @Exclude
    private String email;
    private String age;
    private String state;
    private boolean sex;
    private ArrayList<String> condemnations;

    public User(String email, String age, String state, boolean sex) {
        this.email = email;
        this.age = age;
        this.state = state;
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean getSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public ArrayList<String> getCondemnations() {
        return condemnations;
    }

    public void setCondemnations(ArrayList<String> condemnations) {
        this.condemnations = condemnations;
    }
}
