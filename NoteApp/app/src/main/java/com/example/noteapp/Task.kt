package com.example.noteapp

data class Task(
    val id: Int = 0,
    val name: String,
    val deadline: String? = null, // Có thể null
    val isDone: Int = 0 // 0 = chưa hoàn thành, 1 = hoàn thành
)
