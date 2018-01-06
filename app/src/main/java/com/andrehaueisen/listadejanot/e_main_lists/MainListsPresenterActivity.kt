package com.andrehaueisen.listadejanot.e_main_lists

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.e_main_lists.dagger.DaggerMainListsComponent
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.LoginPermissionDialog
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

    private val mPresidentesSortedList = ArrayList<Politician>()
    private val mSenadoresSortedList = ArrayList<Politician>()
    private val mGovernadoresSortedList = ArrayList<Politician>()
    private val mDeputadosSortedList = ArrayList<Politician>()
    private var mSortType: SortType? = null

    private var mMainListsView: MainListsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.e_activity_main_lists)

        DaggerMainListsComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .build()
                .inject(this)

        mMainListsView = MainListsView(this)

        val extras = intent.extras
        when(extras.getString(BUNDLE_SORT_TYPE)){
            SortType.RECOMMENDATIONS_COUNT.name -> mSortType = SortType.RECOMMENDATIONS_COUNT
            SortType.CONDEMNATIONS_COUNT.name -> mSortType = SortType.CONDEMNATIONS_COUNT
            SortType.TOP_OVERALL_GRADE.name -> mSortType = SortType.TOP_OVERALL_GRADE
            SortType.WORST_OVERALL_GRADE.name -> mSortType = SortType.WORST_OVERALL_GRADE
            SortType.MEDIA_HIGHLIGHT.name -> mSortType = SortType.MEDIA_HIGHLIGHT
        }

        if(savedInstanceState == null && extras != null){
            mPresidentesSortedList.addAll(extras.getParcelableArrayList(BUNDLE_PRESIDENTES_LIST))
            mSenadoresSortedList.addAll(extras.getParcelableArrayList(BUNDLE_SENADORES_LIST))
            mGovernadoresSortedList.addAll(extras.getParcelableArrayList(BUNDLE_GOVERNADORES_LIST))
            mDeputadosSortedList.addAll(extras.getParcelableArrayList(BUNDLE_DEPUTADOS_LIST))

            mMainListsView?.setViews(null)

        } else if (savedInstanceState != null && extras == null) {
            mMainListsView?.setViews(savedInstanceState)
        } else {
            mMainListsView?.setViews(extras)
        }

        postponeTransition()
    }

    private fun postponeTransition(){
        postponeEnterTransition()

        val decor = window.decorView
        decor.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                decor.viewTreeObserver.removeOnPreDrawListener(this)
                startPostponedEnterTransition()
                return true
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMainListsView?.onSaveInstanceState(outState)
    }

    fun showUserVoteListIfLogged() = if (mFirebaseAuthenticator.isUserLoggedIn()) {
        val intent = Intent(this, UserVoteListPresenterActivity::class.java)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE)

    } else {
        LoginPermissionDialog(this).show()
    }

    fun getSortedPresidentes() = mPresidentesSortedList
    fun getSortedSenadores() = mSenadoresSortedList
    fun getSortedGovernadores() = mGovernadoresSortedList
    fun getSortedDeputados() = mDeputadosSortedList
    fun getSortType() = mSortType

}