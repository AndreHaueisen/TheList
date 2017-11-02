package com.andrehaueisen.listadejanot.j_main_lists

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.f_login.LoginActivity
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.j_main_lists.dagger.DaggerMainListsComponent
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import javax.inject.Inject

/**
 * Created by andre on 11/1/2017.
 */
class MainListsPresenterActivity: AppCompatActivity() {

    private val ACTIVITY_REQUEST_CODE = 2

    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    @Inject
    lateinit var mUser: User

    private val mSenadoresSortedList = ArrayList<Politician>()
    private val mGovernadoresSortedList = ArrayList<Politician>()
    private val mDeputadosSortedList = ArrayList<Politician>()
    private var mSortType: SortType? = null

    private var mMainListsView: MainListsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.j_activity_main_lists)

        DaggerMainListsComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .build()
                .inject(this)

        val extras = intent.extras
        if(extras!= null){
            mSenadoresSortedList.addAll(extras.getParcelableArrayList(BUNDLE_SENADORES_LIST))
            mGovernadoresSortedList.addAll(extras.getParcelableArrayList(BUNDLE_GOVERNADORES_LIST))
            mDeputadosSortedList.addAll(extras.getParcelableArrayList(BUNDLE_DEPUTADOS_LIST))

            when(extras.getString(BUNDLE_SORT_TYPE)){
                SortType.RECOMMENDATIONS_COUNT.name -> mSortType = SortType.RECOMMENDATIONS_COUNT
                SortType.CONDEMNATIONS_COUNT.name -> mSortType = SortType.CONDEMNATIONS_COUNT
                SortType.TOP_OVERALL_GRADE.name -> mSortType = SortType.TOP_OVERALL_GRADE
                SortType.WORST_OVERALL_GRADE.name -> mSortType = SortType.WORST_OVERALL_GRADE
            }
        }

        mMainListsView = MainListsView(this)

        if (savedInstanceState == null) {
            mMainListsView?.setViews(null)
        } else {
            mMainListsView?.setViews(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMainListsView?.onSaveInstanceState(outState)
    }

    fun showUserVoteListIfLogged() = if (mFirebaseAuthenticator.isUserLoggedIn()) {
        val intent = Intent(this, UserVoteListPresenterActivity::class.java)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE)

    } else {
        startNewActivity(LoginActivity::class.java)
        finish()
    }

    fun getSortedSenadores() = mSenadoresSortedList
    fun getSortedGovernadores() = mGovernadoresSortedList
    fun getSortedDeputados() = mDeputadosSortedList
    fun getSortType() = mSortType

}