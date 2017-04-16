package com.andrehaueisen.listadejanot.httpDataFetcher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.andrehaueisen.listadejanot.httpDataFetcher.dagger.DaggerHttpDataFetcherComponent
import com.andrehaueisen.listadejanot.httpDataFetcher.dagger.HttpDataFetcherModule
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by andre on 4/15/2017.
 */
class DataServiceActivity : AppCompatActivity(){

    @Inject
    lateinit var mDataService : DataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        DaggerHttpDataFetcherComponent.builder()
                .httpDataFetcherModule(HttpDataFetcherModule())
                .build()
                .injectDataService(this)


        mDataService.getIndividualPoliticianData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<Politician>{
                    override fun onSubscribe(d: Disposable?) {
                        Log.i("DataServiceActivty", "Subscribed")
                    }

                    override fun onNext(it: Politician) {

                    }

                    override fun onComplete() {
                        Log.i("DataServiceActivty", "Completed")
                    }

                    override fun onError(e: Throwable?) {

                    }
                })

    }
}