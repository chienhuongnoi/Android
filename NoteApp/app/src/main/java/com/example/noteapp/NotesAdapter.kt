package com.example.noteapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(var notes: MutableList<Note>, context: Context) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    private val db: NotesDatabseHelper = NotesDatabseHelper(context)
    var selectedItems = mutableListOf<Int>()
    var isMultiSelectMode = false
    var onSelectionChanged: ((Int) -> Unit)? = null
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
//        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
//        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val selectIcon: ImageView = itemView.findViewById(R.id.selectIcon)

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

//        holder.updateButton.setOnClickListener {
//            val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
//                putExtra("note_id", note.id)
//            }
//            holder.itemView.context.startActivity(intent)
//        }
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
//        holder.deleteButton.setOnClickListener {
//            db.deleteNote(note.id)
//            refreshData(db.getAllNotes())
//            Toast.makeText(holder.itemView.context, "Note deleted", Toast.LENGTH_SHORT).show()
//        }
        if(isMultiSelectMode){
            holder.selectIcon.visibility = View.VISIBLE
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
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }
    }
    fun refreshData(newNotes: MutableList<Note>){
        notes = newNotes
        notifyDataSetChanged()
    }
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
    fun clearSelection(){
        selectedItems.clear()
        isMultiSelectMode = false
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }
}