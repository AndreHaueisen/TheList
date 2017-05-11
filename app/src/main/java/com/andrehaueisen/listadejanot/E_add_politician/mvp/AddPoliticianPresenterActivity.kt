package com.andrehaueisen.listadejanot.E_add_politician.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.listadejanot.D_main_list.mvp.MainListModel
import com.andrehaueisen.listadejanot.D_main_list.mvp.MainListPresenterActivity
import com.andrehaueisen.listadejanot.E_add_politician.dagger.DaggerAddPoliticianComponent
import com.andrehaueisen.listadejanot.R
import javax.inject.Inject

class AddPoliticianPresenterActivity : AppCompatActivity() {

    @Inject
    lateinit var mModel : MainListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.e_activity_add_politician)

        DaggerAddPoliticianComponent.builder()
                .mainListComponent(MainListPresenterActivity.get(this).getMainListComponent())
                .build()

        test()
    }

    fun test(){
        val l = 3
        val k = l+2
    }
}
