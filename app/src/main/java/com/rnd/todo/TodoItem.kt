package com.rnd.todo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "todo_items") // Define the table name
data class TodoItem(
    @PrimaryKey(autoGenerate = true) // Room will auto-generate IDs
    val id: Int = 0, // Default value needed for autoGenerate
    val text: String,
    var isCompleted: Boolean = false,
    // New fields
    @ColumnInfo(name = "creation_date") // Explicit column name
    val creationDate: Long = System.currentTimeMillis(), // Default to current time on creation
    @ColumnInfo(name = "update_date")
    var updateDate: Long = System.currentTimeMillis() // Default to current time, will be updated
)