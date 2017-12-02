package com.andrehaueisen.listadejanot.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.Toast


fun <T : Activity> Context.startNewActivity(classToInit: Class<T>, flags: List<Int>? = null, extras: Bundle? = null, options: Bundle? = null) {

    val intent = Intent(this, classToInit)
    flags?.let {
        flags.forEach { flag -> intent.flags = flag }
    }

    if (extras != null) {
        intent.putExtras(extras)
    }

    if (options != null) {
        this.startActivity(intent, options)
    } else {
        this.startActivity(intent)
    }
}

fun Context.isConnectedToInternet(): Boolean {

    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)

}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, message, duration).show()

inline fun <reified T : Any> Context.putValueOnSharedPreferences(key: String, data: T) {

    val editor = this.getSharedPreferences(SHARED_PREFERENCES, 0).edit()

    when (data) {
        is String -> editor.putString(key, data as String)
        is Int -> editor.putInt(key, data as Int)
        is Boolean -> editor.putBoolean(key, data as Boolean)
        is Long -> editor.putLong(key, data as Long)
        is Float -> editor.putFloat(key, data as Float)
        is Set<*> -> editor.putStringSet(key, data as Set<String>)
    }

    editor.apply()
}

fun Activity.getProperLayoutManager(orientation: Int = LinearLayoutManager.VERTICAL): RecyclerView.LayoutManager {
    val display = this.windowManager.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)

    val scaleFactor = metrics.density;

    val widthDp = metrics.widthPixels / scaleFactor
    val heightDp = metrics.heightPixels / scaleFactor
    val smallestWidth = Math.min(widthDp, heightDp)

    return if (smallestWidth >= 600) {
        GridLayoutManager(this, 2, orientation, false)
    } else {
        LinearLayoutManager(this, orientation, false)
    }

}

fun Context.pullStringFromSharedPreferences(key: String): String =
        this.getSharedPreferences(SHARED_PREFERENCES, 0).getString(key, null)

fun Context.pullIntFromSharedPreferences(key: String): Int =
        this.getSharedPreferences(SHARED_PREFERENCES, 0).getInt(key, 0)

fun Context.pullBooleanFromSharedPreferences(key: String, defaultValue: Boolean = false): Boolean =
        this.getSharedPreferences(SHARED_PREFERENCES, 0).getBoolean(key, defaultValue)

fun Context.convertDipToPixel(dipValue: Float): Float {
    val metrics = this.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
}