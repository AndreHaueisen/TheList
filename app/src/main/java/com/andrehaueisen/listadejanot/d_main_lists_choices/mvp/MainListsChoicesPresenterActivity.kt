package com.andrehaueisen.listadejanot.d_main_lists_choices.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionInflater
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.d_main_lists_choices.dagger.DaggerMainListsChoicesComponent
import com.andrehaueisen.listadejanot.d_main_lists_choices.dagger.MainListsChoicesModule
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.utilities.SortType
import com.andrehaueisen.listadejanot.views.LoginPermissionDialog
import com.google.firebase.database.ValueEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by andre on 10/22/2017.
 */
class MainListsChoicesPresenterActivity : AppCompatActivity() {

    private val ACTIVITY_REQUEST_CODE = 1

    @Inject
    lateinit var mFirebaseRepository: FirebaseRepository
    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    @field:[Inject Named("presidentes_listener")]
    lateinit var mPresidentesListener: ValueEventListener

    @field:[Inject Named("deputados_listener")]
    lateinit var mDeputadosListener: ValueEventListener

    @field:[Inject Named("senadores_listener")]
    lateinit var mSenadoresListener: ValueEventListener

    @field:[Inject Named("governadores_listener")]
    lateinit var mGovernadoresListener: ValueEventListener

    @field:[Inject Named("user_listener")]
    lateinit var mUserValueEventListener: ValueEventListener

    @field:[Inject Named("media_highlight_listener")]
    lateinit var mMediaHighlightListener: ValueEventListener

    @Inject
    lateinit var mMainListsChoicesModel: MainListsChoicesModel


    private var mChoicesView: MainListsChoicesView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMainListsChoicesComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .mainListsChoicesModule(MainListsChoicesModule(supportLoaderManager))
                .build()
                .inject(this)

        setContentView(R.layout.d_activity_main_lists_choices)
        mChoicesView = MainListsChoicesView(this)

        if (savedInstanceState == null) {
            mFirebaseRepository.getFullPresidentesList(mPresidentesListener)
            mFirebaseRepository.getFullDeputadosList(mDeputadosListener)
            mFirebaseRepository.getFullSenadoresList(mSenadoresListener)
            mFirebaseRepository.getFullGovernadoresList(mGovernadoresListener)
            mFirebaseRepository.getMediaHighlightList(mMediaHighlightListener)
            mChoicesView?.beginDatabaseLoadingAlertDialog()
            mChoicesView?.verifyUserTokenValidity()
        }

        mChoicesView?.setViews()
        subscribeToPoliticiansLoadingStatus()
        subscribeToPoliticiansListsMap()

        mChoicesView?.loadOnboardingScreen()

        val fadeTransition = TransitionInflater.from(this).inflateTransition(R.transition.windows_fade)
        window.exitTransition = fadeTransition
        window.enterTransition = fadeTransition
    }

    private fun subscribeToPoliticiansLoadingStatus(){
        mMainListsChoicesModel.subscribeToPoliticiansLoadingStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { listsAreReady ->
                    if(listsAreReady){
                        mChoicesView?.dismissAlertDialog()
                        //mChoicesView?.loadOnboardingScreen()
                    }}
    }

    private fun subscribeToPoliticiansListsMap(){
        mMainListsChoicesModel.subscribeToPoliticiansListsMap()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { mChoicesView?.notifyPoliticiansReady(it[0], it[1], it[2], it[4]) }
    }

    fun sortPoliticians(sortType: SortType) {
        mMainListsChoicesModel.sortPoliticiansList(sortType)
    }

    private fun getUserEmail() = mFirebaseAuthenticator.getUserEmail()

    fun showUserVoteListIfLogged(): Unit = if (mFirebaseAuthenticator.isUserLoggedIn()) {
        val intent = Intent(this, UserVoteListPresenterActivity::class.java)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE)

    } else {
        LoginPermissionDialog(this).show()
    }

    fun getAuthenticator() = mFirebaseAuthenticator

    override fun onStart() {
        super.onStart()
        mFirebaseRepository.listenToUser(mUserValueEventListener, getUserEmail())

    }

    override fun onStop() {
        mFirebaseRepository.destroyUserListener(mUserValueEventListener, getUserEmail())
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainListsChoicesModel.onDestroy()
        mFirebaseRepository.destroyPoliticiansListsListeners(
                mDeputadosListener,
                mSenadoresListener,
                mGovernadoresListener,
                mPresidentesListener,
                mMediaHighlightListener)
    }
}