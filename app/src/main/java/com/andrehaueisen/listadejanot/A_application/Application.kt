package com.andrehaueisen.listadejanot.A_application

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import com.andrehaueisen.listadejanot.A_application.dagger.AccountModule
import com.andrehaueisen.listadejanot.A_application.dagger.ApplicationComponent
import com.andrehaueisen.listadejanot.A_application.dagger.ContextModule
import com.andrehaueisen.listadejanot.A_application.dagger.DaggerApplicationComponent
import com.andrehaueisen.listadejanot.R
import javax.inject.Inject

/**
 * Created by andre on 4/15/2017.
 */
class Application : Application(){

    @Inject
    lateinit var newAccount : Account
    @Inject
    lateinit var accountManager : AccountManager

    companion object{
        fun get(activity: Activity) : com.andrehaueisen.listadejanot.A_application.Application{
            return activity.application as com.andrehaueisen.listadejanot.A_application.Application
        }
    }

    lateinit private var mComponent : ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        mComponent = DaggerApplicationComponent.builder()
                .contextModule(ContextModule(this))
                .accountModule(AccountModule())
                .build()

        mComponent.injectAccount(this)

        setupAccount()
    }

    private fun setupAccount(){
        if (null == accountManager.getPassword(newAccount)) {

            if (!accountManager.addAccountExplicitly(newAccount, null, null)) {
                return
            }
            onAccountCreated(newAccount, applicationContext)
        }
    }

    private fun onAccountCreated(newAccount: Account, context: Context) {

        val authority = context.getString(R.string.provider_authority)

        val settingsBundle = Bundle()
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        /*val request = SyncRequest.Builder()
                .setSyncAdapter(newAccount, authority)
                .setManual(true)
                .setExpedited(true)
                .setExtras(Bundle())
                .build()*/

        ContentResolver.requestSync(newAccount, authority, settingsBundle)
        //ContentResolver.setSyncAutomatically(newAccount, authority, true)

    }

    fun getAppComponent() : ApplicationComponent{
        return mComponent
    }
}