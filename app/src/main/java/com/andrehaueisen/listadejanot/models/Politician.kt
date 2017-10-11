package com.andrehaueisen.listadejanot.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by andre on 9/25/2017.
 */
data class Politician(@Exclude var post: Post? = null,
                      var name: String = "",
                      @Exclude var email: String? = null,
                      @Exclude var image: ByteArray? = null,
                      var votesNumber: Long = 0,
                      var honestyGrade: Float = -1F,
                      var leaderGrade: Float = -1F,
                      var promiseKeeperGrade: Float = -1F,
                      var rulesForThePeopleGrade: Float = -1F,
                      var answerVotersGrade: Float = -1F,

                      var honestyCount: Int = 0,
                      var leaderCount: Int = 0,
                      var promiseKeeperCount: Int = 0,
                      var rulesForThePeopleCount: Int = 0,
                      var answerVotersCount: Int = 0,

                      var recommendationsCount: Int = 0,
                      var condemnationsCount: Int = 0,

                      var condemnedBy: HashMap<String, Any> = hashMapOf(),
                      var recommendedBy: HashMap<String, Any> = hashMapOf(),
                      var onMainList: Boolean = false) : Parcelable, Comparable<Politician> {
    enum class Post : Parcelable {
        DEPUTADO, DEPUTADA, SENADOR, SENADORA, GOVERNADOR, GOVERNADORA;

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(ordinal)
        }

        override fun describeContents(): Int = 0

        companion object {

            val CREATOR: Parcelable.Creator<Post> = object : Parcelable.Creator<Post> {

                override fun createFromParcel(`in`: Parcel): Post = Post.values()[`in`.readInt()]

                override fun newArray(size: Int): Array<Post?> = arrayOfNulls(size)
            }
        }
    }

    fun toSimpleMap(isDataGoingToPreList: Boolean?): Map<String, Any> {

        val simplePoliticianMap = HashMap<String, Any>()
        simplePoliticianMap.put("name", name)
        simplePoliticianMap.put("votesNumber", votesNumber)
        simplePoliticianMap.put("condemnedBy", condemnedBy)
        simplePoliticianMap.put("recommendedBy", recommendedBy)

        simplePoliticianMap.put("honestyGrade", honestyGrade)
        simplePoliticianMap.put("leaderGrade", leaderGrade)
        simplePoliticianMap.put("promiseKeeperGrade", promiseKeeperGrade)
        simplePoliticianMap.put("rulesForThePeopleGrade", rulesForThePeopleGrade)
        simplePoliticianMap.put("answerVotersGrade", answerVotersGrade)

        simplePoliticianMap.put("honestyCount", honestyCount)
        simplePoliticianMap.put("leaderCount", leaderCount)
        simplePoliticianMap.put("promiseKeeperCount", promiseKeeperCount)
        simplePoliticianMap.put("rulesForThePeopleCount", rulesForThePeopleCount)
        simplePoliticianMap.put("answerVotersCount", answerVotersCount)

        simplePoliticianMap.put("recommendationsCount", recommendationsCount)
        simplePoliticianMap.put("condemnationsCount", condemnationsCount)

        if (isDataGoingToPreList!!) {
            simplePoliticianMap.put("onMainList", onMainList)
        }

        return simplePoliticianMap
    }

    override operator fun compareTo(politician: Politician): Int =
            Comparators.NAME.compare(this, politician)

    object Comparators {

        val NAME: Comparator<Politician> = Comparator { politician1, politician2 -> politician1.name.compareTo(politician2.name) }

        var VOTE_NUMBER: Comparator<Politician> = Comparator { politician1, politician2 -> (politician2.votesNumber - politician1.votesNumber).toInt() }

    }

    override fun toString(): String = name

    constructor(source: Parcel) : this(
            source.readValue(Int::class.java.classLoader)?.let { Post.values()[it as Int] },
            source.readString(),
            source.readString(),
            source.createByteArray(),
            source.readLong(),
            source.readFloat(),
            source.readFloat(),
            source.readFloat(),
            source.readFloat(),
            source.readFloat(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readSerializable() as HashMap<String, Any>,
            source.readSerializable() as HashMap<String, Any>,
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeValue(post?.ordinal)
        writeString(name)
        writeString(email)
        writeByteArray(image)
        writeLong(votesNumber)
        writeFloat(honestyGrade)
        writeFloat(leaderGrade)
        writeFloat(promiseKeeperGrade)
        writeFloat(rulesForThePeopleGrade)
        writeFloat(answerVotersGrade)
        writeInt(honestyCount)
        writeInt(leaderCount)
        writeInt(promiseKeeperCount)
        writeInt(rulesForThePeopleCount)
        writeInt(answerVotersCount)
        writeInt(recommendationsCount)
        writeInt(condemnationsCount)
        writeSerializable(condemnedBy)
        writeSerializable(recommendedBy)
        writeInt((if (onMainList) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Politician> = object : Parcelable.Creator<Politician> {
            override fun createFromParcel(source: Parcel): Politician = Politician(source)
            override fun newArray(size: Int): Array<Politician?> = arrayOfNulls(size)
        }
    }
}