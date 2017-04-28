package com.andrehaueisen.listadejanot.C_main_list.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.C_main_list.dagger.DaggerMainListComponent
import com.andrehaueisen.listadejanot.C_main_list.dagger.MainListModule
import com.andrehaueisen.listadejanot.models.Politician
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class MainListPresenterActivity : AppCompatActivity(), MainListMvpContract.Presenter {

    @Inject
    lateinit var mModel: MainListModel

    lateinit private var mView: MainListView
    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMainListComponent.builder()
                .mainListModule(MainListModule(this, supportLoaderManager))
                .build()
                .injectModel(this)

        mView = MainListView(this)
        subscribeToModel()

    }

    override fun subscribeToModel() {
        mModel.loadDeputadosData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<Politician> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(senador: Politician) {
                        mView.notifyDeputadoAddition(senador)
                    }

                    override fun onError(t: Throwable?) {

                    }

                    override fun onComplete() {

                    }
                })

        mModel.loadSenadoresData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<Politician> {
                    override fun onError(e: Throwable?) {

                    }

                    override fun onComplete() {

                    }

                    override fun onNext(senador: Politician) {
                        mView.notifySenadorAddition(senador)
                    }

                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }
                })
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}
