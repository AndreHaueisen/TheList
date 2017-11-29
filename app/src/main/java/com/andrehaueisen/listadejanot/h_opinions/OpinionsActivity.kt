package com.andrehaueisen.listadejanot.h_opinions

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.a_application.BaseApplication
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.h_opinions.dagger.DaggerOpinionsComponent
import com.andrehaueisen.listadejanot.utilities.*
import com.andrehaueisen.listadejanot.views.DeleteOpinionDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.google.firebase.database.DataSnapshot
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.h_activity_opinions.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by andre on 8/1/2017.
 */
class OpinionsActivity : AppCompatActivity() {

    @Inject
    @Named("opinions")
    lateinit var mFirebaseRepository: FirebaseRepository

    @Inject
    @Named("opinions")
    lateinit var mFirebaseAuthenticator: FirebaseAuthenticator

    private val mOpinionsMap: HashMap<String, String> = HashMap()

    private lateinit var mPoliticianEmail: String
    private var mUserEmail: String? = null

    private var mHasCurrentOpinion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerOpinionsComponent.builder()
                .applicationComponent(BaseApplication.get(this).getAppComponent())
                .build()
                .injectOpinions(this)

        setContentView(R.layout.h_activity_opinions)

        mPoliticianEmail = intent.extras.getString(BUNDLE_POLITICIAN_EMAIL)
        mUserEmail = intent.extras.getString(BUNDLE_USER_EMAIL)

        setRecyclerView(opinions_recycler_view)
        setImageButtons(mPoliticianEmail, mUserEmail)
        setTextInput(savedInstanceState)
        setCurrentOpinion()

