package com.andrehaueisen.listadejanot.i_main_lists.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.i_main_lists.dagger.DaggerMainListsComponent
import com.andrehaueisen.listadejanot.i_main_lists.dagger.MainListsModule
import com.andrehaueisen.listadejanot.utilities.SortType
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

    var mView: MainListsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMainListsComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .mainListsModule(MainListsModule())
                .build()
                .inject(this)

        mFirebaseRepository.getFullDeputadosList(mDeputadosListener)
        mFirebaseRepository.getFullSenadoresList(mSenadoresListener)
        mFirebaseRepository.getFullGovernadoresList(mGovernadoresListener)

        setContentView(R.layout.i_activity_main_lists)
        mView = MainListsView(this)

        if (savedInstanceState == null) {
            mView?.setViews(null)
        } else {
            mView?.setViews(savedInstanceState)
        }

        subscribeToDeputados()
        subscribeToGovernador()
        subscribeToSenadores()

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
        mFirebaseRepository.destroyPoliticiansListsListeners(mDeputadosListener, mSenadoresListener, mGovernadoresListener)
    }
}