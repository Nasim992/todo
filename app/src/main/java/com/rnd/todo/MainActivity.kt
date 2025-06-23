package com.rnd.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // Import Edit icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // Import Dialog
import com.rnd.todo.ui.theme.MyApplicationTheme
import kotlin.text.format
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : ComponentActivity() {
    private val todoViewModel: TodoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TodoListScreen(todoViewModel)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: TodoViewModel) {
    // Collect the StateFlow as Composable State
    val todoItems by viewModel.todoItems.collectAsState()
    val itemToEdit by viewModel.editingItem

    Scaffold(
        // ... (rest of Scaffold remains the same) ...
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TodoInput(onAddItem = { text ->
                viewModel.addTodoItem(text)
            })

            Spacer(modifier = Modifier.height(16.dp))

            if (todoItems.isEmpty()) {
                Text(
                    text = "No tasks yet. Add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = todoItems, // Use the collected state
                        key = { it.id }
                    ) { item ->
                        TodoListItem(
                            item = item,
                            onToggle = { viewModel.toggleTodoItem(item) },
                            onDelete = { viewModel.removeTodoItem(item) },
                            onEdit = { viewModel.startEditing(item) }
                        )
                        Divider()
                    }
                }
            }
        }

        itemToEdit?.let { currentItem ->
            EditTodoDialog(
                item = currentItem,
                onDismiss = { viewModel.onEditDone() },
                onSave = { updatedText ->
                    viewModel.updateTodoItem(currentItem.copy(text = updatedText))
                }
            )
        }
    }
}
@Composable
fun TodoListItem(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // .clickable(onClick = onToggle) // If you want the whole row clickable
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onToggle() } // Toggle is primary action
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) { // Use a Column for text and dates
            Text(
                text = item.text,
                style = if (item.isCompleted) {
                    MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                modifier = Modifier.clickable(onClick = onToggle) // Make text clickable for toggle
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Updated: ${formatDate(item.updateDate)}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp, // Smaller font for dates
                color = MaterialTheme.colorScheme.onSurfaceVariant // Subtler color
            )
            // Optionally show creationDate too
            // Text(
            //     text = "Created: ${formatDate(item.creationDate)}",
            //     style = MaterialTheme.typography.bodySmall,
            //     fontSize = 10.sp,
            //     color = MaterialTheme.colorScheme.onSurfaceVariant
            // )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Task")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Task")
        }
    }
}

fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault()) // Now uses java.util.Locale.getDefault()
        val netDate = Date(timestamp) // This should be java.util.Date
        sdf.format(netDate)
    } catch (e: Exception) {
        // It's generally better to log the exception or handle it more gracefully
        // e.printStackTrace() // For debugging
        "Error formatting date" // Return a placeholder or re-throw
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoDialog(
    item: TodoItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editText by remember(item) { mutableStateOf(TextFieldValue(item.text)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Task", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    label = { Text("Task description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (editText.text.isNotBlank()) {
                                onSave(editText.text)
                            }
                        },
                        enabled = editText.text.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
// TodoInput Composable remains the same
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoInput(onAddItem: (String) -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("New Task") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (textState.text.isNotBlank()) {
                    onAddItem(textState.text)
                    textState = TextFieldValue("") // Clear the input field
                }
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Task")
        }
    }
}
// --- Previews ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
@Preview(showBackground = true)
fun DefaultPreview() {
    MyApplicationTheme {
        // For preview, we can't easily instantiate the ViewModel with a real DB.
        // So, either mock it or pass data directly to the screen.
        // Let's create a dummy list for the preview.
        val previewItems = listOf(
            TodoItem(id = 1, text = "Buy groceries (Preview)", isCompleted = true),
            TodoItem(id = 2, text = "Walk the dog (Preview)", isCompleted = false)
        )
        val itemToEdit = remember { mutableStateOf<TodoItem?>(null) } // Dummy state for editing

        // Simplified TodoListScreen structure for preview without full ViewModel
        Scaffold(
            topBar = { TopAppBar(title = { Text("Todo List (Preview)") }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                TodoInput(onAddItem = { /* Dummy action */ })
                Spacer(modifier = Modifier.height(16.dp))
                if (previewItems.isEmpty()) {
                    Text("No tasks yet. (Preview)")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(items = previewItems, key = { it.id }) { item ->
                            TodoListItem(
                                item = item,
                                onToggle = { /* Dummy action */ },
                                onDelete = { /* Dummy action */ },
                                onEdit = { itemToEdit.value = item }
                            )
                            Divider()
                        }
                    }
                }
            }
            itemToEdit.value?.let { currentItem ->
                EditTodoDialog(
                    item = currentItem,
                    onDismiss = { itemToEdit.value = null },
                    onSave = { _ -> itemToEdit.value = null  /* Dummy save */ }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun EditTodoDialogPreview() {
    MyApplicationTheme {
        EditTodoDialog(
            item = TodoItem(1, "Existing task to edit", false),
            onDismiss = {},
            onSave = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun TodoListItemPreview() {
    MyApplicationTheme {
        TodoListItem(
            item = TodoItem(1, "Sample Task", false),
            onToggle = {},
            onDelete = {},
            onEdit = {} // Add onEdit for preview
        )
    }
}