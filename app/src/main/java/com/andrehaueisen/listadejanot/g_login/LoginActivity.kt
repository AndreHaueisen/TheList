package com.andrehaueisen.listadejanot.g_login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.d_main_list.mvp.MainListPresenterActivity
import com.andrehaueisen.listadejanot.g_login.dagger.DaggerLoginActivityComponent
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        val idpResponse = IdpResponse.fromResultIntent(data)

        when (idpResponse?.errorCode) {

            ErrorCodes.NO_NETWORK -> {
                //TODO handle errors here
                /*if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            showSnackbar(R.string.unknown_sign_in_response);*/
                Toast.makeText(this, "No network", Toast.LENGTH_LONG).show()
            }

            ErrorCodes.UNKNOWN_ERROR -> {
                Toast.makeText(this, "Unknown error", Toast.LENGTH_LONG).show()
            }

            else -> {
                mFirebaseAuthenticator.saveUserIdOnLogin()
                startNewActivity(MainListPresenterActivity::class.java)
                finish()
            }
        }

    }
}
