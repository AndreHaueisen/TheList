package com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger

import android.content.Context
import com.andrehaueisen.listadejanot.B_httpDataFetcher.DataSyncAdapter
import com.andrehaueisen.listadejanot.B_httpDataFetcher.StubAuthenticator
import dagger.Module
import dagger.Provides
import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * Created by andre on 4/15/2017.
 */
@Module
class DataServiceModule(val applicationContext: Context) {

    private val BASE_URL = "http://www.congressonacional.leg.br/portal/parlamentar"

    @DataServiceScope
    @Provides
    fun provideApplicationContext() : Context{
        return applicationContext
    }

    @DataServiceScope
    @Provides
    fun provideJsoup() : Connection{
        return Jsoup.connect(BASE_URL).timeout(10*1000).method(Connection.Method.GET)
    }

    @DataServiceScope
    @Provides
    fun provideSyncAdapter(connection: Connection, context: Context) : DataSyncAdapter {
        return DataSyncAdapter(connection, context)
    }

    @DataServiceScope
    @Provides
    fun provideStubAuthenticator(context: Context) : StubAuthenticator {
        return StubAuthenticator(context)
    }
}