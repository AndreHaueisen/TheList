package com.andrehaueisen.listadejanot.h_user_vote_list.mvp

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.b_firebase.FirebaseAuthenticator
import com.andrehaueisen.listadejanot.b_firebase.FirebaseRepository
import com.andrehaueisen.listadejanot.c_database.PoliticiansContract
import com.andrehaueisen.listadejanot.models.Politician
import com.andrehaueisen.listadejanot.models.User
import com.andrehaueisen.listadejanot.utilities.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.ByteArrayOutputStream

/**
 * Created by andre on 6/20/2017.
 */
class UserVoteListModel(val mContext: Context,
                        val mLoaderManager: LoaderManager,
                        val mFirebaseRepository: FirebaseRepository,
                        val mFirebaseAuthenticator: FirebaseAuthenticator) : LoaderManager.LoaderCallbacks<Cursor>, UserVoteListMvpContract.Model {

    private val COLUMNS_INDEX_CARGO = 0
    private val COLUMNS_INDEX_NAME = 1
    private val COLUMNS_INDEX_EMAIL = 2
    private val COLUMNS_INDEX_IMAGE = 3

    private lateinit var mOnUserReadyPublisher: PublishSubject<User>
    private lateinit var mOnVoteCountHashMapReadyPublisher: PublishSubject<HashMap<String, Long>>
    private lateinit var mUser: User
    private lateinit var mVoteCountHashMap: HashMap<String, Long>

    private val mOnUserVoteListReadyPublisher: PublishSubject<ArrayList<Politician>> = PublishSubject.create()
    private val mCompositeDisposable = CompositeDisposable()

    fun initiateUserLoad() {
        val userEmail = mFirebaseAuthenticator.getUserEmail()
        if (userEmail == null) {
            mContext.showToast(mContext.getString(R.string.null_email_found))

        } else {
            mOnUserReadyPublisher = mFirebaseRepository.getUser(userEmail)
            mOnUserReadyPublisher
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mOnUserReadyObservable)
        }
    }

    private val mOnUserReadyObservable = object : Observer<User> {

        override fun onNext(user: User?) {
            mUser = user ?: User()
            if (mUser.condemnations.isNotEmpty()) {
                mOnVoteCountHashMapReadyPublisher = mFirebaseRepository.getVoteCountList()
                getVoteCountHashMap()
            }else{
                mOnUserVoteListReadyPublisher.onNext(arrayListOf<Politician>())
            }
        }

        override fun onSubscribe(disposable: Disposable?) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(e: Throwable?) {}
        override fun onComplete() {}
    }

    fun getVoteCountHashMap() {
        mOnVoteCountHashMapReadyPublisher
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mOnVoteListCountReadyObservable)
    }

    private val mOnVoteListCountReadyObservable = object : Observer<HashMap<String, Long>> {
        override fun onNext(voteCountHashMap: HashMap<String, Long>?) {
            mVoteCountHashMap = voteCountHashMap ?: HashMap()
            initiateDataLoad()
        }

        override fun onSubscribe(disposable: Disposable?) {
            mCompositeDisposable.add(disposable)
        }

        override fun onError(e: Throwable?) {}
        override fun onComplete() {}
    }

    private fun initiateDataLoad() {
        if (mLoaderManager.getLoader<Cursor>(LOADER_ID) == null) {
            mLoaderManager.initLoader(LOADER_ID, null, this)

        } else {
            mLoaderManager.restartLoader(LOADER_ID, null, this)
        }
    }

    override fun onCreateLoader(p0: Int, userBundle: Bundle?): Loader<Cursor> {

        val politiciansEntry = PoliticiansContract.Companion.PoliticiansEntry()
        val politiciansEmails = mUser.condemnations.keys.map { email -> email.decodeEmail() }
        val selectionArgsPlaceholders = politiciansEmails.createFormattedString("(", "?,", "?)", ignoreCollectionValues = true)
        return CursorLoader(
                mContext,
                politiciansEntry.CONTENT_URI,
                POLITICIANS_COLUMNS,
                "${politiciansEntry.COLUMN_EMAIL} IN $selectionArgsPlaceholders",
                politiciansEmails.toTypedArray(),
                null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {

        if (data != null && data.count != 0) {
            data.moveToFirst()

            val listOfVotedPoliticians = arrayListOf<Politician>()

            for (i in 0..data.count - 1) {
                val politician: Politician
                if (data.getString(COLUMNS_INDEX_CARGO) == Politician.Post.DEPUTADO.name) {

                    val deputadoName = data.getString(COLUMNS_INDEX_NAME)
                    val deputadoEmail = data.getString(COLUMNS_INDEX_EMAIL)
                    val deputadoImage = data.getBlob(COLUMNS_INDEX_IMAGE)
                    politician = Politician(
                            Politician.Post.DEPUTADO,
                            deputadoName,
                            (mVoteCountHashMap[deputadoEmail.encodeEmail()] ?: 0),
                            deputadoEmail,
                            deputadoImage.resamplePic(100))

                } else {
                    val senadorName = data.getString(COLUMNS_INDEX_NAME)
                    val senadorEmail = data.getString(COLUMNS_INDEX_EMAIL)
                    val senadorImage = data.getBlob(COLUMNS_INDEX_IMAGE)
                    politician = Politician(
                            Politician.Post.SENADOR,
                            senadorName,
                            mVoteCountHashMap[senadorEmail.encodeEmail()] ?: 0,
                            senadorEmail,
                            senadorImage.resamplePic(100))

                }
                listOfVotedPoliticians.add(politician)
                data.moveToNext()
            }

            mOnUserVoteListReadyPublisher.onNext(listOfVotedPoliticians)
            data.close()
        }
    }

    fun loadUserVotesList(): Observable<ArrayList<Politician>> = Observable.defer { mOnUserVoteListReadyPublisher }
    fun getUser() = mUser

    private fun ByteArray.resamplePic(quality: Int): ByteArray {
        val THUMBNAIL_WIDTH = 110
        val THUMBNAIL_HEIGHT = 110

        val bmp = BitmapFactory.decodeByteArray(this, 0, this.size)
        val stream = ByteArrayOutputStream()
        val thumbnailBitmap = Bitmap.createScaledBitmap(bmp, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false)
        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        return stream.toByteArray()
    }

    fun onDestroy() {
        mCompositeDisposable.dispose()
    }

    override fun onLoaderReset(p0: Loader<Cursor>?) {

    }
}