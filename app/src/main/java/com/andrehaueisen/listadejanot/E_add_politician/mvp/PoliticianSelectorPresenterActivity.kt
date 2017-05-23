package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.A_application.BaseApplication
import com.andrehaueisen.listadejanot.E_add_politician.dagger.DaggerPoliticianSelectorComponent
import com.andrehaueisen.listadejanot.E_add_politician.dagger.PoliticianSelectorModule
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PoliticianSelectorPresenterActivity : AppCompatActivity(), PoliticianSelectorMvpContract.Presenter{

    private val LOG_TAG = PoliticianSelectorPresenterActivity::class.java.simpleName

    @Inject
    lateinit var mSelectorModel: PoliticianSelectorModel
    @Inject
    lateinit var mSinglePoliticianModel: SinglePoliticianModel

    lateinit private var mView: PoliticianSelectorView
    private var mPolitician: Politician? = null
    lateinit private var mOriginalSearchablePoliticiansList : ArrayList<Politician>
    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mLastSelectedPoliticianName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val senadoresMainList = ArrayList<Politician>()
        val deputadosMainList = ArrayList<Politician>()

        if (intent.hasExtra(Constants.INTENT_SENADORES_MAIN_LIST)){
            senadoresMainList.addAll(intent.getParcelableArrayListExtra(Constants.INTENT_SENADORES_MAIN_LIST))
        }

        if(intent.hasExtra(Constants.INTENT_DEPUTADOS_MAIN_LIST)){
            deputadosMainList.addAll(intent.getParcelableArrayListExtra(Constants.INTENT_DEPUTADOS_MAIN_LIST))
        }

        DaggerPoliticianSelectorComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .politicianSelectorModule(PoliticianSelectorModule(supportLoaderManager, senadoresMainList, deputadosMainList))
                .build()
                .injectModels(this)

        if(savedInstanceState == null) {
            mView = PoliticianSelectorView(this)
            mView.setViews(false)
            mSelectorModel.connectToFirebase()
            subscribeToPoliticianSelectorModel()

        }else{
            mOriginalSearchablePoliticiansList = savedInstanceState.getParcelableArrayList(Constants.BUNDLE_SEARCHABLE_POLITICIANS)
            if(savedInstanceState.containsKey(Constants.BUNDLE_POLITICIAN)) {
                mPolitician = savedInstanceState.getParcelable(Constants.BUNDLE_POLITICIAN)
            }

            mView = PoliticianSelectorView(this)
            mView.setViews(true)
        }
    }

    override fun subscribeToPoliticianSelectorModel() {
        mSelectorModel.loadSearchablePoliticiansList()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<ArrayList<Politician>> {
                    override fun onSubscribe(disposable: Disposable?) {
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
                        mOriginalSearchablePoliticiansList = searchablePoliticiansList
                        mView.notifySearchablePoliticiansNewList()
                    }

                    override fun onError(e: Throwable?) {

                    }

                    override fun onComplete() {

                    }
                })

    }

    override fun subscribeToSinglePoliticianModel(politicianName: String){
        mLastSelectedPoliticianName = politicianName
        mSinglePoliticianModel.loadSinglePoliticianPublisher()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSinglePoliticianObserver)
    }

    private val mSinglePoliticianObserver = object : MaybeObserver<Politician>{
        override fun onError(t: Throwable?) {
            Log.e(LOG_TAG, t.toString())
        }

        override fun onSubscribe(disposable: Disposable?) {
            mCompositeDisposable.add(disposable)
            mSinglePoliticianModel.initiateSinglePoliticianLoad(mLastSelectedPoliticianName)
        }

        override fun onSuccess(politician: Politician?) {
            mPolitician = politician
            mView.notifyPoliticianReady()
        }

        override fun onComplete() {

        }
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
        outState.putParcelableArrayList(Constants.BUNDLE_SEARCHABLE_POLITICIANS, mOriginalSearchablePoliticiansList)
        if(mPolitician != null){
            outState.putParcelable(Constants.BUNDLE_POLITICIAN, mPolitician)
        }
        super.onSaveInstanceState(outState)
    }

    fun getOriginalSearchablePoliticiansList() = mOriginalSearchablePoliticiansList

    fun getSinglePolitician() = mPolitician

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mSelectorModel.onDestroy()
        mSinglePoliticianModel.onDestroy()
        super.onDestroy()
    }
}
