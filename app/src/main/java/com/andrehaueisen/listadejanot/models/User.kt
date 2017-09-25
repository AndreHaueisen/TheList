package com.andrehaueisen.listadejanot.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by andre on 9/25/2017.
 */
data class User(var condemnations: HashMap<String, Any> = hashMapOf()) : Parcelable {

    fun toSimpleMap(): Map<String, Any> {

        val simpleUserMap = HashMap<String, Any>()
        simpleUserMap.put("condemnations", condemnations)

        return simpleUserMap
    }

    constructor(source: Parcel) : this(
            source.readSerializable() as HashMap<String, Any>
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeSerializable(condemnations)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}