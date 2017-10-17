package com.andrehaueisen.listadejanot.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.widget.Toast




fun <T: Activity> Context.startNewActivity(classToInit: Class<T>, flags: List<Int>? = null, extras: Bundle? = null, options: Bundle? = null){

    val intent = Intent(this, classToInit)
    flags?.let {
        flags.forEach { flag -> intent.flags = flag }
    }

    if(extras != null) {
        intent.putExtras(extras)
    }

    if(options != null){
        this. startActivity(intent, options)
    }else {
        this.startActivity(intent)
    }
}

//Leave here for Kotlin study
inline fun <reified T: Activity> Context.startActivity(){
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

fun Context.getAppropriateLayoutManager(): RecyclerView.LayoutManager{

    val SPAN_COUNT = 2

    return if(resources.configuration.smallestScreenWidthDp < 600){
        LinearLayoutManager(this)
    }else{
        GridLayoutManager(this, SPAN_COUNT)
    }
}

fun Context.isConnectedToInternet(): Boolean{

    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)

}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, message, duration).show()

inline fun <reified T: Any> Context.putValueOnSharedPreferences(key: String, data: T){

    val editor = this.getSharedPreferences(SHARED_PREFERENCES, 0).edit()

    when(data::class){
        String::class -> editor.putString(key, data as String)
        Int::class -> editor.putInt(key, data as Int)
        Boolean::class -> editor.putBoolean(key, data as Boolean)
        Long::class -> editor.putLong(key, data as Long)
        Float::class -> editor.putFloat(key, data as Float)
        Set::class -> editor.putStringSet(key, data as Set<String>)
    }

    editor.apply()
}

fun  Context.pullStringFromSharedPreferences(key: String): String{
    val sharedPreference = this.getSharedPreferences(SHARED_PREFERENCES, 0)
    return sharedPreference.getString(key, null)
}

fun Context.convertDipToPixel(dipValue: Float): Float{
    val metrics = this.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
}