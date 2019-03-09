package com.w10group.misaka

import com.w10group.misaka.queen.queen
import com.w10group.misaka.queen.queenAsync
import kotlinx.coroutines.*

/**
 * 主函数
 */

fun main() = runBlocking {
    val n = 8

    // 普通版本
    val startTime1 = System.currentTimeMillis()
    println("$n 皇后共有 ${queen(n)} 种解法")
    val endTime1 = System.currentTimeMillis()
    println("普通版本执行时间为：${endTime1 - startTime1} ms")

    println()

    // 并发版本
    val startTime2 = System.currentTimeMillis()
    println("$n 皇后共有 ${queenAsync(n).await()} 种解法")
    val endTime2 = System.currentTimeMillis()
    println("并发版本执行时间为：${endTime2 - startTime2} ms")
}