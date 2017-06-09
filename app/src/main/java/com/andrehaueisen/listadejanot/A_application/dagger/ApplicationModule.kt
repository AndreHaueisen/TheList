package com.andrehaueisen.listadejanot.A_application.dagger

import com.andrehaueisen.listadejanot.B_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
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
    fun provideFirebaseAuthenticator() : FirebaseAuthenticator {
        return FirebaseAuthenticator(mDatabaseReference, mFirebaseAuth)
    }
}