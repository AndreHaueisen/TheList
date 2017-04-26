package com.andrehaueisen.listadejanot.B_httpDataFetcher

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger.DaggerDataServiceComponent
import com.andrehaueisen.listadejanot.B_httpDataFetcher.dagger.DataServiceModule
import javax.inject.Inject

/**
 * Created by andre on 4/25/2017.
 */
class StubAuthenticatorService : Service() {

    @Inject
    lateinit var mAuthenticator: StubAuthenticator

    override fun onCreate() {
        Log.d("PopMoviesAutheService", "onCreate - SunshineSyncService")
        // Create a new authenticator object
        DaggerDataServiceComponent.builder()
                .dataServiceModule(DataServiceModule(applicationContext))
                .build()
                .injectStubAuthenticator(this)
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    override fun onBind(intent: Intent): IBinder {
        return mAuthenticator.iBinder
    }
}