        mFirebaseRepository.getPoliticianOpinions(mPoliticianEmail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createObserver())
    }

    private fun setRecyclerView(opinionsRecyclerView: RecyclerView) {
        opinionsRecyclerView.setHasFixedSize(true)
        opinionsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setImageButtons(politicianEmail: String, userEmail: String?) {

        if (userEmail == null) {
            send_opinion_image_button.visibility = View.GONE

        } else {
            send_opinion_image_button.visibility = View.VISIBLE
            send_opinion_image_button.setOnClickListener {

                val opinion = opinion_text_input_layout.editText?.text.toString()
                if (!opinion.isNullOrBlank() && mHasCurrentOpinion) {
                    val opinionReplacementAlertDialog = AlertDialog.Builder(this)

                    with(opinionReplacementAlertDialog) {

                        setTitle(R.string.replace_opinion_dialog_title)
                        setMessage(R.string.replace_opinion_dialog_message)
                        setPositiveButton(R.string.opinion_dialog_replace_button_positive,
                                { _, _ ->
                                    mFirebaseRepository.addOpinionOnPolitician(politicianEmail, userEmail, opinion)
                                })
                        setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
                        show()
                    }

                } else if (!opinion.isNullOrBlank()) {
                    mFirebaseRepository.addOpinionOnPolitician(politicianEmail, userEmail, opinion)
                }
            }
        }

        delete_opinion_image_button.setOnClickListener {

            val alertDialog = DeleteOpinionDialog(this)
            alertDialog.show()

            alertDialog.setPositiveButton().setOnClickListener {
                mFirebaseRepository.removeOpinion(mPoliticianEmail, mUserEmail)
                alertDialog.dismiss()
            }
            alertDialog.setNegativeButton().setOnClickListener { alertDialog.dismiss() }
        }
    }

    private fun setTextInput(bundleSavedState: Bundle?) {
        val opinion = bundleSavedState?.getString(BUNDLE_USER_OPINION)
        if (opinion != null) {
            opinion_text_input_layout.editText?.setText(opinion)
        }
    }

    private fun setCurrentOpinion() {
        current_opinion_text_view.setOnClickListener { opinionView ->
            val opinionAlertDialog = AlertDialog.Builder(this).createNeutralDialog(null, (opinionView as TextView).text.toString())
            opinionAlertDialog.show()
        }
    }

    private fun createObserver() = object : Observer<Pair<FirebaseRepository.FirebaseAction, DataSnapshot>> {

        override fun onSubscribe(disposable: Disposable) {

            if (opinions_recycler_view.adapter == null) {
                mOpinionsMap.clear()
                opinions_recycler_view.adapter = OpinionsAdapter(this@OpinionsActivity, mOpinionsMap)
            } else {
                mOpinionsMap.clear()
                (opinions_recycler_view.adapter as OpinionsAdapter).resetData()
            }

            resolveVisibility()
        }

        override fun onNext(pair: Pair<FirebaseRepository.FirebaseAction, DataSnapshot>) {

            val firebaseAction = pair.first
            val dataSnapshot = pair.second

            fun hasUserAOpinion(opinionsMap: HashMap<String, String>, currentUserEmail: String?): Boolean = opinionsMap.containsKey(currentUserEmail?.encodeEmail())

            when (firebaseAction) {
                FirebaseRepository.FirebaseAction.CHILD_ADDED -> {
                    val emailKey = dataSnapshot.key as String
                    val opinionValue = dataSnapshot.value as String

                    mOpinionsMap[emailKey] = opinionValue
                    (opinions_recycler_view.adapter as OpinionsAdapter).addItem(opinionValue, emailKey)

                    if (!mHasCurrentOpinion && hasUserAOpinion(mOpinionsMap, mFirebaseAuthenticator.getUserEmail())) {
                        current_opinion_text_view.text = opinionValue
                        mHasCurrentOpinion = true
                    }
                    resolveVisibility()
                }

                FirebaseRepository.FirebaseAction.CHILD_CHANGED -> {
                    val emailKey = dataSnapshot.key as String
                    val opinionValue = dataSnapshot.value as String

                    mOpinionsMap[emailKey] = opinionValue
                    (opinions_recycler_view.adapter as OpinionsAdapter).changeItem(opinionValue, emailKey)

                    runTextChange(opinionValue)
                }

                FirebaseRepository.FirebaseAction.CHILD_REMOVED -> {
                    val emailKey = dataSnapshot.key

                    mOpinionsMap.remove(emailKey)
                    (opinions_recycler_view.adapter as OpinionsAdapter).removeItem(emailKey)

                    if (mHasCurrentOpinion && !hasUserAOpinion(mOpinionsMap, mFirebaseAuthenticator.getUserEmail())) {
                        mHasCurrentOpinion = false
                    }
                    resolveVisibility()
                }
            }
        }

        override fun onComplete() = Unit

        override fun onError(p0: Throwable) = Unit
    }


    override fun onStart() {
        super.onStart()

        val politicianImage = intent.extras.getByteArray(BUNDLE_POLITICIAN_IMAGE)
        val politicianName = intent.extras.getString(BUNDLE_POLITICIAN_NAME)

        fun setToolbar() {

            val requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).circleCrop()
            val transitionOptions = DrawableTransitionOptions.withCrossFade()

            Glide.with(this)
                    .load(politicianImage)
                    .apply(requestOptions)
                    .transition(transitionOptions)
                    .into(opined_politician_image_view)

            politician_name_text_view.text = politicianName
        }
        setToolbar()
    }

    private fun runTextChange(opinionValue: String) {
        ExpectAnim()
                .expect(current_opinion_text_view)
                .toBe(Expectations.scale(1.0F, 1.4F))
                .toAnimation()
                .setDuration(QUICK_ANIMATIONS_DURATION)
                .addEndListener {
                    current_opinion_text_view.text = opinionValue

                    ExpectAnim()
                            .expect(current_opinion_text_view)
                            .toBe(Expectations.atItsOriginalScale())
                            .toAnimation()
                            .setDuration(QUICK_ANIMATIONS_DURATION)
                            .start()
                }
                .start()
    }

    private fun resolveVisibility() {

        val encodedEmail = mUserEmail?.encodeEmail()
        val isUserLoggedOut = encodedEmail == null
        val hasUserAPastOpinion = mOpinionsMap.containsKey(encodedEmail)

        if (isUserLoggedOut && mOpinionsMap.isEmpty()) {
            opinions_recycler_view.visibility = View.INVISIBLE
            empty_opinions_list_text_view.visibility = View.VISIBLE
            opinion_text_input_layout.visibility = View.GONE
            opinion_text_input_layout.editText?.visibility = View.GONE
            your_opinion_title_text_view.visibility = View.GONE
            current_opinion_text_view.visibility = View.GONE
            delete_opinion_image_button.visibility = View.GONE
            send_opinion_image_button.visibility = View.GONE

        } else if (isUserLoggedOut && mOpinionsMap.isNotEmpty()) {
            opinions_recycler_view.visibility = View.VISIBLE
            empty_opinions_list_text_view.visibility = View.GONE
            opinion_text_input_layout.visibility = View.GONE
            opinion_text_input_layout.editText?.visibility = View.GONE
            your_opinion_title_text_view.visibility = View.GONE
            current_opinion_text_view.visibility = View.GONE
            delete_opinion_image_button.visibility = View.GONE
            send_opinion_image_button.visibility = View.GONE

        } else if (!isUserLoggedOut && mOpinionsMap.isEmpty()) {
            opinions_recycler_view.visibility = View.INVISIBLE
            empty_opinions_list_text_view.visibility = View.VISIBLE
            opinion_text_input_layout.visibility = View.VISIBLE
            opinion_text_input_layout.editText?.visibility = View.VISIBLE
            your_opinion_title_text_view.visibility = View.GONE
            current_opinion_text_view.visibility = View.GONE
            delete_opinion_image_button.visibility = View.GONE
            send_opinion_image_button.visibility = View.VISIBLE

        } else if (!isUserLoggedOut && mOpinionsMap.isNotEmpty() && !hasUserAPastOpinion) {
            opinions_recycler_view.visibility = View.VISIBLE
            empty_opinions_list_text_view.visibility = View.GONE
            opinion_text_input_layout.visibility = View.VISIBLE
            opinion_text_input_layout.editText?.visibility = View.VISIBLE
            your_opinion_title_text_view.visibility = View.GONE
            current_opinion_text_view.visibility = View.GONE
            delete_opinion_image_button.visibility = View.GONE
            send_opinion_image_button.visibility = View.VISIBLE

        } else if (!isUserLoggedOut && mOpinionsMap.isNotEmpty() && hasUserAPastOpinion) {
            opinions_recycler_view.visibility = View.VISIBLE
            empty_opinions_list_text_view.visibility = View.GONE
            opinion_text_input_layout.visibility = View.VISIBLE
            opinion_text_input_layout.editText?.visibility = View.VISIBLE
            your_opinion_title_text_view.visibility = View.VISIBLE
            current_opinion_text_view.visibility = View.VISIBLE
            delete_opinion_image_button.visibility = View.VISIBLE
            send_opinion_image_button.visibility = View.VISIBLE
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(BUNDLE_USER_OPINION, opinion_text_input_layout.editText?.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mFirebaseRepository.killFirebaseListener(mPoliticianEmail)
        mFirebaseRepository.completePublishOptionsList()
        super.onDestroy()
    }
}