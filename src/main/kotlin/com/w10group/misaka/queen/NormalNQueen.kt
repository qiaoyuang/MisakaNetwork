package com.w10group.misaka.queen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun queen(n: Int): Int = Array(n) { it }.permutationQueen(0)

// 普通递归 N 皇后
private fun Array<Int>.permutationQueen(i: Int): Int {
    var count = 0
    lateinit var d: Array<Int>
    fun judgment() {
        if (d.judgmentQueen()) {
            //d.show()
            count++
        }
    }
    if (i < size - 1) {
        for (j in i until size) {
            d = clone()
            if (i != j) {
                d[i] = d[j].also { d[j] = d[i] }
                judgment()
            } else if (i == 0) judgment()
            count += d.permutationQueen(i + 1)
        }
    }
    return count
}

fun CoroutineScope.queenAsync(n: Int): Deferred<Int> = async {
    val channel = Channel<Int>(1)
    permutationQueenConcurrent(Array(n) { it }, 0, channel)
    channel.receive()
}

// 协程并发 N 皇后
private fun CoroutineScope.permutationQueenConcurrent(array: Array<Int>, i: Int, sendChannel: SendChannel<Int>): Job = launch {
    var count = 0
    val mutex = Mutex()

    suspend fun Array<Int>.judgment() {
        if (judgmentQueen()) {
            mutex.withLock {
                //show()
                count++
            }
        }
    }

    val channelSize = array.size - i
    val subChannel = Channel<Int>(channelSize)
    if (i < channelSize) {
        for (j in i until array.size) {
            launch {
                val d = array.clone()
                if (i != j) {
                    d[i] = d[j].also { d[j] = d[i] }
                    launch { d.judgment() }
                } else if (i == 0) launch { d.judgment() }
                permutationQueenConcurrent(d, i + 1, subChannel)
            }
        }
    }

    repeat(channelSize) {
        val newCount = subChannel.receive()
        mutex.withLock {
            count += newCount
        }
    }
    sendChannel.send(count)
}

private fun Array<Int>.judgmentQueen(): Boolean {
    for (i in indices) {
        for (j in i + 1 until size) {
            val value = j - i
            if (this[i] + value == this[j] || this[i] - value == this[j])
                return false
        }
    }
    return true
}

private fun Array<Int>.show() {
    for (i in indices) {
        for (j in indices)
            print(if (this[j] == i) 1 else 0)
        println()
    }
    println()
}