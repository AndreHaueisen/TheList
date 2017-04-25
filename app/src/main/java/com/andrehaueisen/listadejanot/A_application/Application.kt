package com.andrehaueisen.listadejanot.A_application

import android.app.Activity
import android.app.Application
import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.A_application.dagger.ContextModule
import com.andrehaueisen.listadejanot.A_application.dagger.DaggerApplicationComponent

/**
 * Created by andre on 4/15/2017.
 */
class Application : Application(){

    companion object{
        fun get(activity: Activity) : com.andrehaueisen.listadejanot.A_application.Application{
            return activity.application as com.andrehaueisen.listadejanot.A_application.Application
        }
    }

    lateinit private var mComponent : ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        mComponent = DaggerApplicationComponent.builder()
                .contextModule(ContextModule(this))
                .build()

    }

    fun getAppComponent() : ApplicationComponent{
        return mComponent
    }
}