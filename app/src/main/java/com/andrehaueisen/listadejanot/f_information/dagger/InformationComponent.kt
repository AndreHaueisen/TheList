package com.andrehaueisen.listadejanot.f_information.dagger

import com.andrehaueisen.listadejanot.f_information.mvp.InformationPresenterActivity
import dagger.Component

/**
 * Created by andre on 6/5/2017.
 */
@InformationScope
@Component(modules = arrayOf(InformationModule::class))
interface InformationComponent {

    fun injectModel(presenterActivity: InformationPresenterActivity)
}