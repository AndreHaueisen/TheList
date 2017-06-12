package com.andrehaueisen.listadejanot.e_search_politician.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorPresenterActivity
import dagger.Component

/**
 * Created by andre on 5/11/2017.
 */
@PoliticianSelectorScope
@Component(modules = arrayOf(PoliticianSelectorModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface PoliticianSelectorComponent {

    fun inject(politicianSelectorPresenterActivity: PoliticianSelectorPresenterActivity)
}