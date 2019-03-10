package com.w10group.misaka.queen

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

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
            }
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
private fun CoroutineScope.permutationQueenConcurrent(array: Array<Int>, index: Int, sendChannel: SendChannel<Int>): Job = launch {
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

    val channelSize = array.size - index
    val subChannel = Channel<Int>(channelSize)
    val jobList = LinkedList<Job>()
    if (index < channelSize) {
        for (j in index until array.size) {
            val d = array.clone()
            if (index != j) {
                d[index] = d[j].also { d[j] = d[index] }
                jobList.add(launch { d.judgment() })
            }
            jobList.add(permutationQueenConcurrent(d, index + 1, subChannel))
        }
    }

    repeat(channelSize) {
        val job = launch {
            val newCount = subChannel.receive()
            mutex.withLock { count += newCount }
        }
        jobList.add(job)
    }
    jobList.forEach { it.join() }
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

// 用 0 和 1 的矩阵描述一种 N 皇后的情况
private fun Array<Int>.show() {
    for (i in indices) {
        for (j in indices)
            print(if (this[j] == i) 1 else 0)
        println()
    }
    println()
}