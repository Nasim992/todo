package com.rnd.todo

import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.util.*
import android.app.DatePickerDialog // For the Android View system dialog
import android.content.res.Configuration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoListScreen(viewModel: TodoViewModel) {
    val categorizedTasks by viewModel.categorizedTasks.collectAsState()
    val itemToEdit by viewModel.editingItem

    Scaffold(
        topBar = { TopAppBar(title = { Text("Todo List") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Apply horizontal padding once here
        ) {
            // TODO: Update TodoInput to allow setting due dates
            TodoInput(
                onAddItem = { text, dueDate -> // <<< CORRECTED: Lambda now accepts 'text' and 'dueDate'
                    viewModel.addTodoItem(text, dueDate) // Pass both to the ViewModel
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (categorizedTasks.overdue.isEmpty() &&
                categorizedTasks.today.isEmpty() &&
                categorizedTasks.tomorrow.isEmpty() &&
                categorizedTasks.thisWeek.isEmpty() &&
                categorizedTasks.upcoming.isEmpty() &&
                categorizedTasks.completed.isEmpty()
            ) {
                Text(
                    text = "No tasks yet. Add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {

                    // Function to simplify section creation
                    fun sectionItems(title: String, items: List<TodoItem>) {
                        if (items.isNotEmpty()) {
                            stickyHeader {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.9f
                                            )
                                        )
                                        .padding(vertical = 8.dp, horizontal = 16.dp) // Match column padding
                                )
                            }
                            items(items, key = { "task-${it.id}" }) { item ->
                                TodoListItem(
                                    item = item,
                                    onToggle = { viewModel.toggleTodoItem(item) },
                                    onDelete = { viewModel.removeTodoItem(item) },
                                    onEdit = { viewModel.startEditing(item) }
                                )
                                HorizontalDivider()
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) } // Space after section
                        }
                    }

                    sectionItems("Overdue", categorizedTasks.overdue)
                    sectionItems("Today", categorizedTasks.today)
                    sectionItems("Tomorrow", categorizedTasks.tomorrow)
                    sectionItems("This Week", categorizedTasks.thisWeek)
                    sectionItems("Upcoming / No Due Date", categorizedTasks.upcoming) // Combined for simplicity
                    sectionItems("Completed", categorizedTasks.completed)
                }
            }
        }

// Inside TodoListScreen composable, where itemToEdit is handled:
        itemToEdit?.let { currentItem ->
            EditTodoDialog(
                item = currentItem,
                onDismiss = { viewModel.onEditDone() },
                onSave = { originalItem, updatedText, updatedDueDate -> // <<< MODIFIED
                    viewModel.updateTodoItem(
                        originalItem.copy( // Use copy to preserve ID and other properties
                            text = updatedText,
                            dueDate = updatedDueDate
                            // updateDate will be handled in the ViewModel or repository
                        )
                    )
                }
            )
        }
    }
}
fun formatDueDate(timestamp: Long?): String? {
    if (timestamp == null) return null
    return try {
        // Example format, adjust as needed
        val sdf = SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Invalid Date" // Or handle error appropriately
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
            // Display Due Date
            item.dueDate?.let { dueDateMillis ->
                formatDueDate(dueDateMillis)?.let { formattedDate ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Due: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else if (DateUtils.isOverdue(item.dueDate) && !item.isCompleted) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Updated: ${formatDate(item.updateDate)}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp, // Smaller font for dates
                color = MaterialTheme.colorScheme.onSurfaceVariant // Subtler color
            )
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
    // MODIFIED: onSave now takes the original item and the updated text & due date
    onSave: (originalItem: TodoItem, updatedText: String, updatedDueDate: Long?) -> Unit
) {
    var editText by remember(item.text) { mutableStateOf(TextFieldValue(item.text)) }
    // State for the due date being edited in the dialog
    var selectedDueDateInDialog by remember(item.dueDate) { mutableStateOf(item.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun formatDateDisplay(timestamp: Long?): String {
        return if (timestamp == null) {
            "Select Due Date"
        } else {
            SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(Date(timestamp))
        }
    }

    // DatePickerDialog logic (similar to TodoInput)
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        selectedDueDateInDialog?.let {
            calendar.timeInMillis = it
        }
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            // OnDateSetListener:
            // The first parameter type is android.widget.DatePicker
            { _, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth) // Month is 0-indexed
                    set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0) // Normalize time
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDueDateInDialog = newCalendar.timeInMillis
                showDatePicker = false // Hide picker after selection
            },
            initialYear,
            initialMonth,
            initialDay
        )
        datePickerDialog.setOnDismissListener { showDatePicker = false }
        LaunchedEffect(Unit) {
            datePickerDialog.show()
        }
    }

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

                // Task Description TextField
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    label = { Text("Task description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp)) // Added space

                // Due Date Picker Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f) // Allow button to take available space
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Edit Due Date",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(formatDateDisplay(selectedDueDateInDialog))
                    }
                    // Optional: Clear Due Date Button if a due date is selected
                    if (selectedDueDateInDialog != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { selectedDueDateInDialog = null }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Due Date")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
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
                                // MODIFIED: Pass the original item, updated text and new due date
                                onSave(item, editText.text, selectedDueDateInDialog)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoInput(
    onAddItem: (text: String, dueDate: Long?) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // calendar instance for date calculations, not directly for dialog display logic here
    // val calendar = Calendar.getInstance()

    fun formatDateDisplay(timestamp: Long?): String {
        return if (timestamp == null) {
            "Select Due Date"
        } else {
            SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(Date(timestamp))
        }
    }
    if (showDatePicker) {
        // Prepare initial values for the DatePickerDialog
        val currentCalendar = Calendar.getInstance()
        if (selectedDueDate != null) {
            currentCalendar.timeInMillis = selectedDueDate!!
        }
        val initialYear = currentCalendar.get(Calendar.YEAR)
        val initialMonth = currentCalendar.get(Calendar.MONTH)
        val initialDay = currentCalendar.get(Calendar.DAY_OF_MONTH)

        // Create and show the Android View DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth) // Month is 0-indexed
                    set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0) // Normalize time
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDueDate = newCalendar.timeInMillis
                showDatePicker = false // Hide picker after selection
            },
            initialYear,
            initialMonth,
            initialDay
        )

        datePickerDialog.setOnDismissListener {
            showDatePicker = false // Hide if dismissed by clicking outside or cancel
        }
        LaunchedEffect(Unit) { // Use a constant key if it only needs to launch once per showDatePicker flip to true
            datePickerDialog.show()
        }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("New Task Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true }, // This will trigger the dialog logic above
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select Due Date",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(formatDateDisplay(selectedDueDate))
            }
            if (selectedDueDate != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { selectedDueDate = null }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear Due Date")
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (textState.text.isNotBlank()) {
                    onAddItem(textState.text, selectedDueDate)
                    textState = TextFieldValue("")
                    selectedDueDate = null
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = textState.text.isNotBlank()
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Task Icon")
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Add Task")
        }
    }
}

// --- Previews ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        val initialPreviewItems = remember {
            listOf(
                TodoItem(
                    id = 1,
                    text = "Buy groceries",
                    dueDate = System.currentTimeMillis() + 86400000
                ),
                TodoItem(
                    id = 2,
                    text = "Walk the dog",
                    isCompleted = true,
                    dueDate = System.currentTimeMillis() - 86400000
                ),
                TodoItem(id = 3, text = "Read a book", dueDate = null)
            )
        }
        var itemsState by remember { mutableStateOf(initialPreviewItems) } // Use mutableStateOf for the list

        var itemToEdit by remember { mutableStateOf<TodoItem?>(null) }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Preview Todo List") }) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                TodoInput(onAddItem = { text, dueDate ->
                    val newItem = TodoItem(
                        id = (itemsState.maxOfOrNull { it.id } ?: 0) + 1,
                        text = text,
                        dueDate = dueDate,
                        creationDate = System.currentTimeMillis(),
                        updateDate = System.currentTimeMillis()
                    )
                    itemsState = itemsState + newItem
                    println("Preview: Add item called - Text: '$text', DueDate: $dueDate")
                })
                Spacer(modifier = Modifier.height(16.dp))
                if (itemsState.isEmpty()) {
                    Text("No tasks yet. (Preview)")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(
                            items = itemsState,
                            key = { "preview-item-${it.id}" }) { item -> // Use itemsState
                            TodoListItem(
                                item = item,
                                onToggle = {
                                    itemsState = itemsState.map { listItem ->
                                        if (listItem.id == item.id) listItem.copy(
                                            isCompleted = !listItem.isCompleted,
                                            updateDate = System.currentTimeMillis()
                                        ) else listItem
                                    }
                                },
                                onDelete = {
                                    itemsState =
                                        itemsState.filter { listItem -> listItem.id != item.id }
                                },
                                onEdit = { itemToEdit = item }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            itemToEdit?.let { currentItem ->
                EditTodoDialog(
                    item = currentItem,
                    onDismiss = { itemToEdit = null },
                    onSave = { originalItem, updatedText, updatedDueDate ->
                        // Update itemsState to reflect the change in the preview
                        itemsState = itemsState.map { listItem ->
                            if (listItem.id == originalItem.id) {
                                // Use originalItem.copy to ensure we're updating the correct instance
                                // and preserving other properties like 'id', 'creationDate', 'isCompleted'
                                originalItem.copy(
                                    text = updatedText,
                                    dueDate = updatedDueDate,
                                    updateDate = System.currentTimeMillis() // Also update the updateDate
                                )
                            } else {
                                listItem
                            }
                        }
                        itemToEdit = null  // Dismiss the dialog
                        println("Preview: Saved item - ID: ${originalItem.id}, Text: '$updatedText', DueDate: $updatedDueDate")
                    }
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
            item = TodoItem(
                id = 1,
                text = "Existing task to edit",
                isCompleted = false, // Assuming isCompleted is a field
                dueDate = System.currentTimeMillis() // Example due date for preview
                // Add other necessary fields for TodoItem as per your data class definition
            ),
            onDismiss = {
                println("Preview EditDialog: Dismissed")
            },
            onSave = { originalItem, updatedText, updatedDueDate ->
                // In a preview, you typically just log the action
                // No actual data saving happens here
                println("Preview EditDialog: Save clicked")
                println("Original Item ID: ${originalItem.id}")
                println("Updated Text: '$updatedText'")
                println("Updated Due Date: ${updatedDueDate?.let { Date(it) } ?: "None"}")
            }
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