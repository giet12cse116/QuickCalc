package com.pp.Quickcalc.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Operator(val symbol: String) : Parcelable {
    PLUS("+"),
    MINUS("-");

    fun apply(a: Int, b: Int): Int {
        return when (this) {
            PLUS -> a + b
            MINUS -> a - b
        }
    }

    fun opposite(): Operator {
        return when (this) {
            PLUS -> MINUS
            MINUS -> PLUS
        }
    }
}
