package com.rnd.todo

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val todoDao: TodoDao
    val todoItems: StateFlow<List<TodoItem>>

    var editingItem = mutableStateOf<TodoItem?>(null)
        private set

    init {
        val database = AppDatabase.getDatabase(application)
        todoDao = database.todoDao()
        todoItems = todoDao.getAllTodoItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addTodoItem(text: String) {
        if (text.isNotBlank()) {
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                // creationDate will use its default value from TodoItem
                // updateDate will also use its default value, which is fine for a new item
                val newItem = TodoItem(
                    text = text,
                    isCompleted = false,
                    creationDate = currentTime, // Explicitly set or rely on default
                    updateDate = currentTime    // Explicitly set or rely on default
                )
                todoDao.insertTodoItem(newItem)
            }
        }
    }

    fun toggleTodoItem(item: TodoItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(
                isCompleted = !item.isCompleted,
                updateDate = System.currentTimeMillis() // Update the updateDate
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

    fun updateTodoItem(updatedItemFromDialog: TodoItem) { // Parameter name changed for clarity
        viewModelScope.launch {
            // Ensure we are using the original item's ID and creationDate
            // and only updating text, completion status, and updateDate
            val currentItemInDb = todoDao.getTodoItemById(updatedItemFromDialog.id) // Fetch to ensure integrity
            currentItemInDb?.let {
                val fullyUpdatedItem = it.copy(
                    text = updatedItemFromDialog.text, // Text from dialog
                    isCompleted = updatedItemFromDialog.isCompleted, // Completion status from dialog/original
                    updateDate = System.currentTimeMillis() // Set new update date
                )
                todoDao.updateTodoItem(fullyUpdatedItem)
            }
        }
        onEditDone()
    }
}