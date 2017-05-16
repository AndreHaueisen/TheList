package com.andrehaueisen.listadejanot.E_add_politician.dagger

import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.E_add_politician.mvp.PoliticianSelectorPresenterActivity
import dagger.Component

/**
 * Created by andre on 5/11/2017.
 */
@PoliticianSelectorScope
@Component(modules = arrayOf(PoliticianSelectorModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface PoliticianSelectorComponent {

    fun injectModels(politicianSelectorPresenterActivity: PoliticianSelectorPresenterActivity)
}