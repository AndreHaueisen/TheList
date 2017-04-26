package com.andrehaueisen.listadejanot.A_application.dagger

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.andrehaueisen.listadejanot.R
import dagger.Module
import dagger.Provides

/**
 * Created by andre on 4/25/2017.
 */
@Module(includes = arrayOf(ContextModule::class))
class AccountModule {

    @ApplicationScope
    @Provides
    fun provideAccountManager(context: Context) : AccountManager{
        return context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    }

    @ApplicationScope
    @Provides
    fun provideAccount(context: Context) : Account{
        return Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type))
    }


}