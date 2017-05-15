package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.A_application.BaseApplication
import com.andrehaueisen.listadejanot.E_add_politician.dagger.AddPoliticianModule
import com.andrehaueisen.listadejanot.E_add_politician.dagger.DaggerAddPoliticianComponent
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PoliticianSelectorPresenterActivity : AppCompatActivity(), PoliticianSelectorMvpContract.Presenter{

    @Inject
    lateinit var mSelectorModel: PoliticianSelectorModel
    lateinit private var mView: PoliticianSelectorView

    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.e_activity_politician_selector)

        val senadoresMainList = ArrayList<Politician>()
        val deputadosMainList = ArrayList<Politician>()

        if (intent.hasExtra(Constants.INTENT_SENADORES_MAIN_LIST)){
            senadoresMainList.addAll(intent.getParcelableArrayListExtra(Constants.INTENT_SENADORES_MAIN_LIST))
        }

        if(intent.hasExtra(Constants.INTENT_DEPUTADOS_MAIN_LIST)){
            deputadosMainList.addAll(intent.getParcelableArrayListExtra(Constants.INTENT_DEPUTADOS_MAIN_LIST))
        }

        DaggerAddPoliticianComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .addPoliticianModule(AddPoliticianModule(supportLoaderManager, senadoresMainList, deputadosMainList))
                .build()
                .injectModel(this)

        if(savedInstanceState == null) {
            mView = PoliticianSelectorView(this)
            mView.setViews(false)
            mSelectorModel.connectToFirebase()
            subscribeToModel()
        }else{
            mView = PoliticianSelectorView(this, savedInstanceState)
            mView.setViews(true)
        }
    }

    override fun subscribeToModel() {
        mSelectorModel.loadSearchablePoliticiansList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onNext(deputadosPreList: ArrayList<Politician>) {
                        mView.notifySearchablePoliticiansNewList(deputadosPreList)
                    }

                    override fun onError(e: Throwable?) {

                    }

                    override fun onComplete() {

                    }
                })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mView.onCreateOptionsMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mView.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putAll(mView.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mSelectorModel.onDestroy()
        super.onDestroy()
    }
}
