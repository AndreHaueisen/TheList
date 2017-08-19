package com.andrehaueisen.listadejanot.a_application

import android.app.Activity
import android.app.Application
import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationModule
import com.andrehaueisen.listadejanot.a_application.dagger.ContextModule
import com.andrehaueisen.listadejanot.a_application.dagger.DaggerApplicationComponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by andre on 4/15/2017.
 */
class BaseApplication : Application(){

    companion object{
        fun get(activity: Activity) : BaseApplication = activity.application as BaseApplication

    }

    lateinit private var mComponent : ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        //TODO search about firebase persistence
        val firebaseInstance = FirebaseDatabase.getInstance()
        //firebaseInstance.setPersistenceEnabled(true)

        val firebaseReference = firebaseInstance.reference
        //firebaseReference.keepSynced(true)

        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(firebaseReference, FirebaseAuth.getInstance()))
                .contextModule(ContextModule(this))
                .build()

    }

    fun getAppComponent() : ApplicationComponent = mComponent
}