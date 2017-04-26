package com.andrehaueisen.listadejanot.B_httpDataFetcher

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger.DaggerDataServiceComponent
import com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger.DataServiceModule

import javax.inject.Inject

/**
 * Created by andre on 4/15/2017.
 */
class DataService : Service(){

    @Inject
    lateinit var mDataSyncAdapter: DataSyncAdapter

    override fun onCreate() {
        DaggerDataServiceComponent.builder()
                .dataServiceModule(DataServiceModule(applicationContext))
                .build()
                .injectSyncAdapter(this)

    }

    override fun onBind(intent: Intent?): IBinder {
        return mDataSyncAdapter.syncAdapterBinder
    }
}