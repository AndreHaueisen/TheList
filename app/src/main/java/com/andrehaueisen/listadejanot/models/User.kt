package com.andrehaueisen.listadejanot.models

import com.google.firebase.database.Exclude
import java.util.*

/**
 * Created by andre on 5/3/2017.
 */

data class User(@Exclude var email: String, var age: String, var state: String, var sex: Boolean) {
    var condemnations: ArrayList<String>? = null
}
