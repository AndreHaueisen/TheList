package com.andrehaueisen.listadejanot.d_main_list

import android.app.Activity
import android.app.DialogFragment
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.google.firebase.database.DataSnapshot
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * Created by andre on 8/1/2017.
 */
class OpinionsDialog() : DialogFragment() {

    private lateinit var mFirebaseRepository: FirebaseRepository

    private val mOpinionsMap = HashMap<String, String>()
    private var mOpinion: String? = null

    private lateinit var mPoliticianImageView: ImageView
    private lateinit var mEmptyListTextView: TextView
    private lateinit var mCurrentOpinionTitleTextView: TextView
    private lateinit var mCurrentOpinionTextView: TextView
    private lateinit var mPoliticianNameTextView: TextView
    private lateinit var mOpinionsRecyclerView: RecyclerView
    private lateinit var mDeleteOpinionImageButton: ImageButton
    private lateinit var mSendOpinionImageButton: ImageButton
    private lateinit var mOpinionTextInput: TextInputLayout

    private lateinit var mPoliticianEmail: String
    private var mUserEmail: String? = null

    constructor(firebaseRepository: FirebaseRepository) : this() {
        mFirebaseRepository = firebaseRepository
    }

    companion object {

        fun newInstance(args: Bundle, firebaseRepository: FirebaseRepository): OpinionsDialog {
            val opinionDialog = OpinionsDialog(firebaseRepository)
            opinionDialog.arguments = args

            return opinionDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.d_dialog_fragment_opinions, container, false)

        view?.let {
            mPoliticianImageView = view.findViewById<ImageView>(R.id.politician_image_view)
            mEmptyListTextView = view.findViewById<TextView>(R.id.empty_opinions_list_text_view)
            mCurrentOpinionTitleTextView = view.findViewById<TextView>(R.id.your_opinion_title_text_view)
            mCurrentOpinionTextView = view.findViewById<TextView>(R.id.current_opinion_text_view)
            mPoliticianNameTextView = view.findViewById<TextView>(R.id.politician_name_text_view)
            mOpinionsRecyclerView = view.findViewById<RecyclerView>(R.id.opinions_recycler_view)
            mDeleteOpinionImageButton = view.findViewById<ImageButton>(R.id.delete_text_image_button)
            mSendOpinionImageButton = view.findViewById<ImageButton>(R.id.send_opinion_image_button)
            mOpinionTextInput = view.findViewById<TextInputLayout>(R.id.opinion_text_input_layout)
        }

        setRecyclerView(mOpinionsRecyclerView)
        setImageButtons(mPoliticianEmail, mUserEmail)
        setTextInput()

        mFirebaseRepository.getPoliticianOpinions(mPoliticianEmail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createObserver())

        return view
    }

