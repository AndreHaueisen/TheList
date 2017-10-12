package com.andrehaueisen.listadejanot.e_information.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import io.reactivex.disposables.CompositeDisposable

class InformationPresenterActivity : AppCompatActivity(), InformationMvpContract.Presenter {

    private var mView: InformationView? = null
    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mView = InformationView(this)
        mView?.setViews()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mView?.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        mView = null
        mCompositeDisposable.dispose()
        super.onDestroy()
    }
}
