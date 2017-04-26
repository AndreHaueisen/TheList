package com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger

import com.andrehaueisen.listadejanot.B_httpDataFetcher.DataService
import com.andrehaueisen.listadejanot.B_httpDataFetcher.StubAuthenticatorService
import dagger.Component

/**
 * Created by andre on 4/25/2017.
 */
@DataServiceScope
@Component(modules = arrayOf(DataServiceModule::class))
interface DataServiceComponent {

    fun injectSyncAdapter(dataService: DataService)
    fun injectStubAuthenticator(stubAuthenticatorService: StubAuthenticatorService)

}