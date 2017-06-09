package com.andrehaueisen.listadejanot.G_login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.A_application.BaseApplication
import com.andrehaueisen.listadejanot.B_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.D_main_list.mvp.MainListPresenterActivity
import com.andrehaueisen.listadejanot.G_login.dagger.DaggerLoginActivityComponent
import com.andrehaueisen.listadejanot.utilities.startNewActivity
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

        when(resultCode){
            Activity.RESULT_OK -> {
                mFirebaseAuthenticator.saveUserIdOnLogin()
                startNewActivity(MainListPresenterActivity::class.java)
                finish()
            }

            Activity.RESULT_CANCELED -> {
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
            }
        }

    }
}
