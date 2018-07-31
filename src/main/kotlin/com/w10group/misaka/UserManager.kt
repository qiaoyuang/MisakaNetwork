package com.w10group.misaka

import java.util.ArrayList

/**
 * 身份验证管理器
 */

object UserManager {

    private val mUserIdList by lazy {
        val list = listOf(10032, 10039, 10044, 10046, 10050,
                10090, 10501, 10774, 10777, 10840, 20001)
        val arrayList = ArrayList<Int>()
        arrayList.addAll(list)
        arrayList
    }

    fun identityCheck(id: Int): Boolean = mUserIdList.any { it == id }

}