package com.example.noteapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.databinding.FragmentTasksBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksFragment : Fragment() {

    private lateinit var binding: FragmentTasksBinding
    private lateinit var db: NotesDatabseHelper
    private lateinit var taskAdapter: TaskAdapter
    private var taskList: MutableList<Task> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = NotesDatabseHelper(requireContext())

        setupRecyclerView()
        loadTasks()

        binding.addTaskButton.setOnClickListener {
            openTaskDialog(onSave = { task ->
                db.addTask(task)
                loadTasks()
                Toast.makeText(requireContext(), "Đã thêm nhiệm vụ", Toast.LENGTH_SHORT).show()
            })
        }

    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = taskList,
            context = requireContext(),
            onEdit = { task ->
                openTaskDialog(task) { updated ->
                    db.updateTask(updated)
                    loadTasks()
                    Toast.makeText(requireContext(), "Đã sửa nhiệm vụ", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = { task ->
                db.deleteTask(task.id)
                loadTasks()
                Toast.makeText(requireContext(), "Đã xoá nhiệm vụ", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTask.adapter = taskAdapter
    }

    private fun loadTasks() {
        taskList = db.getAllTasks()
        taskAdapter.updateData(taskList)
        taskAdapter.sortTasks()
    }


    private fun openTaskDialog(task: Task? = null, onSave: (Task) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_task, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

//        val cbDone = dialogView.findViewById<CheckBox>(R.id.cbDone)
        val edtTaskName = dialogView.findViewById<EditText>(R.id.edtTaskName)
        val btnReminder = dialogView.findViewById<Button>(R.id.btnReminder)
        val btnDone = dialogView.findViewById<Button>(R.id.btnDone)
        val tvDeadline = dialogView.findViewById<TextView>(R.id.tvDeadline)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        var selectedDeadline: String? = null

        // Nếu sửa → load dữ liệu cũ
        if (task != null) {
            edtTaskName.setText(task.name)
//            cbDone.isChecked = task.isDone == 1
            selectedDeadline = task.deadline
            tvDeadline.text = task.deadline ?: "Không có deadline"
        }

        // Xử lý chọn deadline
        btnReminder.setOnClickListener {
            showDateTimePicker { datetime ->
                selectedDeadline = datetime
                tvDeadline.text = "Hạn: $datetime"
            }
        }

        btnDone.setOnClickListener {
            val newTask = Task(
                id = task?.id ?: 0,
                name = edtTaskName.text.toString(),
//                isDone = if (cbDone.isChecked) 1 else 0,
                deadline = selectedDeadline
            )

            onSave(newTask)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showDateTimePicker(onSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        // DatePicker
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                // TimePicker sau khi chọn ngày
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)

                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val finalTime = format.format(calendar.time)

                        onSelected(finalTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


}
