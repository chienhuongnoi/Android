package com.example.noteapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    var tasks: MutableList<Task>,
    private val context: Context,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private val db = NotesDatabseHelper(context)

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskName: TextView = itemView.findViewById(R.id.taskNameTextView)
        val chkCheckBox: CheckBox = itemView.findViewById(R.id.chkCheckBox)
        val tvStatus: TextView = itemView.findViewById(R.id.statusTextView)
        val tvDate: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvTaskName.text = task.name
        holder.chkCheckBox.isChecked = task.isDone == 1
        holder.tvDate.text = task.deadline ?: ""
        val deadlineMillis = parseDeadlineToMillis(task.deadline)
        val now = System.currentTimeMillis()
        //log
        println(task.deadline)
// 1. Xử lý trạng thái
        when {
            task.isDone == 1 -> {
                holder.tvStatus.text = "Đã hoàn thành"
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))

                holder.tvDate.text = ""
            }

            deadlineMillis != null && deadlineMillis < now -> {
                holder.tvStatus.text = "Quá hạn"
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"))

                holder.tvDate.text = "Hạn: ${formatDeadline(task.deadline!!)}"
            }

            task.deadline.isNullOrEmpty() -> {
                holder.tvStatus.text = "Chưa hoàn thành"
                holder.tvStatus.setTextColor(Color.parseColor("#888888"))

                holder.tvDate.text = ""
            }

            else -> {
                holder.tvStatus.text = "Chưa hoàn thành"
                holder.tvStatus.setTextColor(Color.parseColor("#FFC107"))

                holder.tvDate.text = "Hạn: ${formatDeadline(task.deadline!!)}"
            }
        }

        // Thay đổi style nếu hoàn thành: mờ + gạch ngang
        if (task.isDone == 1) {
            holder.tvTaskName.paintFlags =
                holder.tvTaskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTaskName.alpha = 0.5f
            holder.tvDate.alpha = 0.5f
            holder.chkCheckBox.alpha = 0.5f
        } else {
            holder.tvTaskName.paintFlags =
                holder.tvTaskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTaskName.alpha = 1f
            holder.tvDate.alpha = 1f
            holder.chkCheckBox.alpha = 1f
        }

        holder.itemView.setOnClickListener {
            onEdit(task)
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xoá nhiệm vụ")
                .setMessage("Bạn chắc chắn muốn xoá \"${task.name}\"?")
                .setPositiveButton("Xoá") { _, _ ->
                    onDelete(task)
                }
                .setNegativeButton("Hủy", null)
                .show()
            true
        }
        holder.chkCheckBox.setOnClickListener {
            val newIsDone = if (holder.chkCheckBox.isChecked) 1 else 0
            db.updateTask(task.copy(isDone = newIsDone))
            tasks[position] = task.copy(isDone = newIsDone)
            notifyItemChanged(position)
            sortTasks()
        }
    }

    fun updateData(newList: MutableList<Task>) {
        tasks = newList
        notifyDataSetChanged()
    }
    fun sortTasks() {
        tasks.sortWith(compareBy<Task>(
            // 1. Ưu tiên chưa hoàn thành
            { it.isDone },

            // 2. Ưu tiên các task có deadline
            { it.deadline == null },

            // 3. Cuối cùng sort theo deadline tăng dần
            {
                if (it.deadline == null) Long.MAX_VALUE
                else convertDeadlineToMillis(it.deadline!!)
            }
        ))

        notifyDataSetChanged()
    }
    private fun formatDeadline(deadline: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(deadline)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            deadline // fallback nếu lỗi format
        }
    }
    private fun convertDeadlineToMillis(deadline: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.parse(deadline)?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
    fun parseDeadlineToMillis(deadline: String?): Long? {
        if (deadline.isNullOrEmpty()) return null

        val formats = arrayOf(
            "yyyy-MM-dd HH:mm",
            "dd/MM/yyyy HH:mm",
            "MM/dd/yyyy HH:mm",
            "yyyy/MM/dd HH:mm"
        )

        for (pattern in formats) {
            try {
                val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                dateFormat.isLenient = false // Không tự động fix lỗi
                val date = dateFormat.parse(deadline)
                return date?.time
            } catch (e: Exception) {
                continue
            }
        }

        return null
    }
}
