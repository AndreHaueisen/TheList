package com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationScope
import com.andrehaueisen.listadejanot.A_application.dagger.ContextModule
import com.andrehaueisen.listadejanot.B_httpDataFetcher.DataService
import dagger.Module
import dagger.Provides
import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * Created by andre on 4/15/2017.
 */
@Module(includes = arrayOf(ContextModule::class))
class HttpDataFetcherModule{

    private val BASE_URL = "http://www.congressonacional.leg.br/portal/parlamentar"

    @ApplicationScope
    @Provides
    fun provideJsoup() : Connection{
        return Jsoup.connect(BASE_URL).timeout(10*1000).method(Connection.Method.GET)
    }

    @ApplicationScope
    @Provides
    fun provideDataService(connection: Connection, context: Context) : DataService{
        return DataService(connection, context)
    }
}