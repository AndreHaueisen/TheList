package com.andrehaueisen.listadejanot.F_information.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.F_information.mvp.InformationModel
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 6/5/2017.
 */
@Module
class InformationModule(val context: Context) {

    @InformationScope
    @Provides
    fun provideInformationModel() = InformationModel(context)
}