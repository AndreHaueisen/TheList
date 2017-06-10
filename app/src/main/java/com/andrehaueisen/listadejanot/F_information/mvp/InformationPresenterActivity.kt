package com.andrehaueisen.listadejanot.F_information.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.F_information.dagger.DaggerInformationComponent
import com.andrehaueisen.listadejanot.F_information.dagger.InformationModule
import com.andrehaueisen.listadejanot.models.PlayerStatus
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class InformationPresenterActivity : AppCompatActivity(), InformationMvpContract.Presenter {

    @Inject
    lateinit var mModel: InformationModel
    var mView: InformationView? = null
    val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerInformationComponent.builder()
                .informationModule(InformationModule(this))
                .build()
                .injectModel(this)

        mView = InformationView(this)
        mView?.setViews()

        listenToPlayerStatus()
    }

    override fun listenToPlayerStatus(){
        mModel.getPlayerStatusPublisher()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<PlayerStatus>{
                    override fun onSubscribe(disposable: Disposable) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(playerStatus: PlayerStatus) {
                        mView?.newPlayerEventReported(playerStatus)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {

                    }
                })
    }

    override fun playOnBoardingAudio() {
        mModel.playOnBoardingAudio()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mView?.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        mModel.onDestroy()
        mView = null
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}
