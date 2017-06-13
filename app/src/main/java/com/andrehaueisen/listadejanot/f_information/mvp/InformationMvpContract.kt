package com.andrehaueisen.listadejanot.f_information.mvp

import android.view.MenuItem

/**
 * Created by andre on 6/5/2017.
 */
interface InformationMvpContract {


    interface Presenter

    interface View{
        fun setViews()
        fun onOptionsItemSelected(item: MenuItem)

    }

}