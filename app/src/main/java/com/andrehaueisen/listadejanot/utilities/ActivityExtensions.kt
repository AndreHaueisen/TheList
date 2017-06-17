package com.andrehaueisen.listadejanot.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent

fun <T: Activity> Activity.startNewActivity(classToInit: Class<T>){
    val intent = Intent(this, classToInit)
    this.startActivity(intent)
}

//Leave here for Kotlin study
inline fun <reified T: Activity> Context.startActivity(){
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}
