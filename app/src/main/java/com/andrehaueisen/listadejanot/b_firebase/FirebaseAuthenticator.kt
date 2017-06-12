package com.andrehaueisen.listadejanot.b_firebase

import android.app.Activity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.LOCATION_UID_MAPPINGS
import com.andrehaueisen.listadejanot.utilities.LOCATION_USERS_DATA
import com.andrehaueisen.listadejanot.utilities.encodeEmail
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

/**
 * Created by andre on 6/8/2017.
 */
class FirebaseAuthenticator(private val mDatabaseReference: DatabaseReference, val mFirebaseAuth: FirebaseAuth) {

    private val REQUEST_CODE = 0

    fun isUserLoggedIn() = mFirebaseAuth.currentUser != null

    fun getUserEmail() = mFirebaseAuth.currentUser?.email

    fun startLoginFlow(activity: Activity) = activity.startActivityForResult(
            AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(listOf(
                            AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                    .setLogo(R.drawable.ic_camara_deputados)
                    .setTheme(R.style.LogInTheme)
                    .build(), REQUEST_CODE)

    fun saveUserIdOnLogin() {
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            val database = mDatabaseReference.child(LOCATION_UID_MAPPINGS).child(currentUser.uid)

            database.setValue(currentUser.email?.encodeEmail())
        }
    }

    fun logout() {
        mFirebaseAuth.signOut()
    }

    fun saveUserData(userEmail: String, user: User) {
        val database = mDatabaseReference.child(LOCATION_USERS_DATA).child(userEmail)
        database.setValue(user)
    }
}