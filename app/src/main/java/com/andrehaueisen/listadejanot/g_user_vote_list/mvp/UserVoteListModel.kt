package com.andrehaueisen.listadejanot.g_user_vote_list.mvp

import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.encodeEmail
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListModel(private val mDeputadosList: ArrayList<Politician>,
                        private val mSenadoresList: ArrayList<Politician>,
                        private val mGovernadoresList: ArrayList<Politician>,
                        private val mPresidentesList: ArrayList<Politician>,
                        private val mUser: User) {

    private val mOnUserListsPoliticiansReadyPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mCompositeDisposable = CompositeDisposable()
    private val mOnUserListsPoliticians = ArrayList<Politician>()

    fun fetchChosenPoliticians() {

        val userChosenPoliticiansEmails = mutableSetOf<String>()
        userChosenPoliticiansEmails.addAll(mUser.recommendations.keys)
        userChosenPoliticiansEmails.addAll(mUser.condemnations.keys)

        mOnUserListsPoliticians.addAll(mDeputadosList.filter { politician ->
            userChosenPoliticiansEmails.contains(politician.email?.encodeEmail())
        })

        mOnUserListsPoliticians.addAll(mSenadoresList.filter { politician ->
            userChosenPoliticiansEmails.contains(politician.email?.encodeEmail())
        })

        mOnUserListsPoliticians.addAll(mGovernadoresList.filter { politician ->
            userChosenPoliticiansEmails.contains(politician.email?.encodeEmail())
        })

        mOnUserListsPoliticians.addAll(mPresidentesList.filter { politician ->
            userChosenPoliticiansEmails.contains(politician.email?.encodeEmail())
        })

        mOnUserListsPoliticiansReadyPublisher.onNext(mOnUserListsPoliticians)
    }

    fun subscribeToOnUserListsPoliticians(): Observable<ArrayList<Politician>> = Observable.defer { mOnUserListsPoliticiansReadyPublisher }

    fun getUser() = mUser

    fun onDestroy() = mCompositeDisposable.dispose()

}