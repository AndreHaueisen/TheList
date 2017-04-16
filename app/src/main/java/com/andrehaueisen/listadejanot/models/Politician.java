package com.andrehaueisen.listadejanot.models;

/**
 * Created by andre on 4/15/2017.
 */

public class Politician {

    private Post mCargo;
    private String mImageUrl;
    private String mName;
    private String mEmail;
    private byte[] mImage;

    public enum Post{
        DEPUTADO, SENADOR
    }

    public Politician(Post cargo){
        mCargo = cargo;
    }

    public Politician(Post cargo, String imageUrl, String name) {
        mCargo = cargo;
        mImageUrl = imageUrl;
        mName = name;
    }

    public Politician(Post cargo, String imageUrl, String name, String email) {
        mCargo = cargo;
        mImageUrl = imageUrl;
        mName = name;
        mEmail = email;
    }

    public Politician(Post cargo, String imageUrl, String name, String email, byte[] image) {
        mCargo = cargo;
        mImageUrl = imageUrl;
        mName = name;
        mEmail = email;
        mImage = image;
    }

    public Post getCargo() {
        return mCargo;
    }

    public void setCargo(Post cargo) {
        mCargo = cargo;
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
}
