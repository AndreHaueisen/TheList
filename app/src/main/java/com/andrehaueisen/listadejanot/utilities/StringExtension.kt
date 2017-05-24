package com.andrehaueisen.listadejanot.utilities

fun String.encodeEmail() = replace('.', ',')
fun String.decodeEmail() = replace(',', '.')

