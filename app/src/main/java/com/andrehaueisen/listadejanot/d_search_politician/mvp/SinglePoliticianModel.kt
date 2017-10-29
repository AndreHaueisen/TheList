package com.andrehaueisen.listadejanot.d_search_politician.mvp

import android.os.AsyncTask
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.ListAction
import com.andrehaueisen.listadejanot.utilities.RatingBarType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 5/16/2017.
 */
class SinglePoliticianModel(private val mFirebaseRepository: FirebaseRepository,
                            private val mFirebaseAuthenticator: FirebaseAuthenticator,
                            private val mSelectorModel: PoliticianSelectorModel)
    : PoliticianSelectorMvpContract.IndividualPoliticianModel {

    private var mSinglePoliticianPublisher: PublishSubject<Politician> = PublishSubject.create()
    private val mCompositeDisposable = CompositeDisposable()

    override fun initiateSinglePoliticianLoad(politicianName: String) {
        SearchPoliticianOnList().execute(politicianName)
    }

    fun updateLists(listAction: ListAction, politician: Politician) {
        val userEmail = mFirebaseAuthenticator.getUserEmail()!!
        mFirebaseRepository.handleListChangeOnDatabase(listAction, politician, userEmail)
    }

    fun updateGrade(voteType: RatingBarType, outdatedGrade: Float, newGrade: Float, politician: Politician, user: User) {
        val userEmail = mFirebaseAuthenticator.getUserEmail()!!
        mFirebaseRepository.handleGradeChange(voteType, outdatedGrade, newGrade, politician, userEmail, user)
    }

    override fun loadSinglePoliticianPublisher(): PublishSubject<Politician> =
            mSinglePoliticianPublisher

    override fun onDestroy() {
        if (!mCompositeDisposable.isDisposed) mCompositeDisposable.dispose()
    }

    inner class SearchPoliticianOnList : AsyncTask<String, Unit, Politician?>() {

        override fun doInBackground(vararg names: String?): Politician? {
            val politicianName = names[0]

            return try {
                mSelectorModel.getSearchablePoliticiansList().first { politician -> politician.name == politicianName }
            } catch (noElementFound: NoSuchElementException) {
                null
            }
        }

        override fun onPostExecute(politicianFound: Politician?) {
            if (politicianFound != null) {
                mSinglePoliticianPublisher.onNext(politicianFound)
            }
        }
    }
}