package com.andrehaueisen.listadejanot.f_politician_selector.dagger

import com.andrehaueisen.listadejanot.a_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorPresenterActivity
import dagger.Component

/**
 * Created by andre on 5/11/2017.
 */
@PoliticianSelectorScope
@Component(modules = arrayOf(PoliticianSelectorModule::class, ImageFetcherModule::class), dependencies = arrayOf(ApplicationComponent::class))
interface PoliticianSelectorComponent {

    fun inject(politicianSelectorPresenterActivity: PoliticianSelectorPresenterActivity)
}