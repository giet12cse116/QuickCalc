package com.pp.Quickcalc.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoundState(
    val baseNumber: Int,
    val operator: Operator,
    val delta: Int,
    val options: List<Int>,
    val correctAnswer: Int
) : Parcelable {
    val expressionDisplay: String
        get() = "$baseNumber ${operator.symbol} $delta"
}
