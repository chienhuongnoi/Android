package com.example.noteapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale

class NotesAdapter(var notes: MutableList<Note>, context: Context) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    private val db: NotesDatabseHelper = NotesDatabseHelper(context)
    var selectedItems = mutableListOf<Int>()
    var isMultiSelectMode = false
    var onSelectionChanged: ((Int) -> Unit)? = null
    var searchQuery: String = ""
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnLongClickListener {
                if (!isMultiSelectMode) {
                    isMultiSelectMode = true
                    toggleSelection(adapterPosition)
                    notifyDataSetChanged()
                }
                true
            }
        }
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val selectIcon: ImageView = itemView.findViewById(R.id.selectIcon)
        val btnPin: ImageView = itemView.findViewById(R.id.btnPin)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int
    ) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        holder.itemView.setOnClickListener {
            if (isMultiSelectMode) {
                toggleSelection(position)
            } else {
                val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
                    putExtra("note_id", note.id)
                }
                holder.itemView.context.startActivity(intent)
            }
        }
        holder.dateTextView.text = formatDate(note.createdAt)

        //Ấn nút ghim
        holder.btnPin.setOnClickListener {
            val newPinnedValue = if (note.isPinned == 0) 1 else 0
            db.togglePin(note.id, newPinnedValue)
            note.isPinned = newPinnedValue
            notes.sortWith(
                compareByDescending<Note> { it.isPinned }
                    .thenByDescending { it.createdAt }
            )
            notifyDataSetChanged()
        }
        holder.btnPin.setImageResource(
            if (note.isPinned == 1) R.drawable.ic_pin_filled
            else R.drawable.ic_pin
        )

        //Highlight màu chữ khi tìm kiếm
        holder.titleTextView.text = highlight(note.title, searchQuery)
        holder.contentTextView.text = highlight(note.content, searchQuery)
        //Hiển thị chế độ chọn
        if(isMultiSelectMode){
            holder.selectIcon.visibility = View.VISIBLE
            holder.btnPin.visibility = View.GONE
            if(selectedItems.contains(position)){
                //Khi được chọn
                holder.selectIcon.setImageResource(R.drawable.outline_check_circle_24)
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.selected_item_bg)
                )
            }
            else{
                //Khi không được chọn
                holder.selectIcon.setImageResource(R.drawable.outline_circle_24)
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.white)
                )
            }
        }
        else{
            //Khi thoát chế độ chọn
            holder.selectIcon.visibility = View.GONE
            holder.btnPin.visibility = View.VISIBLE
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }
    }
    //Hàm cập nhật lại dữ liệu
    fun refreshData(newNotes: MutableList<Note>, query: String = ""){
        notes = newNotes
        searchQuery = query
        notifyDataSetChanged()
    }
    //Hàm ử lý chọn nhiều mục
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }

        notifyItemChanged(position)
        onSelectionChanged?.invoke(selectedItems.size)

        // Nếu hết chọn -> thoát chế độ
        if (selectedItems.isEmpty()) {
            isMultiSelectMode = false
            notifyDataSetChanged()
            onSelectionChanged?.invoke(0)
        }
    }
    //Hàm xoá chọn nhiều mục
    fun clearSelection(){
        selectedItems.clear()
        isMultiSelectMode = false
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }
    //Hàm nổi bật màu chữ khi tìm kiếm
    private fun highlight(text: String, query: String): SpannableString {
        val spannable = SpannableString(text)
        if (query.isEmpty()) return spannable

        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        var start = lowerText.indexOf(lowerQuery)
        while (start >= 0) {
            val end = start + query.length
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#FFC94D")), //
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = lowerText.indexOf(lowerQuery, end)
        }

        return spannable
    }
    fun formatDate(time: String): String {
        val t = time.toLong()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(t))
    }
}