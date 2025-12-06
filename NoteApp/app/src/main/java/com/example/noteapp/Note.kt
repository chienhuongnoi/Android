package com.example.noteapp

data class Note(val id: Int, val title: String, val content: String, val categoryId: Int? = null, val isPinned: Int = 0)
