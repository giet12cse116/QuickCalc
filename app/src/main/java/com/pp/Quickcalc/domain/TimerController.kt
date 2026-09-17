package com.pp.Quickcalc.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerController(
    private val scope: CoroutineScope,
    private val tickMs: Long = 50L
) {
    private var job: Job? = null
    private val _timeLeftMs = MutableStateFlow(0L)
    val timeLeftMs: StateFlow<Long> = _timeLeftMs.asStateFlow()

    private var totalMs: Long = 0L

    fun start(durationMs: Long, onExpire: () -> Unit) {
        job?.cancel()
        totalMs = durationMs
        _timeLeftMs.value = durationMs
        job = scope.launch {
            while (_timeLeftMs.value > 0) {
                delay(tickMs)
                _timeLeftMs.value = (_timeLeftMs.value - tickMs).coerceAtLeast(0)
            }
            onExpire()
        }
    }

    fun addTime(bonusMs: Long, capMs: Long = totalMs) {
        _timeLeftMs.value = (_timeLeftMs.value + bonusMs).coerceAtMost(capMs)
    }

    fun stop() {
        job?.cancel()
    }

    fun progressFraction(): Float =
        if (totalMs == 0L) 0f else (_timeLeftMs.value.toFloat() / totalMs.toFloat())
}
