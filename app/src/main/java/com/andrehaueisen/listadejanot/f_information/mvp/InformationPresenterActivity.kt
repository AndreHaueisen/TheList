package com.andrehaueisen.listadejanot.f_information.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean = super.onCreateOptionsMenu(menu)

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
