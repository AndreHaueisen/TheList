package com.andrehaueisen.listadejanot.b_firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.utilities.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Created by andre on 6/8/2017.
 */
class FirebaseAuthenticator(private val mContext: Context, private val mDatabaseReference: DatabaseReference, val mFirebaseAuth: FirebaseAuth) {

    private val LOG_TAG = FirebaseAuthenticator::class.java.simpleName
    private val REQUEST_CODE = 0

    fun isUserLoggedIn() = mFirebaseAuth.currentUser != null

    fun getUserEmail() = mFirebaseAuth.currentUser?.email

    fun startLoginFlow(activity: Activity) = activity.startActivityForResult(
            AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(listOf(
                            AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                    .setLogo(R.drawable.ic_janot_big)
                    .setTheme(R.style.LogInTheme)
                    .build(), REQUEST_CODE)

    fun saveUserIdOnLogin() {
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            val database = mDatabaseReference.child(LOCATION_UID_MAPPINGS).child(currentUser.uid)
            val encodedEmail = currentUser.email?.encodeEmail()

            database.setValue(encodedEmail)
            sendRegistrationToServer(encodedEmail)
        }
    }

    private fun sendRegistrationToServer(email: String?) {
        val token = mContext.pullStringFromSharedPreferences(SHARED_MESSAGE_TOKEN)

        if(email != null) {
            mDatabaseReference.child(LOCATION_MESSAGE_TOKENS).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.e(LOG_TAG, "Token not sent to server! Device won`t receive notifications")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    val tokensMap: MutableMap<String, Any>

                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        val genericTypeIndicator = object : GenericTypeIndicator<MutableMap<String, Any>>() {}
                        tokensMap = dataSnapshot.getValue(genericTypeIndicator) ?: mutableMapOf()
                    } else {
                        tokensMap = mutableMapOf()
                    }

                    tokensMap.put(email, token)
                    mDatabaseReference.child(LOCATION_MESSAGE_TOKENS).updateChildren(tokensMap)
                }
            })
        }
    }

    fun logout() {
        mFirebaseAuth.signOut()
    }

}