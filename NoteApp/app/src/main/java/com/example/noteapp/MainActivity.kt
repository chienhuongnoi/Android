package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NotesDatabseHelper
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = NotesDatabseHelper(this)
        notesAdapter = NotesAdapter(db.getAllNotes(), this)

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter


        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }
        notesAdapter.onSelectionChanged = {count ->
            if (count > 0){
                binding.closeMultiSelectButton.visibility = View.VISIBLE
                binding.addButton.visibility = View.GONE
                binding.deleteSelectedItemButton.visibility = View.VISIBLE
                binding.selectedCountTextView.text = "$count selected"
                binding.selectedCountTextView.visibility = View.VISIBLE
            }
            else{
                binding.closeMultiSelectButton.visibility = View.GONE
                binding.addButton.visibility = View.VISIBLE
                binding.selectedCountTextView.visibility = View.GONE
                binding.deleteSelectedItemButton.visibility = View.GONE
            }
        }
        binding.closeMultiSelectButton.setOnClickListener {
            notesAdapter.clearSelection()
        }
        binding.deleteSelectedItemButton.setOnClickListener {
            val selectedPositions = notesAdapter.selectedItems.toList().sortedDescending()

            for (index in selectedPositions) {
                val noteId = notesAdapter.notes[index].id
                db.deleteNote(noteId)
            }

            notesAdapter.refreshData(db.getAllNotes())
            notesAdapter.clearSelection()
        }
    }

    override fun onResume() {
        super.onResume()
        notesAdapter.refreshData(db.getAllNotes())
    }
}