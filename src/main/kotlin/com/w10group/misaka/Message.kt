package com.w10group.misaka

/**
 * 消息传输格式
 */

data class Message(var id: Int = 0,
                   var name: String = "",
                   var content: String = "")