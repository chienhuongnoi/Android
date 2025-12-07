package com.example.noteapp.ui.notes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.database.NotesDatabseHelper
import com.example.noteapp.databinding.ActivityAddNoteBinding
import com.example.noteapp.model.Note

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: NotesDatabseHelper
    private var selectedCategoryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = NotesDatabseHelper(this)
        selectedCategoryId = intent.getIntExtra("selectedCategoryId", 0)

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            val note = Note(
                id = 0,
                title = title,
                content = content,
                categoryId = selectedCategoryId   // GHI VÀO ĐÚNG CATEGORY!!!
            )

            db.insertNote(note)

            Toast.makeText(this, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}