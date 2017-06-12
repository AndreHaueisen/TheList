package com.andrehaueisen.listadejanot.a_application.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
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

