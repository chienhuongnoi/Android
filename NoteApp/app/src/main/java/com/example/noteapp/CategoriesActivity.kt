package com.example.noteapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {
    private lateinit var adapter: CategoryAdapter
    private lateinit var db: NotesDatabseHelper
    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NotesDatabseHelper(this)


        adapter = CategoryAdapter(db.getAllCategories()) { category ->
            val intent = Intent()
            intent.putExtra("categoryId", category.id)
            intent.putExtra("categoryName", category.name)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter

        adapter.setOnDeleteListener { category ->
            AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa '${category.name}'?")
                .setPositiveButton("Xóa") { _, _ ->
                    db.deleteCategory(category.id)
                    db.deleteNotesByCategory(category.id)
                    adapter.updateData(db.getAllCategories())
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        binding.btnAddCategory.setOnClickListener {
            val input = EditText(this)
            input.hint = "Tên danh mục"

            AlertDialog.Builder(this)
                .setTitle("Thêm danh mục")
                .setView(input)
                .setPositiveButton("Thêm") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) {
                        db.insertCategory(Category(0, name))
                        adapter.updateData(db.getAllCategories())
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
}