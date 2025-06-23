package com.rnd.todo

import androidx.room.*
import kotlinx.coroutines.flow.Flow // For reactive updates

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items ORDER BY id DESC")
    fun getAllTodoItems(): Flow<List<TodoItem>> // Use Flow for observable queries

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if item with same ID exists
    suspend fun insertTodoItem(item: TodoItem)

    @Update
    suspend fun updateTodoItem(item: TodoItem)

    @Delete
    suspend fun deleteTodoItem(item: TodoItem)

    // Optional: If you need to delete by ID directly
    @Query("DELETE FROM todo_items WHERE id = :itemId")
    suspend fun deleteTodoItemById(itemId: Int)

    // Optional: Get a single item by ID (e.g., for editing)
    @Query("SELECT * FROM todo_items WHERE id = :itemId")
    suspend fun getTodoItemById(itemId: Int): TodoItem?
}