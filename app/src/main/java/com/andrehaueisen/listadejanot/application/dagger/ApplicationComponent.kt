package com.andrehaueisen.listadejanot.application.dagger

import com.andrehaueisen.listadejanot.httpDataFetcher.DataServiceActivity
import com.andrehaueisen.listadejanot.httpDataFetcher.dagger.HttpDataFetcherModule
import dagger.Component

/**
 * Created by andre on 4/15/2017.
 */
@ApplicationScope
@Component(modules = arrayOf(ContextModule::class, HttpDataFetcherModule::class))
interface ApplicationComponent{

    fun injectDataService(dataServiceActivity: DataServiceActivity)
}

