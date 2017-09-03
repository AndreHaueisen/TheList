package com.andrehaueisen.listadejanot.d_main_list.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.d_main_list.dagger.DaggerMainListComponent
import com.andrehaueisen.listadejanot.d_main_list.dagger.MainListModule
import com.andrehaueisen.listadejanot.g_login.LoginActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.startNewActivity
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class MainListPresenterActivity : AppCompatActivity(),
        MainListMvpContract.Presenter,
        MainListDeputadosView.DeputadosDataFetcher,
        MainListGovernadoresView.GovernadoresDataFetcher,
        MainListSenadoresView.SenadoresDataFetcher{

    @Inject
    lateinit var mModel: MainListModel
    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    private var mView: MainListView? = null
    private val mCompositeDisposable = CompositeDisposable()
    var mIsScreenRotation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMainListComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .mainListModule(MainListModule(supportLoaderManager))
                .build()
                .inject(this)

        if(savedInstanceState == null) {
            mIsScreenRotation = false
            mView = MainListView(this)
            mView?.setViews()
        }else{
            mIsScreenRotation = true
            mView = MainListView(this, savedInstanceState)
            mView?.setViews()
        }
    }

    override fun subscribeToDeputados() {
        mModel.connectToDeputadosOnFirebase()
        mModel.loadDeputadosMainList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>>{
                    override fun onSubscribe(disposable: Disposable) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(deputados: ArrayList<Politician>) {
                        mView?.notifyDeputadosNewList(deputados)
                    }

                    override fun onError(t: Throwable) = Unit

                    override fun onComplete() = Unit
                })
    }

    override fun subscribeToGovernadores() {
        mModel.connectToGovernadoresOnFirebase()
        mModel.loadGovernadoresMainList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<ArrayList<Politician>>{
                    override fun onSubscribe(disposable: Disposable) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(governadores: ArrayList<Politician>) {
                        mView?.notifyGovernadoresNewList(governadores)
                    }

                    override fun onError(e: Throwable) = Unit

                    override fun onComplete() = Unit
                })
    }

    override fun subscribeToSenadores() {
        mModel.connectToSenadoresOnFirebase()
        mModel.loadSenadoresMainList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>>{
                    override fun onSubscribe(disposable: Disposable) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(senadores: ArrayList<Politician>) {
                        mView?.notifySenadoresNewList(senadores)
                    }

                    override fun onError(e: Throwable) = Unit

                    override fun onComplete() = Unit
                })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mView?.onCreateOptionsMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mView?.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putAll(mView?.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    override fun logUserOut(){
        mFirebaseAuthenticator.logout()
        startNewActivity(LoginActivity::class.java)
        finish()
    }

    override fun onStop() {
        mIsScreenRotation = false
        super.onStop()
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mModel.onDestroy()
        super.onDestroy()
    }
}
