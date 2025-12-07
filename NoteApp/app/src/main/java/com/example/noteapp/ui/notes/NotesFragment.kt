package com.example.noteapp.ui.notes

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.ui.categories.CategoriesActivity
import com.example.noteapp.adapter.CategoryHorizontalAdapter
import com.example.noteapp.adapter.NotesAdapter
import com.example.noteapp.database.NotesDatabseHelper
import com.example.noteapp.databinding.FragmentNotesBinding
import com.example.noteapp.model.Category
import com.example.noteapp.model.Note

class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding
    private lateinit var db: NotesDatabseHelper
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var catAdapter: CategoryHorizontalAdapter

    private var allNotes: List<Note> = listOf()
    private var currentCategoryId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = NotesDatabseHelper(requireContext())

        allNotes = db.getAllNotes()
        notesAdapter = NotesAdapter(allNotes.toMutableList(), requireContext())

        // RecyclerView ghi chú
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notesRecyclerView.adapter = notesAdapter

        // Nút thêm
        binding.addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddNoteActivity::class.java)
            intent.putExtra("selectedCategoryId", currentCategoryId)
            startActivity(intent)
        }

        // Chế độ đa chọn
        notesAdapter.onSelectionChanged = { count ->
            if (count > 0) {
                binding.closeMultiSelectButton.visibility = View.VISIBLE
                binding.addButton.visibility = View.GONE
                binding.deleteSelectedItemButton.visibility = View.VISIBLE
                binding.notesHeading.text = "Đã chọn $count mục"
            } else {
                binding.closeMultiSelectButton.visibility = View.GONE
                binding.addButton.visibility = View.VISIBLE
                binding.notesHeading.text = "Ghi chú."
                binding.deleteSelectedItemButton.visibility = View.GONE
            }
        }

        // Đóng đa chọn
        binding.closeMultiSelectButton.setOnClickListener {
            notesAdapter.clearSelection()
        }

        // Xóa nhiều ghi chú
        binding.deleteSelectedItemButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá các mục đã chọn?")
                .setPositiveButton("Xoá") { _, _ ->
                    val selectedPositions = notesAdapter.selectedItems.toList().sortedDescending()

                    for (index in selectedPositions) {
                        val noteId = notesAdapter.notes[index].id
                        db.deleteNote(noteId)
                    }

                    allNotes = db.getAllNotes()
                    notesAdapter.refreshData(allNotes.toMutableList())
                    notesAdapter.clearSelection()
                }
                .setNegativeButton("Huỷ", null)
                .show()
        }

        // Tìm kiếm
        binding.searchEditText.addTextChangedListener { text ->
            filterNotes(text.toString())
        }

        // Đi đến Category Activity
        binding.categoryButton.setOnClickListener {
            val intent = Intent(requireContext(), CategoriesActivity::class.java)
            startActivity(intent)
        }

        // Danh mục
        val categories = mutableListOf(Category(-1, "Tất cả"))
        categories.addAll(db.getAllCategories())

        catAdapter = CategoryHorizontalAdapter(categories) { cat ->
            currentCategoryId = cat.id

            if (cat.id == -1) {
                allNotes = db.getAllNotes()
            } else {
                allNotes = db.getNotesByCategory(cat.id)
            }

            notesAdapter.refreshData(allNotes.toMutableList())
        }

        binding.rvCategoriesHorizontal.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategoriesHorizontal.adapter = catAdapter
    }

    override fun onResume() {
        super.onResume()
        allNotes = db.getNotesByCategory(currentCategoryId)
        notesAdapter.refreshData(allNotes.toMutableList())
        loadCategories()
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
        val sortedNotes = filteredNotes.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.createdAt }
        )
        notesAdapter.refreshData(sortedNotes.toMutableList(), query)
    }
    private fun loadCategories() {
        val categories = db.getAllCategories().toMutableList()

        categories.add(0, Category(-1, "Tất cả"))
        catAdapter.updateData(categories)
    }
}

