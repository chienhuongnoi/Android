package com.example.noteapp

data class Note(val id: Int, val title: String, val content: String, val categoryId: Int? = null, var isPinned: Int = 0, val createdAt: String = "")
