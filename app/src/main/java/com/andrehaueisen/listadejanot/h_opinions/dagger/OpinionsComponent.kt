package com.andrehaueisen.listadejanot.h_opinions.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.h_opinions.OpinionsActivity
import dagger.Component

/**
 * Created by andre on 8/23/2017.
 */
@OpinionsScope
@Component(modules = arrayOf(OpinionsModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface OpinionsComponent{

    fun injectOpinions(opinionsActivity: OpinionsActivity)

}