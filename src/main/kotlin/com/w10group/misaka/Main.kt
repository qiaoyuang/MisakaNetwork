package com.w10group.misaka

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 主函数
 */

fun main(args: Array<String>) = runBlocking {
    createByCoroutines(this)
}

typealias SocketList = CopyOnWriteArrayList<Socket>

private suspend fun createByCoroutines(coroutineScope: CoroutineScope) {
    println("服务器正在启动......")
    val socketList = SocketList()
    val serverSocket = ServerSocket(3000)
    var count = 0
    println("服务器启动完成，正在等待连接。")
    while (true) {
        val socket = serverSocket.accept()
        socketList.add(socket)
        val deferred = coroutineScope.async {
            val gson = Gson()
            BufferedReader(InputStreamReader(socket.getInputStream())).use {
                var line = it.readLine()
                try {
                    while (line.isNotBlank()) {
                        val message = gson.fromJson(line, Message::class.java)
                        if (UserManager.identityCheck(message.id)) {
                            println("${message.name}：${message.content}")
                            socketList.forEach { selectSocket ->
                                if (selectSocket !== socket) {
                                    val printWriter = PrintWriter(selectSocket.getOutputStream())
                                    printWriter.write(line)
                                    printWriter.flush()
                                    printWriter.close()
                                }
                            }
                        }
                        line = it.readLine()
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    println("异常：收到的数据格式非法。")
                }
            }
        }
        deferred.join()
        println("当前已连接客户端数量：${++count}")
    }
}


private fun createByRxJava() {
    println("服务器正在启动......")
    val socketList = CopyOnWriteArrayList<Socket>()
    Observable.create<Socket> {
        val serverSocket = ServerSocket(3000)
        var count = 0
        println("服务器启动完成，正在等待连接。")
        while (true) {
            val socket = serverSocket.accept()
            socketList.add(socket)
            it.onNext(socket)
            println("当前已连接客户端数量：${++count}")
        }
    }
            .observeOn(Schedulers.io())
            .subscribeBy {
                val gson = Gson()
                val bufferedReader = BufferedReader(InputStreamReader(it.getInputStream()))
                var line = bufferedReader.readLine()
                try {
                    while (line.isNotBlank()) {
                        val message = gson.fromJson(line, Message::class.java)
                        if (UserManager.identityCheck(message.id)) {
                            println("${message.name}：${message.content}")
                            socketList.forEach { socket ->
                                if (socket !== it) {
                                    val printWriter = PrintWriter(socket.getOutputStream())
                                    printWriter.write(line)
                                    printWriter.flush()
                                    printWriter.close()
                                }
                            }
                        }
                        line = bufferedReader.readLine()
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    println("异常：收到的数据格式非法。")
                } finally {
                    bufferedReader.close()
                }
            }
}