package com.andrehaueisen.listadejanot.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast


fun <T: Activity> Context.startNewActivity(classToInit: Class<T>, flags: List<Int>? = null){
    val intent = Intent(this, classToInit)
    flags?.let {
        flags.forEach { flag -> intent.flags = flag }
    }
    this.startActivity(intent)
}

//Leave here for Kotlin study
inline fun <reified T: Activity> Context.startActivity(){
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

fun Context.getAppropriateLayoutManager(): RecyclerView.LayoutManager{

    val SPAN_COUNT = 2
    val dm = applicationContext.resources.displayMetrics
    val screenWidth = dm.widthPixels / dm.density
    val screenHeight = dm.heightPixels / dm.density

    val smallestWidth: Float
    if (screenWidth < screenHeight) {
        smallestWidth =  screenWidth
    } else {
        smallestWidth = screenHeight
    }

    if(smallestWidth < 600){
        return LinearLayoutManager(this)
    }else{
        return GridLayoutManager(this, SPAN_COUNT)
    }
}

fun Context.isConnectedToInternet(): Boolean{

    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)

}

fun Context.showToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
