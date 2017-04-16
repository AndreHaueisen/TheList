package com.andrehaueisen.listadejanot.httpDataFetcher.dagger

import com.andrehaueisen.listadejanot.httpDataFetcher.DataService
import dagger.Module
import dagger.Provides
import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * Created by andre on 4/15/2017.
 */
@Module
class HttpDataFetcherModule{

    private val BASE_URL = "http://www.congressonacional.leg.br/portal/parlamentar"

    @DataFetcherScope
    @Provides
    fun provideJsoup() : Connection{
        return Jsoup.connect(BASE_URL).method(Connection.Method.GET)
    }

    @DataFetcherScope
    @Provides
    fun provideDataService(connection: Connection) : DataService{
        return DataService(connection)
    }
}