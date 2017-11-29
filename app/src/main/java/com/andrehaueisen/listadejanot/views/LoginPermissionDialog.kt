package com.andrehaueisen.listadejanot.views

import android.app.Activity
import android.os.Bundle
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.d_main_lists_choices.mvp.MainListsChoicesPresenterActivity
import com.andrehaueisen.listadejanot.e_main_lists.MainListsPresenterActivity
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.g_user_vote_list.mvp.UserVoteListPresenterActivity
import com.andrehaueisen.listadejanot.j_login.LoginActivity
import com.andrehaueisen.listadejanot.utilities.CallingActivity
import com.andrehaueisen.listadejanot.utilities.INTENT_CALLING_ACTIVITY
import com.andrehaueisen.listadejanot.utilities.INTENT_POLITICIAN_NAME
import com.andrehaueisen.listadejanot.utilities.startNewActivity

/**
 * Created by andre on 11/23/2017.
 */
class LoginPermissionDialog(activity: Activity, val politicianName: String? = null): AlertDialog(activity) {

    private val mActivity: Activity = activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_login_permision)
        setCancelable(true)

        val idAnimatedVectorDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.anim_ic_id)
        val idAnimationView = findViewById<ImageView>(R.id.animation_view)
        idAnimationView?.setImageDrawable(idAnimatedVectorDrawable)
        idAnimatedVectorDrawable?.start()

        findViewById<Button>(R.id.positive_button)?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View) {

                val callingActivityName = when(mActivity){
                    is MainListsChoicesPresenterActivity-> CallingActivity.MAIN_LISTS_CHOICES_PRESENTER_ACTIVITY.name
                    is MainListsPresenterActivity -> CallingActivity.MAIN_LISTS_PRESENTER_ACTIVITY.name

                    is PoliticianSelectorPresenterActivity -> {
                        mActivity.finish()
                        CallingActivity.POLITICIAN_SELECTOR_PRESENTER_ACTIVITY.name

                    }
                    is UserVoteListPresenterActivity -> {
                        mActivity.finish()
                        CallingActivity.USER_VOTE_LIST_PRESENTER_ACTIVITY.name
                    }

                    else -> CallingActivity.MAIN_LISTS_CHOICES_PRESENTER_ACTIVITY.name
                }

                val extras = Bundle()
                extras.putString(INTENT_CALLING_ACTIVITY, callingActivityName)
                if(politicianName != null)
                    extras.putString(INTENT_POLITICIAN_NAME, politicianName)

                context.startNewActivity(LoginActivity::class.java, extras = extras)
                dismiss()
            }
        })

        findViewById<Button>(R.id.negative_button)?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                dismiss()
            }
        })

    }

}