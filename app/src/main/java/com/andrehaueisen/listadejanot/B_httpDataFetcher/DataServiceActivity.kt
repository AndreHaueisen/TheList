package com.andrehaueisen.listadejanot.B_httpDataFetcher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.andrehaueisen.listadejanot.A_application.Application
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by andre on 4/15/2017.
 */
class DataServiceActivity : AppCompatActivity(){

    val LOG = DataServiceActivity::class.java.simpleName

    @Inject
    lateinit var mDataService : DataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val appComponent = Application.get(this).getAppComponent()
        appComponent.injectDataService(this)


        mDataService.savePoliticiansToDatabase()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<Boolean> {
                    override fun onSubscribe(d: Disposable?) {
                        Log.i(LOG, "Subscribed")
                    }

                    override fun onError(e: Throwable) {
                        Log.e(LOG, "Erro aqui ${e} = ${e.printStackTrace()}")
                    }

                    override fun onSuccess(t: Boolean?) {
                        Log.i(LOG, "All politicians where added to database: $t")
                    }
                })

        val dbFile = getDatabasePath("politicians.db")
        Log.i(LOG, "${dbFile.absolutePath}\n${dbFile.path}\n${dbFile.canonicalPath}")


    }
}