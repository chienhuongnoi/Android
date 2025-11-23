package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NotesDatabseHelper
    private lateinit var notesAdapter: NotesAdapter

    lateinit var allNotes: List<Note>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = NotesDatabseHelper(this)
        notesAdapter = NotesAdapter(db.getAllNotes(), this)
        allNotes = db.getAllNotes()

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
//                binding.selectedCountTextView.text = "$count selected"
//                binding.selectedCountTextView.visibility = View.VISIBLE
                binding.notesHeading.text = "Đã chọn $count mục"
            }
            else{
                binding.closeMultiSelectButton.visibility = View.GONE
                binding.addButton.visibility = View.VISIBLE
//                binding.selectedCountTextView.visibility = View.GONE
                binding.notesHeading.text = "Notes."
                binding.deleteSelectedItemButton.visibility = View.GONE
            }
        }
        binding.closeMultiSelectButton.setOnClickListener {
            notesAdapter.clearSelection()
        }
        binding.deleteSelectedItemButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá các mục đã chọn?")
                .setPositiveButton("Xoá") { _, _ ->
                    val selectedPositions = notesAdapter.selectedItems.toList().sortedDescending()

                    for (index in selectedPositions) {
                        val noteId = notesAdapter.notes[index].id
                        db.deleteNote(noteId)
                    }
                    //Cập nhật lại dữ liều tìm kiếm
                    allNotes = db.getAllNotes()
                    //Cập nhật lại adapter
                    notesAdapter.refreshData(db.getAllNotes())
                    //Xoá chọn
                    notesAdapter.clearSelection()
                }
                .setNegativeButton("Huỷ") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        binding.searchEditText.addTextChangedListener {text ->
            filterNotes(text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        allNotes = db.getAllNotes()
        notesAdapter.refreshData(db.getAllNotes())
    }
    private fun filterNotes(query: String) {
        val filteredNotes = if (query.isEmpty()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
        notesAdapter.refreshData(filteredNotes as MutableList<Note>, query)
    }
}