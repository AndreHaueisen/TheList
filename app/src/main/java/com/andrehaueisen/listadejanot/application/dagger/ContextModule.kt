package com.andrehaueisen.listadejanot.application.dagger

import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 4/15/2017.
 */
@Module
class ContextModule(val mContext: Context) {


    @Provides
    @ApplicationScope
    fun provideContext() : Context{
        return mContext
    }

}