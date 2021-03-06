package com.andrehaueisen.listadejanot.a_application

import android.app.Activity
import android.app.Application
import com.andrehaueisen.listadejanot.BuildConfig
import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationModule
import com.andrehaueisen.listadejanot.a_application.dagger.ContextModule
import com.andrehaueisen.listadejanot.a_application.dagger.DaggerApplicationComponent
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.google.android.gms.ads.MobileAds
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

        MobileAds.initialize(this, BuildConfig.ADD_MOB_ID)

        val firebaseInstance = FirebaseDatabase.getInstance()
        //firebaseInstance.setLogLevel(Logger.Level.DEBUG)
        firebaseInstance.setPersistenceEnabled(true)

        val firebaseReference = firebaseInstance.reference
        firebaseReference.keepSynced(true)

        val user = User()

        val senadores = ArrayList<Politician>()
        val deputados = ArrayList<Politician>()
        val governadores = ArrayList<Politician>()
        val presidentes = ArrayList<Politician>()
        val mediaHighlights = ArrayList<String>()

        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(
                        firebaseReference,
                        FirebaseAuth.getInstance(),
                        user,
                        deputados,
                        senadores,
                        governadores,
                        presidentes,
                        mediaHighlights))
                .contextModule(ContextModule(this))
                .build()

    }

    fun getAppComponent() : ApplicationComponent = mComponent
}