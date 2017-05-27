package com.andrehaueisen.listadejanot.utilities

fun <T> Collection<T>.createFormattedString(prefix: String, infix: String, suffix: String, ignoreCollectionValues: Boolean) : String{

    val stringBuilder = StringBuilder(prefix)

    if(ignoreCollectionValues) {
        for ((index, _) in iterator().withIndex()) {
            if (index != size - 1) {
                stringBuilder.append(infix)
            } else {
                stringBuilder.append(suffix)
            }
        }

    }else{
        for ((index, value) in iterator().withIndex()) {
            if (index != size - 1) {
                stringBuilder.append("$value$infix")
            } else {
                stringBuilder.append(suffix)
            }
        }
    }

    return stringBuilder.toString()
}