    private fun setRecyclerView(opinionsRecyclerView: RecyclerView) {
        opinionsRecyclerView.setHasFixedSize(true)
        opinionsRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    private fun setImageButtons(politicianEmail: String, userEmail: String?) {

        if (userEmail == null) {
            mSendOpinionImageButton.visibility = View.GONE
        } else {
            mSendOpinionImageButton.visibility = View.VISIBLE
            mSendOpinionImageButton.setOnClickListener {
                if (!mOpinion.isNullOrBlank()) {
                    mFirebaseRepository.addOpinionOnPolitician(politicianEmail, userEmail, mOpinion!!)
                }
            }
        }

        mDeleteOpinionImageButton.setOnClickListener {
            mFirebaseRepository.removeOpinion(mPoliticianEmail, mUserEmail)
        }
    }

    private fun createObserver() = object : Observer<Pair<FirebaseRepository.FirebaseAction, DataSnapshot>> {

        override fun onSubscribe(disposable: Disposable) {

            if (mOpinionsRecyclerView.adapter == null) {
                mOpinionsMap.clear()
                mOpinionsRecyclerView.adapter = OpinionsAdapter(activity!!, mOpinionsMap)
            } else {
                mOpinionsMap.clear()
                (mOpinionsRecyclerView.adapter as OpinionsAdapter).resetData()
            }

            resolveVisibility()
        }

        override fun onNext(pair: Pair<FirebaseRepository.FirebaseAction, DataSnapshot>) {

            val firebaseAction = pair.first
            val dataSnapshot = pair.second

            when (firebaseAction) {
                FirebaseRepository.FirebaseAction.CHILD_ADDED -> {
                    val emailKey = dataSnapshot.key as String
                    val opinionValue = dataSnapshot.value as String

                    mOpinionsMap[emailKey] = opinionValue
                    (mOpinionsRecyclerView.adapter as OpinionsAdapter).addItem(opinionValue, emailKey)

                    if (mOpinionsMap.containsKey(emailKey.encodeEmail())) mCurrentOpinionTextView.text = opinionValue
                    resolveVisibility()
                }

                FirebaseRepository.FirebaseAction.CHILD_CHANGED -> {
                    val emailKey = dataSnapshot.key as String
                    val opinionValue = dataSnapshot.value as String

                    mOpinionsMap[emailKey] = opinionValue
                    (mOpinionsRecyclerView.adapter as OpinionsAdapter).changeItem(opinionValue, emailKey)

                    runTextChange(opinionValue)
                }

                FirebaseRepository.FirebaseAction.CHILD_REMOVED -> {
                    val emailKey = dataSnapshot.key

                    mOpinionsMap.remove(emailKey)
                    (mOpinionsRecyclerView.adapter as OpinionsAdapter).removeItem(emailKey)
                    resolveVisibility()
                }
            }
        }

        override fun onComplete() {}

        override fun onError(p0: Throwable) {}
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mPoliticianEmail = arguments.getString(BUNDLE_POLITICIAN_EMAIL)
        mUserEmail = arguments.getString(BUNDLE_USER_EMAIL)
    }


    override fun onStart() {
        super.onStart()

        val politicianImage = arguments.getByteArray(BUNDLE_POLITICIAN_IMAGE)
        val politicianName = arguments.getString(BUNDLE_POLITICIAN_NAME)

        fun setToolbar() {

            Glide.with(activity).load(politicianImage)
                    .crossFade()
                    .bitmapTransform(CropCircleTransformation(activity))
                    .placeholder(R.drawable.politician_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mPoliticianImageView)

            mPoliticianNameTextView.text = politicianName
        }
        setToolbar()
    }

    private fun runTextChange(opinionValue: String) {
        ExpectAnim()
                .expect(mCurrentOpinionTextView)
                .toBe(Expectations.scale(1.0F, 1.4F))
                .toAnimation()
                .setDuration(QUICK_ANIMATIONS_DURATION)
                .addEndListener {
                    mCurrentOpinionTextView.text = opinionValue

                    ExpectAnim()
                            .expect(mCurrentOpinionTextView)
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
            mOpinionsRecyclerView.visibility = View.INVISIBLE
            mEmptyListTextView.visibility = View.VISIBLE
            mOpinionTextInput.visibility = View.GONE
            mOpinionTextInput.editText?.visibility = View.GONE
            mCurrentOpinionTitleTextView.visibility = View.GONE
            mCurrentOpinionTextView.visibility = View.GONE
            mDeleteOpinionImageButton.visibility = View.GONE
            mSendOpinionImageButton.visibility = View.GONE

        } else if (isUserLoggedOut && mOpinionsMap.isNotEmpty()) {
            mOpinionsRecyclerView.visibility = View.VISIBLE
            mEmptyListTextView.visibility = View.GONE
            mOpinionTextInput.visibility = View.GONE
            mOpinionTextInput.editText?.visibility = View.GONE
            mCurrentOpinionTitleTextView.visibility = View.GONE
            mCurrentOpinionTextView.visibility = View.GONE
            mDeleteOpinionImageButton.visibility = View.GONE
            mSendOpinionImageButton.visibility = View.GONE

        } else if (!isUserLoggedOut && mOpinionsMap.isEmpty()) {
            mOpinionsRecyclerView.visibility = View.INVISIBLE
            mEmptyListTextView.visibility = View.VISIBLE
            mOpinionTextInput.visibility = View.VISIBLE
            mOpinionTextInput.editText?.visibility = View.VISIBLE
            mCurrentOpinionTitleTextView.visibility = View.GONE
            mCurrentOpinionTextView.visibility = View.GONE
            mDeleteOpinionImageButton.visibility = View.GONE
            mSendOpinionImageButton.visibility = View.VISIBLE

        } else if (!isUserLoggedOut && mOpinionsMap.isNotEmpty() && !hasUserAPastOpinion) {
            mOpinionsRecyclerView.visibility = View.VISIBLE
            mEmptyListTextView.visibility = View.GONE
            mOpinionTextInput.visibility = View.VISIBLE
            mOpinionTextInput.editText?.visibility = View.VISIBLE
            mCurrentOpinionTitleTextView.visibility = View.GONE
            mCurrentOpinionTextView.visibility = View.GONE
            mDeleteOpinionImageButton.visibility = View.GONE
            mSendOpinionImageButton.visibility = View.VISIBLE

        } else if (!isUserLoggedOut && mOpinionsMap.isNotEmpty() && hasUserAPastOpinion) {
            mOpinionsRecyclerView.visibility = View.VISIBLE
            mEmptyListTextView.visibility = View.GONE
            mOpinionTextInput.visibility = View.VISIBLE
            mOpinionTextInput.editText?.visibility = View.VISIBLE
            mCurrentOpinionTitleTextView.visibility = View.VISIBLE
            mCurrentOpinionTextView.visibility = View.VISIBLE
            mDeleteOpinionImageButton.visibility = View.VISIBLE
            mSendOpinionImageButton.visibility = View.VISIBLE
        }

    }

    override fun onDetach() {
        mFirebaseRepository.killFirebaseListener(mPoliticianEmail)
        mFirebaseRepository.completePublishOptionsList()
        super.onDetach()
    }

    override fun onResume() {
        val params = dialog.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = params

        super.onResume()
    }

    override fun onDestroyView() {
        val dialog = dialog

        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    private fun setTextInput() {
        mOpinionTextInput.editText?.addTextChangedListener(mOpinionTextWatcher)
    }

    private val mOpinionTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            mOpinion = editable?.toString()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }


}