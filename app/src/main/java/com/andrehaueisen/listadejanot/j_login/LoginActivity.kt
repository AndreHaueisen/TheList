package com.andrehaueisen.listadejanot.j_login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.d_main_lists_choices.mvp.MainListsChoicesPresenterActivity
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.j_login.dagger.DaggerLoginActivityComponent
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    @Inject
    lateinit var mFirebaseRepository: FirebaseRepository

    @Inject
    lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerLoginActivityComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .build()
                .injectFirebaseAuthenticator(this)

        if (mFirebaseAuthenticator.isUserLoggedIn()) {
            finish()

        } else {
            mFirebaseAuthenticator.startLoginFlow(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val idpResponse = IdpResponse.fromResultIntent(data)

        if(idpResponse != null) {
            when (idpResponse.error?.errorCode) {

                ErrorCodes.NO_NETWORK -> {
                    showToast(getString(R.string.no_network))
                    startNewActivity(MainListsChoicesPresenterActivity::class.java)
                    finish()
                }

                ErrorCodes.UNKNOWN_ERROR -> {
                    showToast(getString(R.string.unknown_error))
                    startNewActivity(MainListsChoicesPresenterActivity::class.java)
                    finish()
                }

                else -> {
                    mFirebaseAuthenticator.saveUserIdOnLogin()
                    mFirebaseRepository.listenToUser(mUserListener, mFirebaseAuthenticator.getUserEmail())
                }
            }
        }else{
            startCallingActivity()
        }

    }

    private val mUserListener = object: ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {}

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            val refreshedUser: User = dataSnapshot?.getValue(User::class.java) ?: User()
            mUser.refreshUser(refreshedUser)

            startCallingActivity()
        }
    }

    private fun startCallingActivity(){

        if(intent.hasExtra(INTENT_CALLING_ACTIVITY)){
            val politicianName : String? = intent.extras.getString(INTENT_POLITICIAN_NAME)
            val callingActivity: String = intent.extras.getString(INTENT_CALLING_ACTIVITY)

            when(callingActivity){
                CallingActivity.MAIN_LISTS_CHOICES_PRESENTER_ACTIVITY.name -> {
                    startNewActivity(MainListsChoicesPresenterActivity::class.java)
                }

                CallingActivity.MAIN_LISTS_PRESENTER_ACTIVITY.name -> {}

                CallingActivity.POLITICIAN_SELECTOR_PRESENTER_ACTIVITY.name -> {
                    val extras = Bundle()
                    if (politicianName != null){
                        extras.putString(INTENT_POLITICIAN_NAME, politicianName)
                    }
                    startNewActivity(PoliticianSelectorPresenterActivity::class.java, extras = extras)
                }

                CallingActivity.USER_VOTE_LIST_PRESENTER_ACTIVITY.name ->{
                    startNewActivity(UserVoteListPresenterActivity::class.java)
                }
            }

            mFirebaseRepository.destroyUserListener(mUserListener, mFirebaseAuthenticator.getUserEmail())
            finish()
        }
    }
}
