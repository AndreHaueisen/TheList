package com.andrehaueisen.listadejanot.a_application.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/3/2017.
 */
@Module
class ApplicationModule(val mDatabaseReference: DatabaseReference, val mFirebaseAuth: FirebaseAuth) {

    @ApplicationScope
    @Provides
    fun provideFirebaseRepository() : FirebaseRepository {
        return FirebaseRepository(mDatabaseReference)
    }

    @ApplicationScope
    @Provides
    fun provideFirebaseAuthenticator(context: Context) : FirebaseAuthenticator {
        return FirebaseAuthenticator(context, mDatabaseReference, mFirebaseAuth)
    }
}