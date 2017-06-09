package com.andrehaueisen.listadejanot.A_application.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.B_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.B_firebase.FirebaseRepository
import dagger.Component

/**
 * Created by andre on 4/15/2017.
 */
@ApplicationScope
@Component(modules = arrayOf(ContextModule::class, ApplicationModule::class))
interface ApplicationComponent{

    fun loadFirebaseRepository() : FirebaseRepository

    fun loadFirebaseAuthenticator() : FirebaseAuthenticator

    fun loadContext() : Context
}

