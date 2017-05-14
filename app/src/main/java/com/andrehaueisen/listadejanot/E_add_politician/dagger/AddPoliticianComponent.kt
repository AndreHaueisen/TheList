package com.andrehaueisen.listadejanot.E_add_politician.dagger

import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.E_add_politician.mvp.PoliticianSelectorPresenterActivity
import dagger.Component

/**
 * Created by andre on 5/11/2017.
 */
@AddPoliticianScope
@Component(modules = arrayOf(AddPoliticianModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface AddPoliticianComponent {

    fun injectModel(politicianSelectorPresenterActivity: PoliticianSelectorPresenterActivity)
}