package com.andrehaueisen.listadejanot.a_application.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * Created by andre on 5/3/2017.
 */
@Module
class ApplicationModule(private val mDatabaseReference: DatabaseReference,
                        private val mFirebaseAuth: FirebaseAuth,
                        private val mUser: User,
                        private val mDeputados: ArrayList<Politician>,
                        private val mSenadores: ArrayList<Politician>,
                        private val mGovernadores: ArrayList<Politician>) {

    @ApplicationScope
    @Provides
    fun provideFirebaseRepository() : FirebaseRepository = FirebaseRepository(mUser, mDatabaseReference)

    @ApplicationScope
    @Provides
    fun provideFirebaseAuthenticator(context: Context) : FirebaseAuthenticator =
            FirebaseAuthenticator(context, mDatabaseReference, mFirebaseAuth)

    @ApplicationScope
    @Provides
    fun provideUser(): User = mUser

    @ApplicationScope
    @Provides
    @Named("deputados_list")
    fun provideDeputados(): ArrayList<Politician> = mDeputados

    @ApplicationScope
    @Provides
    @Named("senadores_list")
    fun provideSenadores(): ArrayList<Politician> = mSenadores

    @ApplicationScope
    @Provides
    @Named("governadores_list")
    fun provideGovernadores(): ArrayList<Politician> = mGovernadores


}