package com.andrehaueisen.listadejanot.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by andre on 9/25/2017.
 */
data class User(val condemnations: HashMap<String, String> = hashMapOf(),
                val recommendations: HashMap<String, String> = hashMapOf(),
                val honestyGrades: HashMap<String, Float> = hashMapOf(),
                val leaderGrades: HashMap<String, Float> = hashMapOf(),
                val promiseKeeperGrades: HashMap<String, Float> = hashMapOf(),
                val rulesForThePeopleGrades: HashMap<String, Float> = hashMapOf(),
                val answerVotersGrades: HashMap<String, Float> = hashMapOf()) : Parcelable {
    fun refreshUser(user: User) {
        condemnations.clear()
        condemnations.putAll(user.condemnations)

        recommendations.clear()
        recommendations.putAll(user.recommendations)

        honestyGrades.clear()
        honestyGrades.putAll(user.honestyGrades)

        leaderGrades.clear()
        leaderGrades.putAll(user.leaderGrades)

        promiseKeeperGrades.clear()
        promiseKeeperGrades.putAll(user.promiseKeeperGrades)

        rulesForThePeopleGrades.clear()
        rulesForThePeopleGrades.putAll(user.rulesForThePeopleGrades)

        answerVotersGrades.clear()
        answerVotersGrades.putAll(user.answerVotersGrades)
    }

    fun toSimpleMap(): Map<String, Any> {

        val simpleUserMap = HashMap<String, Any>()
        simpleUserMap.put("condemnations", condemnations)
        simpleUserMap.put("recommendations", recommendations)

        simpleUserMap.put("honestyGrades", honestyGrades)
        simpleUserMap.put("leaderGrades", leaderGrades)
        simpleUserMap.put("promiseKeeperGrades", promiseKeeperGrades)
        simpleUserMap.put("rulesForThePeopleGrades", rulesForThePeopleGrades)
        simpleUserMap.put("answerVotersGrades", answerVotersGrades)

        return simpleUserMap
    }

    constructor(source: Parcel) : this(
            source.readSerializable() as HashMap<String, String>,
            source.readSerializable() as HashMap<String, String>,
            source.readSerializable() as HashMap<String, Float>,
            source.readSerializable() as HashMap<String, Float>,
            source.readSerializable() as HashMap<String, Float>,
            source.readSerializable() as HashMap<String, Float>,
            source.readSerializable() as HashMap<String, Float>
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeSerializable(condemnations)
        writeSerializable(recommendations)
        writeSerializable(honestyGrades)
        writeSerializable(leaderGrades)
        writeSerializable(promiseKeeperGrades)
        writeSerializable(rulesForThePeopleGrades)
        writeSerializable(answerVotersGrades)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}