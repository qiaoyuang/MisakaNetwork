package com.w10group.misaka

import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.net.ServerSocket
import java.net.Socket

/**
 * 主函数
 */

fun main(args: Array<String>) {
    println("服务器正在启动......")
    Observable.create<Socket> {
        val serverSocket = ServerSocket(3000)
        var count = 0
        println("服务器启动完成，正在等待连接。")
        while (true) {
            it.onNext(serverSocket.accept())
            println("当前已连接客户端数量：${++count}")
        }
    }
            .observeOn(Schedulers.io())
            .subscribeBy {
                
            }
}