package com.andrehaueisen.listadejanot.A_application.dagger

import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 5/3/2017.
 */
@Module
class ApplicationModule(val mDatabaseReference: DatabaseReference) {

    @ApplicationScope
    @Provides
    fun providesDatabaseReference(): DatabaseReference{
        return mDatabaseReference
    }
}