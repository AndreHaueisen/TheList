package com.andrehaueisen.listadejanot.e_search_politician.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.e_search_politician.dagger.DaggerPoliticianSelectorComponent
import com.andrehaueisen.listadejanot.e_search_politician.dagger.PoliticianSelectorModule
import com.andrehaueisen.listadejanot.g_login.LoginActivity
import com.andrehaueisen.listadejanot.h_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.utilities.*
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.e_activity_politician_selector.*
import javax.inject.Inject

class PoliticianSelectorPresenterActivity : AppCompatActivity(), PoliticianSelectorMvpContract.Presenter {

    private val LOG_TAG = PoliticianSelectorPresenterActivity::class.java.simpleName

    @Inject
    lateinit var mSelectorModel: PoliticianSelectorModel
    @Inject
    lateinit var mSinglePoliticianModel: SinglePoliticianModel
    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    private var mView: PoliticianSelectorView? = null
    private var mPolitician: Politician? = null
    private val mCompositeDisposable = CompositeDisposable()

    lateinit private var mOriginalSearchablePoliticiansList: ArrayList<Politician>
    lateinit private var mLastSelectedPoliticianName: String

    private var mNameFromNotification: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val senadoresMainList = ArrayList<Politician>()
        val deputadosMainList = ArrayList<Politician>()

        if (intent.hasExtra(INTENT_SENADORES_MAIN_LIST)) {
            senadoresMainList.addAll(intent.getParcelableArrayListExtra(INTENT_SENADORES_MAIN_LIST))
        }

        if (intent.hasExtra(INTENT_DEPUTADOS_MAIN_LIST)) {
            deputadosMainList.addAll(intent.getParcelableArrayListExtra(INTENT_DEPUTADOS_MAIN_LIST))
        }

        if (intent.hasExtra(INTENT_POLITICIAN_NAME)) {
            mNameFromNotification = intent.getStringExtra(INTENT_POLITICIAN_NAME)
        }

        DaggerPoliticianSelectorComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .politicianSelectorModule(PoliticianSelectorModule(supportLoaderManager))
                .build()
                .inject(this)

        if (savedInstanceState == null) {
            mView = PoliticianSelectorView(this)
            mView!!.setViews(isSavedState = false)
            mSelectorModel.connectToFirebase()
            subscribeToPoliticianSelectorModel()

        } else {
            mOriginalSearchablePoliticiansList = savedInstanceState.getParcelableArrayList(BUNDLE_SEARCHABLE_POLITICIANS)
            mSelectorModel.setSearchablePoliticiansList(mOriginalSearchablePoliticiansList)
            if (savedInstanceState.containsKey(BUNDLE_POLITICIAN)) {
                mPolitician = savedInstanceState.getParcelable(BUNDLE_POLITICIAN)
            }

            mView = PoliticianSelectorView(this)
            mView!!.setViews(isSavedState = true)
        }
    }

    override fun onNewIntent(intent: Intent?) {

        intent?.let {
            if (intent.hasExtra(INTENT_POLITICIAN_NAME)) {
                mNameFromNotification = intent.getStringExtra(INTENT_POLITICIAN_NAME)
                mSelectorModel.connectToFirebase()
                subscribeToPoliticianSelectorModel()
            }
        }

        super.onNewIntent(intent)
    }

    override fun subscribeToPoliticianSelectorModel() {
        mSelectorModel.loadSearchablePoliticiansList()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mMaybeListObserver)
    }

    private val mMaybeListObserver = object: MaybeObserver<ArrayList<Politician>>{

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
        }

        override fun onSuccess(searchablePoliticiansList: ArrayList<Politician>) {
            mOriginalSearchablePoliticiansList = searchablePoliticiansList
            mView?.notifySearchablePoliticiansNewList()

            if (mNameFromNotification != null) {
                mView?.performOnCompleteTextViewAutoSearch(mNameFromNotification as String)
            }else{
                ExpectAnim()
                        .expect(select_politician_toolbar)
                        .toBe(Expectations.centerInParent(false, true))
                        .toAnimation().setDuration(DEFAULT_ANIMATIONS_DURATION)
                        .start()
            }
        }

        override fun onComplete() {

        }

        override fun onError(error: Throwable) {
            Log.e(LOG_TAG, error.message)
        }
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
        override fun onError(t: Throwable) {
            Log.e(LOG_TAG, t.toString())
        }

        override fun onSubscribe(disposable: Disposable) {
            mCompositeDisposable.add(disposable)
            mSinglePoliticianModel.initiateSinglePoliticianLoad(mLastSelectedPoliticianName)
        }

        override fun onSuccess(politician: Politician) {
            mPolitician = politician
            mView?.notifyPoliticianReady()
        }

        override fun onComplete() {

        }
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
        outState.putParcelableArrayList(BUNDLE_SEARCHABLE_POLITICIANS, mOriginalSearchablePoliticiansList)
        if (mPolitician != null) {
            outState.putParcelable(BUNDLE_POLITICIAN, mPolitician)
        }
        super.onSaveInstanceState(outState)
    }

    override fun updatePoliticianVote(politician: Politician, view: PoliticianSelectorMvpContract.View) {
        if (mFirebaseAuthenticator.isUserLoggedIn()) {
            mSinglePoliticianModel.updatePoliticianVote(politician, view)
        } else {
            startNewActivity(LoginActivity::class.java)
            finish()
        }
    }

    override fun showUserVoteListIfLogged() {
        if (mFirebaseAuthenticator.isUserLoggedIn()) {
            startNewActivity(UserVoteListPresenterActivity::class.java)
        } else {
            startNewActivity(LoginActivity::class.java)
            finish()
        }
    }

    fun getOriginalSearchablePoliticiansList() = mOriginalSearchablePoliticiansList

    fun getSinglePolitician() = mPolitician

    fun getUserEmail() = mFirebaseAuthenticator.getUserEmail()

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mSelectorModel.onDestroy()
        mSinglePoliticianModel.onDestroy()
        mView?.onDestroy()
        mView = null
        super.onDestroy()
    }
}
