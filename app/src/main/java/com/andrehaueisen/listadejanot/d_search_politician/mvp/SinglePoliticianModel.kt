package com.andrehaueisen.listadejanot.d_search_politician.mvp

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

        val politicianFound = try {
            mSelectorModel.getSearchablePoliticiansList().first { politician -> politician.name == politicianName }
        }catch (noElementFound: NoSuchElementException){
            null
        }

        if(politicianFound != null){
            mSinglePoliticianPublisher.onNext(politicianFound)
        }

    }

    fun updateLists(listAction: ListAction, politician: Politician){
        val userEmail = mFirebaseAuthenticator.getUserEmail()!!
        mFirebaseRepository.handleListChangeOnDatabase(listAction, politician, userEmail)
    }

    fun updateGrade(voteType: RatingBarType, outdatedGrade:Float, newGrade: Float, politician: Politician, user: User){
        val userEmail = mFirebaseAuthenticator.getUserEmail()!!
        mFirebaseRepository.handleGradeChange(voteType, outdatedGrade, newGrade, politician, userEmail, user)
    }

    override fun loadSinglePoliticianPublisher(): PublishSubject<Politician> =
            mSinglePoliticianPublisher

    override fun onDestroy() {
        if (!mCompositeDisposable.isDisposed) mCompositeDisposable.dispose()
    }


}