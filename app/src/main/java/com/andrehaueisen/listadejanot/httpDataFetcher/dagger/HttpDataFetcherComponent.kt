package com.andrehaueisen.listadejanot.httpDataFetcher.dagger

import com.andrehaueisen.listadejanot.httpDataFetcher.DataServiceActivity
import dagger.Component

/**
 * Created by andre on 4/15/2017.
 */
@DataFetcherScope
@Component(modules = arrayOf(HttpDataFetcherModule::class))
interface HttpDataFetcherComponent{

    fun injectDataService(dataServiceActivity: DataServiceActivity)

}