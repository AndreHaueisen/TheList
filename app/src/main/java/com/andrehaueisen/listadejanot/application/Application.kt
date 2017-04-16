package com.andrehaueisen.listadejanot.application

import android.app.Activity
import android.app.Application
import com.andrehaueisen.listadejanot.application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.application.dagger.ContextModule
import com.andrehaueisen.listadejanot.application.dagger.DaggerApplicationComponent

/**
 * Created by andre on 4/15/2017.
 */
class Application : Application(){

    companion object{
        fun get(activity: Activity) : com.andrehaueisen.listadejanot.application.Application{
            return activity.application as com.andrehaueisen.listadejanot.application.Application
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