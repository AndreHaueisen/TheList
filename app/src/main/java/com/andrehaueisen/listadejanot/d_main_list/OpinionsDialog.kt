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
import android.widget.TextView
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.utilities.BUNDLE_POLITICIAN_EMAIL
import com.andrehaueisen.listadejanot.utilities.BUNDLE_USER_EMAIL
import com.andrehaueisen.listadejanot.utilities.QUICK_ANIMATIONS_DURATION
import com.andrehaueisen.listadejanot.utilities.encodeEmail
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by andre on 8/1/2017.
 */
class OpinionsDialog() : DialogFragment() {

    private lateinit var mEmptyListTextView: TextView
    private lateinit var mCurrentOpinionTitleTextView: TextView
    private lateinit var mCurrentOpinionTextView: TextView
    private lateinit var mOpinionTextInput: TextInputLayout
    private lateinit var mDeleteOpinionImageButton: ImageButton
    private lateinit var mSendOpinionImageButton: ImageButton
    private lateinit var mOpinionsRecyclerView: RecyclerView
    private lateinit var mFirebaseRepository: FirebaseRepository

    private val mOpinionsMap = HashMap<String, String>()
    private var mOpinion: String? = null

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

        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.d_dialog_fragment_opinions, container, false)

        mEmptyListTextView = view?.findViewById(R.id.empty_opinions_list_text_view) as TextView
        mCurrentOpinionTitleTextView = view.findViewById(R.id.your_opinion_title_text_view) as TextView
        mCurrentOpinionTextView = view.findViewById(R.id.current_opinion_text_view) as TextView
        mOpinionsRecyclerView = view.findViewById(R.id.opinionsRecyclerView) as RecyclerView
        mDeleteOpinionImageButton = view.findViewById(R.id.delete_text_image_button) as ImageButton
        mSendOpinionImageButton = view.findViewById(R.id.send_opinion_image_button) as ImageButton
        mOpinionTextInput = view.findViewById(R.id.opinion_text_input_layout) as TextInputLayout

        setRecyclerView(mOpinionsRecyclerView)
        setImageButtons(mPoliticianEmail, mUserEmail)
        setTextInput()

        return view
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mPoliticianEmail = arguments.getString(BUNDLE_POLITICIAN_EMAIL)
        mUserEmail = arguments.getString(BUNDLE_USER_EMAIL)

        mFirebaseRepository.getPoliticianOpinions(mPoliticianEmail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (first, dataSnapshot) ->

                    if (mOpinionsRecyclerView.adapter == null) {
                        mOpinionsRecyclerView.adapter = OpinionsAdapter(activity!!, mOpinionsMap)
                    }

                    when (first) {
                        FirebaseRepository.FirebaseAction.CHILD_ADDED -> {
                            val emailKey = dataSnapshot.key as String
                            val opinionValue = dataSnapshot.value as String

                            mOpinionsMap[emailKey] = opinionValue
                            (mOpinionsRecyclerView.adapter as OpinionsAdapter).addItem(opinionValue, emailKey)
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

                })
    }

    private fun runTextChange(opinionValue: String){
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

    private fun resolveVisibility(){

        if (mOpinionsMap.isEmpty()) {
            mOpinionsRecyclerView.visibility = View.INVISIBLE
            mCurrentOpinionTitleTextView.visibility = View.GONE
            mCurrentOpinionTextView.visibility = View.GONE
            mDeleteOpinionImageButton.visibility = View.GONE
            mEmptyListTextView.visibility = View.VISIBLE

        } else {
            mOpinionsRecyclerView.visibility = View.VISIBLE
            mEmptyListTextView.visibility = View.GONE
            setCurrentOpinionTextViewVisibility()
        }
    }

    private fun setCurrentOpinionTextViewVisibility() {

        val encodedEmail = mUserEmail?.encodeEmail()
        if (mOpinionsMap.containsKey(encodedEmail)) {
            mCurrentOpinionTitleTextView.visibility = View.VISIBLE
            mCurrentOpinionTextView.visibility = View.VISIBLE
            mDeleteOpinionImageButton.visibility = View.VISIBLE
            mCurrentOpinionTextView.text = mOpinionsMap[encodedEmail]

        } else {
            mCurrentOpinionTitleTextView.visibility = View.GONE
            mCurrentOpinionTextView.visibility = View.GONE
            mDeleteOpinionImageButton.visibility = View.GONE
        }
    }

    override fun onDetach() {
        mFirebaseRepository.killFirebaseListener(mPoliticianEmail)
        super.onDetach()
    }


/*override fun onResume() {
    val params = dialog.window!!.attributes
    params.width = ViewGroup.LayoutParams.MATCH_PARENT
    params.height = ViewGroup.LayoutParams.MATCH_PARENT
    dialog.window!!.attributes = params

    super.onResume()
}*/

    private fun setRecyclerView(opinionsRecyclerView: RecyclerView) {
        opinionsRecyclerView.setHasFixedSize(true)
        opinionsRecyclerView.layoutManager = LinearLayoutManager(activity)
        //opinionsRecyclerView.itemAnimator = DefaultItemAnimator.
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