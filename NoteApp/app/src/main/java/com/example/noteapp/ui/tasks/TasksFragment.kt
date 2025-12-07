package com.example.noteapp.ui.tasks

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.notification.DeadlineReceiver
import com.example.noteapp.R
import com.example.noteapp.adapter.TaskAdapter
import com.example.noteapp.database.NotesDatabseHelper
import com.example.noteapp.databinding.FragmentTasksBinding
import com.example.noteapp.model.Task
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
                val newId = db.addTask(task) // newId là Int
                val savedTask = task.copy(id = newId.toInt())
                scheduleDeadlineNotification(savedTask)
                loadTasks()
                Toast.makeText(requireContext(), "Đã thêm nhiệm vụ", Toast.LENGTH_SHORT).show()
            })
        }
        requestExactAlarmPermission()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = taskList,
            context = requireContext(),
            onEdit = { task ->
                openTaskDialog(task) { updated ->
                    cancelDeadlineNotification(updated.id)
                    scheduleDeadlineNotification(updated)

                    db.updateTask(updated)
                    loadTasks()
                    Toast.makeText(requireContext(), "Đã sửa nhiệm vụ", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = { task ->
                cancelDeadlineNotification(task.id)
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

        val edtTaskName = dialogView.findViewById<EditText>(R.id.edtTaskName)
        val btnReminder = dialogView.findViewById<Button>(R.id.btnReminder)
        val btnDone = dialogView.findViewById<Button>(R.id.btnDone)
        val tvDeadline = dialogView.findViewById<TextView>(R.id.tvDeadline)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        var selectedDeadline: String? = null

        // Nếu sửa → load dữ liệu cũ
        if (task != null) {
            edtTaskName.setText(task.name)
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

                        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
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
    private fun scheduleDeadlineNotification(task: Task) {
        if (task.deadline.isNullOrEmpty() || task.isDone == 1) return

        // parse deadline string ("dd/MM/yyyy HH:mm") => time millis
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val date = try { sdf.parse(task.deadline) } catch (e: Exception) { null } ?: return
        val triggerAtMillis = date.time

        val intent = Intent(requireContext(), DeadlineReceiver::class.java).apply {
            putExtra("taskName", task.name)
            putExtra("taskId", task.id)
        }

        val pending = PendingIntent.getBroadcast(
            requireContext(),
            task.id, // unique per task
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Nếu trigger time đã qua, bạn có thể quyết định: bắn ngay hoặc bỏ.
        if (triggerAtMillis <= System.currentTimeMillis()) {
            // bắn ngay
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500L, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }
    //Hàm huỷ thông báo
    private fun cancelDeadlineNotification(taskId: Int) {
        val intent = Intent(requireContext(), DeadlineReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            requireContext(),
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(AlarmManager::class.java)

            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
        }
    }
}