package com.andrehaueisen.listadejanot.g_login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListPresenterActivity
import com.andrehaueisen.listadejanot.g_login.dagger.DaggerLoginActivityComponent
import com.andrehaueisen.listadejanot.utilities.showToast
import com.andrehaueisen.listadejanot.utilities.startNewActivity
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

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

        when (idpResponse?.errorCode) {

            ErrorCodes.NO_NETWORK -> {
                showToast(getString(R.string.no_network))
                startNewActivity(MainListPresenterActivity::class.java)
                finish()
            }

            ErrorCodes.UNKNOWN_ERROR -> {
                showToast(getString(R.string.unknown_error))
                startNewActivity(MainListPresenterActivity::class.java)
                finish()
            }

            else -> {
                mFirebaseAuthenticator.saveUserIdOnLogin()
                startNewActivity(MainListPresenterActivity::class.java)
                finish()
            }
        }

    }
}
