package com.andrehaueisen.listadejanot.a_application.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/3/2017.
 */
@Module
class ApplicationModule(private val mDatabaseReference: DatabaseReference, private val mFirebaseAuth: FirebaseAuth, private val mUser: User) {

    @ApplicationScope
    @Provides
    fun provideFirebaseRepository(context: Context) : FirebaseRepository = FirebaseRepository(context, mDatabaseReference)

    @ApplicationScope
    @Provides
    fun provideFirebaseAuthenticator(context: Context) : FirebaseAuthenticator =
            FirebaseAuthenticator(context, mDatabaseReference, mFirebaseAuth)

    @ApplicationScope
    @Provides
    fun provideUser(): User = mUser
}