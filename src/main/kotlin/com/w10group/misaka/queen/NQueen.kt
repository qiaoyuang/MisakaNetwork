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
    if (i < size - 1) {
        for (j in i until size) {
            val d = clone()
            if (i != j) {
                d[i] = d[j].also { d[j] = d[i] }
                if (d.judgmentQueen()) count++
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
private fun CoroutineScope.permutationQueenConcurrent(array: Array<Int>, i: Int, sendChannel: SendChannel<Int>): Job = launch {
    var count = 0
    val mutex = Mutex()
    val channelSize = array.size - i
    val subChannel = Channel<Int>(channelSize)
    val jobList = LinkedList<Job>()
    if (i < array.size - 1) {
        for (j in i until array.size) {
            val d = array.clone()
            if (i != j) {
                d[i] = d[j].also { d[j] = d[i] }
                val job = launch {
                    if (d.judgmentQueen()) {
                        mutex.withLock {
                            count++
                        }
                    }
                }
                jobList.add(job)
            }
            permutationQueenConcurrent(d, i + 1, subChannel)
        }
        repeat(channelSize) {
            val job = launch {
                val newCount = subChannel.receive()
                mutex.withLock { count += newCount }
            }
            jobList.add(job)
        }
        jobList.forEach { it.join() }
    }
    sendChannel.send(count)
}

// 判断一个数组是否符合 N 皇后的情况
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