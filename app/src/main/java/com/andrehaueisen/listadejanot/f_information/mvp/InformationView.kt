package com.andrehaueisen.listadejanot.f_information.mvp

import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.utilities.VOTES_TO_MAIN_LIST_THRESHOLD
import kotlinx.android.synthetic.main.f_activity_information_presenter.*


/**
 * Created by andre on 6/5/2017.
 */
class InformationView(val mPresenterActivity: InformationPresenterActivity) : InformationMvpContract.View {

    init {
        mPresenterActivity.setContentView(R.layout.f_activity_information_presenter)
    }

    override fun setViews() {

        with(mPresenterActivity) {
            fun setToolbar() {
                val actionBar = information_toolbar
                setSupportActionBar(actionBar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowTitleEnabled(true)
            }

            fun setMotivationTextView(){
                app_motivation_text_view.text = mPresenterActivity.getString(R.string.understand_app_motivation, VOTES_TO_MAIN_LIST_THRESHOLD)
            }

            fun setEmailTextView() {
                my_email_text_view.text = getString(R.string.contact_email)
                my_email_text_view.linksClickable = true
                my_email_text_view.setOnClickListener {
                    with(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${mPresenterActivity.getString(R.string.contact_email)}"))) {

                        putExtra(Intent.EXTRA_EMAIL, mPresenterActivity.getString(R.string.contact_email))
                        putExtra(Intent.EXTRA_SUBJECT, mPresenterActivity.getString(R.string.subject_email))

                        mPresenterActivity.startActivity(Intent.createChooser(this, mPresenterActivity.getString(R.string.send_email_title)))
                    }
                }
            }

            setToolbar()
            setMotivationTextView()
            setEmailTextView()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem) {

        when (item.itemId) {
            android.R.id.home -> mPresenterActivity.finish()
        }
    }
}