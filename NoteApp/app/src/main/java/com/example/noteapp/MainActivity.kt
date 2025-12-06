package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NotesDatabseHelper
    private lateinit var notesAdapter: NotesAdapter
    lateinit var catAdapter: CategoryHorizontalAdapter

    lateinit var allNotes: List<Note>

    private var currentCategoryId : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = NotesDatabseHelper(this)
        notesAdapter = NotesAdapter(db.getAllNotes(), this)
        allNotes = db.getAllNotes()

        //Recycler view
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        //Nút thêm
        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("selectedCategoryId", currentCategoryId)
            startActivity(intent)
        }
        //Chế độ đa chọn
        notesAdapter.onSelectionChanged = {count ->
            if (count > 0){
                binding.closeMultiSelectButton.visibility = View.VISIBLE
                binding.addButton.visibility = View.GONE
                binding.deleteSelectedItemButton.visibility = View.VISIBLE
                binding.notesHeading.text = "Đã chọn $count mục"
            }
            else{
                binding.closeMultiSelectButton.visibility = View.GONE
                binding.addButton.visibility = View.VISIBLE
                binding.notesHeading.text = "Notes."
                binding.deleteSelectedItemButton.visibility = View.GONE
            }
        }
        //Nút đóng chế độ đa chọn
        binding.closeMultiSelectButton.setOnClickListener {
            notesAdapter.clearSelection()
        }
        //Xóa nhiều mục
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
        //tìm kiếm
        binding.searchEditText.addTextChangedListener {text ->
            filterNotes(text.toString())
        }
        //Điều hướng sang màn hình danh mục
        binding.categoryButton.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivityForResult(intent, 1)
        }
        // Lấy danh mục + thêm ALL ở đầu
        val categories = mutableListOf<Category>()
        categories.add(Category(-1, "Tất cả"))
        categories.addAll(db.getAllCategories())

        //Recycler view danh mục
        catAdapter = CategoryHorizontalAdapter(categories) { cat ->
            currentCategoryId = cat.id
            if (cat.id == -1) {   // Lấy all notes
                notesAdapter.refreshData(db.getAllNotes())
                //cập nhật allnote để lọc
                allNotes = db.getAllNotes()
            } else {
                notesAdapter.refreshData(db.getNotesByCategory(cat.id))
                //cập nật allnote để lọc
                allNotes = db.getNotesByCategory(cat.id)
            }
        }

        //Hiển thị danh mục ở mainactivity
        binding.rvCategoriesHorizontal.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategoriesHorizontal.adapter = catAdapter
    }

    override fun onResume() {
        super.onResume()
        allNotes = db.getNotesByCategory(currentCategoryId)
        notesAdapter.refreshData(db.getNotesByCategory(currentCategoryId))
        loadCategories()
    }
    //Hàm lọc note theo tiêu đề hoặc nội dung
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
    //Haàm load lại danh mục
    private fun loadCategories() {
        val categories = db.getAllCategories().toMutableList()

        categories.add(0, Category(-1, "Tất cả"))
        catAdapter.updateData(categories)
    }
}