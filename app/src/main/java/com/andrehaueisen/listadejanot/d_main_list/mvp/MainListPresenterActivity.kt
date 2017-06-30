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


class MainListPresenterActivity : AppCompatActivity(), MainListMvpContract.Presenter {

    @Inject
    lateinit var mModel: MainListModel
    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    private var mView: MainListView? = null
    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMainListComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .mainListModule(MainListModule(supportLoaderManager))
                .build()
                .inject(this)

        if(savedInstanceState == null) {
            mView = MainListView(this)
            mView?.setViews()
            mModel.connectToFirebase()
            subscribeToModel()
        }else{
            mView = MainListView(this, savedInstanceState)
            mView?.setViews()
        }
    }

    override fun onRestart() {
        mModel.connectToFirebase()
        super.onRestart()
    }

    private val mDeputadosMainListObserver = object : Observer<ArrayList<Politician>>{
        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onNext(deputados: ArrayList<Politician>) {
            mView?.notifyDeputadosNewList(deputados)
        }

        override fun onError(t: Throwable) {

        }

        override fun onComplete() {

        }
    }

    private val mSenadoresMainListObserver = object : Observer<ArrayList<Politician>>{
        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onNext(senadores: ArrayList<Politician>) {
            mView?.notifySenadoresNewList(senadores)
        }

        override fun onError(e: Throwable) {

        }

        override fun onComplete() {

        }
    }

    override fun subscribeToModel() {
        mModel.loadDeputadosMainList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mDeputadosMainListObserver)

        mModel.loadSenadoresMainList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSenadoresMainListObserver)
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

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mModel.onDestroy()
        mView = null
        super.onDestroy()
    }
}
