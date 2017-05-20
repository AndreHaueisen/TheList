package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.A_application.BaseApplication
import com.andrehaueisen.listadejanot.E_add_politician.dagger.DaggerPoliticianSelectorComponent
import com.andrehaueisen.listadejanot.E_add_politician.dagger.PoliticianSelectorModule
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.Constants
import io.reactivex.Observer
import io.reactivex.SingleObserver
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
    lateinit var mIndividualPoliticianModel : IndividualPoliticianSelectorModel

    lateinit private var mView: PoliticianSelectorView

    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mOriginalSearchablePoliticiansList: ArrayList<Politician>
    private var mPairNameImage : Pair<String, ByteArray>? = null

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

        DaggerPoliticianSelectorComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .politicianSelectorModule(PoliticianSelectorModule(supportLoaderManager, senadoresMainList, deputadosMainList))
                .build()
                .injectModels(this)

        if(savedInstanceState == null) {
            mView = PoliticianSelectorView(this)
            mView.setViews(false)
            mSelectorModel.connectToFirebase()
            subscribeToModel()

        }else{
            mOriginalSearchablePoliticiansList = savedInstanceState.getParcelableArrayList(Constants.BUNDLE_SEARCHABLE_POLITICIANS)
            if(savedInstanceState.containsKey(Constants.BUNDLE_PAIR_NAME_IMAGE)) {
                mPairNameImage = savedInstanceState.getSerializable(Constants.BUNDLE_PAIR_NAME_IMAGE) as Pair < String, ByteArray>
            }

            mView = PoliticianSelectorView(this)
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

                    override fun onNext(searchablePoliticiansList: ArrayList<Politician>) {
                        mOriginalSearchablePoliticiansList = searchablePoliticiansList
                        mView.notifySearchablePoliticiansNewList()
                    }

                    override fun onError(e: Throwable?) {

                    }

                    override fun onComplete() {

                    }
                })

    }

    override fun subscribeToIndividualModel(politicianName: String){
        mIndividualPoliticianModel.loadSinglePoliticianObservable()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Pair<String, ByteArray>> {
                    override fun onSuccess(pairPoliticianNameImage: Pair<String, ByteArray>) {
                        mPairNameImage = pairPoliticianNameImage
                        mView.notifyPoliticianImageReady()

                    }

                    override fun onSubscribe(disposable: Disposable?) {
                        mIndividualPoliticianModel.initiateSinglePoliticianLoad(politicianName)
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                    }
                })
                /*.subscribe(object : Observer<Pair<String, ByteArray>>{
                    override fun onSubscribe(disposable: Disposable?) {
                        mIndividualPoliticianModel.initiateSinglePoliticianLoad(politicianName)
                        mCompositeDisposable.add(disposable)
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {
                        Log.e(LOG_TAG, e.toString())
                    }

                    override fun onNext(pairPoliticianNameImage: Pair<String, ByteArray>?) {
                        mPairNameImage = pairPoliticianNameImage
                        mView.notifyPoliticianImageReady()
                    }
                })*/
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
        if(mPairNameImage != null){
            outState.putSerializable(Constants.BUNDLE_PAIR_NAME_IMAGE, mPairNameImage)
        }
        super.onSaveInstanceState(outState)
    }

    fun getOriginalSearchablePoliticiansList() = mOriginalSearchablePoliticiansList

    fun getNameImagePair() = mPairNameImage

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mSelectorModel.onDestroy()
        mIndividualPoliticianModel.onDestroy()
        super.onDestroy()
    }
}
