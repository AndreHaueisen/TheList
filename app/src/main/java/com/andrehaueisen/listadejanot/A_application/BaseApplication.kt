package com.andrehaueisen.listadejanot.A_application

import android.app.Activity
import android.app.Application
import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationModule
import com.andrehaueisen.listadejanot.A_application.dagger.DaggerApplicationComponent
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by andre on 4/15/2017.
 */
class BaseApplication : Application(){

    companion object{
        fun get(activity: Activity) : BaseApplication {
            return activity.application as BaseApplication
        }

    }

    lateinit private var mComponent : ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(FirebaseDatabase.getInstance().reference))
                .build()

    }

    fun getAppComponent() : ApplicationComponent{
        return mComponent
    }
}