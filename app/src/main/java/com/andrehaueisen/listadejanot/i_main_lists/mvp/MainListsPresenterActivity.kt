package com.andrehaueisen.listadejanot.i_main_lists.mvp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.i_main_lists.dagger.DaggerMainListsComponent
import com.andrehaueisen.listadejanot.i_main_lists.dagger.MainListsModule
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.SortType
import com.andrehaueisen.listadejanot.utilities.startNewActivity
import com.google.firebase.database.ValueEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.i_activity_main_lists.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by andre on 10/22/2017.
 */
class MainListsPresenterActivity : AppCompatActivity() {

    private val ACTIVITY_REQUEST_CODE = 1

    @Inject
    lateinit var mFirebaseRepository: FirebaseRepository
    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    @field:[Inject Named("deputados_listener")]
    lateinit var mDeputadosListener: ValueEventListener

    @field:[Inject Named("senadores_listener")]
    lateinit var mSenadoresListener: ValueEventListener

    @field:[Inject Named("governadores_listener")]
    lateinit var mGovernadoresListener: ValueEventListener

    @field:[Inject Named("user_listener")]
    lateinit var mUserValueEventListener: ValueEventListener

    @Inject
    lateinit var mMainListsModel: MainListsModel

    @Inject
    lateinit var mUser: User

    var mView: MainListsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMainListsComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .mainListsModule(MainListsModule(supportLoaderManager))
                .build()
                .inject(this)

        setContentView(R.layout.i_activity_main_lists)
        mView = MainListsView(this)

        if (savedInstanceState == null) {
            mFirebaseRepository.getFullDeputadosList(mDeputadosListener)
            mFirebaseRepository.getFullSenadoresList(mSenadoresListener)
            mFirebaseRepository.getFullGovernadoresList(mGovernadoresListener)
            mView?.setViews(null)
        } else {
            mView?.setViews(savedInstanceState)
        }

        subscribeToPoliticiansLoadingStatus()
        subscribeToDeputados()
        subscribeToGovernador()
        subscribeToSenadores()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mView?.onSaveInstanceState(outState)
    }

    private fun subscribeToPoliticiansLoadingStatus(){
        mMainListsModel.subscribeToPoliticiansLoadingStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { listsAreReady -> if(listsAreReady) mView?.dismissAlertDialog()}
    }

    private fun subscribeToDeputados() {
        mMainListsModel.subscribeToDeputados()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { politicians -> mView?.notifyNewDeputado(politicians) }
    }

    private fun subscribeToSenadores() {
        mMainListsModel.subscribeToSenadores()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { politicians -> mView?.notifyNewSenador(politicians) }
    }

    private fun subscribeToGovernador() {
        mMainListsModel.subscribeToGovernadores()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { politicians -> mView?.notifyNewGovernador(politicians) }
    }

    fun sortPoliticians(sortType: SortType) {
        mMainListsModel.sortPoliticiansList(sortType)
    }

    private fun getUserEmail() = mFirebaseAuthenticator.getUserEmail()

    fun showUserVoteListIfLogged() = if (mFirebaseAuthenticator.isUserLoggedIn()) {
        val intent = Intent(this, UserVoteListPresenterActivity::class.java)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE)

    } else {
        startNewActivity(LoginActivity::class.java)
        finish()
    }

    fun invalidateUser(){
        mUser.refreshUser(User())
    }

    override fun onStart() {
        super.onStart()
        mFirebaseRepository.listenToUser(mUserValueEventListener, getUserEmail())
    }

    override fun onBackPressed() {
        if (group_buttons.visibility == View.VISIBLE)
            super.onBackPressed()
        else
            mView?.onBackPressed()
    }

    override fun onStop() {
        mFirebaseRepository.destroyUserListener(mUserValueEventListener, getUserEmail())
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainListsModel.onDestroy()
        mFirebaseRepository.destroyPoliticiansListsListeners(mDeputadosListener, mSenadoresListener, mGovernadoresListener)
    }
}