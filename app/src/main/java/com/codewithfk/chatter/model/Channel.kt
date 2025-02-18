package com.codewithfk.chatter.model

data class Channel(
    val id: String = "",
    val name: String,
    val createdAt: Long = System.currentTimeMillis()) {
    val lastMessage: String = ""
    val time: String = ""

}