package com.andrehaueisen.listadejanot.a_application.dagger

import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 4/15/2017.
 */
@Module
class ContextModule(private val mContext: Context) {

    @ApplicationScope
    @Provides
    fun provideContext() : Context = mContext

}