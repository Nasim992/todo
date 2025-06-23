package com.rnd.todo

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


// Data class to hold categorized tasks
data class CategorizedTasks(
    val overdue: List<TodoItem> = emptyList(),
    val today: List<TodoItem> = emptyList(),
    val tomorrow: List<TodoItem> = emptyList(),
    val thisWeek: List<TodoItem> = emptyList(),
    val upcoming: List<TodoItem> = emptyList(), // For tasks beyond this week or without due dates
    val completed: List<TodoItem> = emptyList()
)


class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val todoDao: TodoDao
    var editingItem = mutableStateOf<TodoItem?>(null)
        private set

    // Expose categorized tasks
    val categorizedTasks: StateFlow<CategorizedTasks>


    init {
        val database = AppDatabase.getDatabase(application)
        todoDao = database.todoDao()

        // Combine all items and then categorize them
        categorizedTasks = todoDao.getAllTodoItems() // Assuming this fetches all, including completed
            .map { allItems ->
                val todayStart = DateUtils.getTodayStartMillis()
                val todayEnd = DateUtils.getTodayEndMillis()
                val tomorrowStart = DateUtils.getTomorrowStartMillis()
                val tomorrowEnd = DateUtils.getTomorrowEndMillis()
                val startOfWeek = DateUtils.getStartOfWeekMillis() // Assuming week starts based on current locale
                val endOfWeek = DateUtils.getEndOfWeekMillis()

                val activeItems = allItems.filter { !it.isCompleted }
                val completedItems = allItems.filter { it.isCompleted }

                val overdue = activeItems.filter { DateUtils.isOverdue(it.dueDate, todayStart) }
                val today = activeItems.filter { DateUtils.isToday(it.dueDate, todayStart, todayEnd) }
                val tomorrow = activeItems.filter { DateUtils.isTomorrow(it.dueDate, tomorrowStart, tomorrowEnd) }

                // "This Week" excluding today and tomorrow to avoid duplicates if you list them separately
                // Or you can make "This Week" inclusive and then filter out in the UI if needed
                val thisWeek = activeItems.filter {
                    DateUtils.isThisWeek(it.dueDate, todayStart, endOfWeek) &&
                            !DateUtils.isToday(it.dueDate, todayStart, todayEnd) &&
                            !DateUtils.isTomorrow(it.dueDate, tomorrowStart, tomorrowEnd) &&
                            !DateUtils.isOverdue(it.dueDate, todayStart) // Also exclude overdue from this week
                }.sortedBy { it.dueDate }

                // Upcoming tasks are those active tasks that are not overdue, today, tomorrow, or this week.
                // This includes tasks with due dates beyond this week OR tasks with no due dates.
                val upcoming = activeItems.filter { item ->
                    item.dueDate == null || // Include tasks with no due date
                            (item.dueDate!! > endOfWeek && // Or tasks beyond this week
                                    !overdue.contains(item) &&
                                    !today.contains(item) &&
                                    !tomorrow.contains(item) &&
                                    !thisWeek.contains(item))
                }.sortedBy { it.dueDate }


                CategorizedTasks(
                    overdue = overdue.sortedBy { it.dueDate },
                    today = today.sortedBy { it.dueDate },
                    tomorrow = tomorrow.sortedBy { it.dueDate },
                    thisWeek = thisWeek, // Already sorted
                    upcoming = upcoming, // Already sorted
                    completed = completedItems.sortedByDescending { it.updateDate } // Recently completed first
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CategorizedTasks() // Initial empty state
            )
    }

    fun addTodoItem(text: String, dueDateMillis: Long? = null) {
        if (text.isNotBlank()) {
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                val newItem = TodoItem(
                    text = text,
                    isCompleted = false,
                    creationDate = currentTime,
                    updateDate = currentTime,
                    dueDate = dueDateMillis // Set the due date
                )
                todoDao.insertTodoItem(newItem)
            }
        }
    }


    fun toggleTodoItem(item: TodoItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                isCompleted = !item.isCompleted,
                updateDate = System.currentTimeMillis()
            )
            todoDao.updateTodoItem(updatedItem)
        }
    }


    fun removeTodoItem(item: TodoItem) {
        viewModelScope.launch {
            todoDao.deleteTodoItem(item)
        }
    }

    fun startEditing(item: TodoItem) {
        editingItem.value = item
    }

    fun onEditDone() {
        editingItem.value = null
    }

    // When updating, you might also update the dueDate
    fun updateTodoItem(updatedItemFromDialog: TodoItem /*, newDueDateMillis: Long? = null*/) {
        viewModelScope.launch {
            val currentItemInDb = todoDao.getTodoItemById(updatedItemFromDialog.id)
            currentItemInDb?.let {
                val fullyUpdatedItem = it.copy(
                    text = updatedItemFromDialog.text,
                    isCompleted = updatedItemFromDialog.isCompleted, // if editable in dialog
                    updateDate = System.currentTimeMillis(),
                    dueDate = updatedItemFromDialog.dueDate // Assume dueDate is part of updatedItemFromDialog
                    // or pass as separate param: newDueDateMillis
                )
                todoDao.updateTodoItem(fullyUpdatedItem)
            }
        }
        onEditDone()
    }

}