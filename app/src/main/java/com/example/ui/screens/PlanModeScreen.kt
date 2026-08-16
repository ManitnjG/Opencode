package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlanTaskEntity
import com.example.data.model.PlanTaskPriority
import com.example.data.model.ProjectEntity
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeRed
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import com.example.ui.theme.OpenCodeSurfaceVariant

enum class TaskFilter {
    ALL, PENDING, COMPLETED, CRITICAL_HIGH
}

@Composable
fun PlanModeScreen(
    currentProject: ProjectEntity?,
    tasks: List<PlanTaskEntity>,
    onToggleTask: (PlanTaskEntity) -> Unit,
    onAddTask: (title: String, desc: String, priority: PlanTaskPriority, phase: String, files: String) -> Unit,
    onUpdateTask: (PlanTaskEntity) -> Unit,
    onDeleteTask: (String) -> Unit,
    onDeleteCompleted: () -> Unit,
    onRegeneratePlan: (String?) -> Unit,
    onExecuteTaskInBuildMode: (PlanTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<PlanTaskEntity?>(null) }
    var showAiPlanGeneratorDialog by remember { mutableStateOf(false) }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val criticalCount = tasks.count { it.priority == PlanTaskPriority.CRITICAL }
    val highCount = tasks.count { it.priority == PlanTaskPriority.HIGH }
    val mediumCount = tasks.count { it.priority == PlanTaskPriority.MEDIUM }
    val lowCount = tasks.count { it.priority == PlanTaskPriority.LOW }

    val filteredTasks = tasks.filter { task ->
        val matchesCategory = when (selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.PENDING -> !task.isCompleted
            TaskFilter.COMPLETED -> task.isCompleted
            TaskFilter.CRITICAL_HIGH -> task.priority == PlanTaskPriority.CRITICAL || task.priority == PlanTaskPriority.HIGH
        }
        val query = searchQuery.trim()
        val matchesSearch = if (query.isEmpty()) {
            true
        } else {
            task.title.contains(query, ignoreCase = true) ||
            task.description.contains(query, ignoreCase = true) ||
            task.priority.name.contains(query, ignoreCase = true) ||
            task.phase.contains(query, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header Section: Blueprint Overview & Metrics ---
        Surface(
            color = OpenCodeSurface,
            border = BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OpenCodeCyan.copy(alpha = 0.18f))
                                .border(1.dp, OpenCodeCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Plan Mode",
                                tint = OpenCodeCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PLAN MODE",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = OpenCodeCyan.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "AI Workflow Roadmap",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenCodeCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Organize architectural tasks before executing code in Build Mode",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Project Badge
                    if (currentProject != null) {
                        Surface(
                            color = OpenCodeSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, OpenCodeBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = OpenCodePurple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentProject.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Top Progress Card with Percentage & Linear Progress Bar ---
                Surface(
                    color = OpenCodeSurfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (progress == 1f && totalCount > 0) OpenCodeMint.copy(alpha = 0.5f) else OpenCodeBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_progress_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (progress == 1f && totalCount > 0) Icons.Default.CheckCircle else Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = if (progress == 1f && totalCount > 0) OpenCodeMint else OpenCodeCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (totalCount == 0) "No tasks added yet" else if (progress == 1f) "All Tasks Completed" else "Task Completion Progress",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Percentage Chip
                            Surface(
                                color = if (progress == 1f && totalCount > 0) OpenCodeMint.copy(alpha = 0.15f) else OpenCodeCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (progress == 1f && totalCount > 0) OpenCodeMint.copy(alpha = 0.5f) else OpenCodeCyan.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (progress == 1f && totalCount > 0) OpenCodeMint else OpenCodeCyan,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("plan_progress_percentage")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // High-visibility Animated Progress Bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("plan_mode_progress_bar"),
                            color = if (progress == 1f && totalCount > 0) OpenCodeMint else OpenCodeCyan,
                            trackColor = OpenCodeSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Secondary Status Row: Completed & Remaining Counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$completedCount of $totalCount completed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                            val remaining = totalCount - completedCount
                            Text(
                                text = if (remaining > 0) "$remaining task${if (remaining == 1) "" else "s"} remaining" else if (totalCount > 0) "Ready to Build" else "0 tasks",
                                fontSize = 11.sp,
                                color = if (remaining == 0 && totalCount > 0) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (remaining == 0 && totalCount > 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Priority Stat Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityCountPill(priority = PlanTaskPriority.CRITICAL, count = criticalCount)
                    PriorityCountPill(priority = PlanTaskPriority.HIGH, count = highCount)
                    PriorityCountPill(priority = PlanTaskPriority.MEDIUM, count = mediumCount)
                    PriorityCountPill(priority = PlanTaskPriority.LOW, count = lowCount)
                }
            }
        }

        // --- Controls Bar: Filters & Action Buttons ---
        Surface(
            color = OpenCodeSurfaceElevated,
            border = BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // + Add Task Button
                        Surface(
                            color = OpenCodeCyan,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { showAddTaskDialog = true }
                                .testTag("plan_add_task_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Task",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Task",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        // ✨ AI Auto-Plan Button
                        Surface(
                            color = OpenCodePurple.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, OpenCodePurple.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { showAiPlanGeneratorDialog = true }
                                .testTag("plan_ai_generator_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Plan",
                                    tint = OpenCodePurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✨ AI Plan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenCodePurple
                                )
                            }
                        }
                    }

                    // Clear Completed Action
                    if (completedCount > 0) {
                        Surface(
                            color = OpenCodeSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, OpenCodeBorder),
                            modifier = Modifier.clickable { onDeleteCompleted() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Completed",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Clear Done ($completedCount)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Search Bar for Title & Priority Tags
                Surface(
                    color = OpenCodeSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (searchQuery.isNotBlank()) OpenCodeCyan.copy(alpha = 0.6f) else OpenCodeBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Tasks",
                            tint = if (searchQuery.isNotBlank()) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Default
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(OpenCodeCyan),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search by title or priority (e.g. CRITICAL, HIGH, Auth)...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .testTag("plan_task_search_input")
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                                    .testTag("plan_clear_search_button")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChipButton(
                        label = "All (${tasks.size})",
                        selected = selectedFilter == TaskFilter.ALL,
                        onClick = { selectedFilter = TaskFilter.ALL }
                    )
                    FilterChipButton(
                        label = "Pending (${tasks.count { !it.isCompleted }})",
                        selected = selectedFilter == TaskFilter.PENDING,
                        onClick = { selectedFilter = TaskFilter.PENDING }
                    )
                    FilterChipButton(
                        label = "Completed ($completedCount)",
                        selected = selectedFilter == TaskFilter.COMPLETED,
                        onClick = { selectedFilter = TaskFilter.COMPLETED }
                    )
                    FilterChipButton(
                        label = "Critical / High (${criticalCount + highCount})",
                        selected = selectedFilter == TaskFilter.CRITICAL_HIGH,
                        onClick = { selectedFilter = TaskFilter.CRITICAL_HIGH }
                    )
                }
            }
        }

        // --- Task List (Column Layout) ---
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = OpenCodeCyan.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (tasks.isEmpty()) "No workflow tasks planned yet" else if (searchQuery.isNotBlank()) "No tasks match '$searchQuery'" else "No tasks match the filter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (tasks.isEmpty())
                            "Add roadmap items or click 'AI Plan' to generate a structured AI workflow"
                        else if (searchQuery.isNotBlank())
                            "Try searching by another title keyword or priority tag (CRITICAL, HIGH, MEDIUM, LOW)"
                        else
                            "Try selecting 'All' to view all tasks in this plan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (tasks.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAiPlanGeneratorDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OpenCodeCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("plan_tasks_list"),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    PlanTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onEdit = { editingTask = task },
                        onDelete = { onDeleteTask(task.id) },
                        onExecuteInBuildMode = { onExecuteTaskInBuildMode(task) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // --- Add Task Dialog ---
    if (showAddTaskDialog) {
        PlanTaskEditDialog(
            task = null,
            onDismiss = { showAddTaskDialog = false },
            onSave = { title, desc, priority, phase, files ->
                onAddTask(title, desc, priority, phase, files)
                showAddTaskDialog = false
            }
        )
    }

    // --- Edit Task Dialog ---
    editingTask?.let { taskToEdit ->
        PlanTaskEditDialog(
            task = taskToEdit,
            onDismiss = { editingTask = null },
            onSave = { title, desc, priority, phase, files ->
                onUpdateTask(
                    taskToEdit.copy(
                        title = title,
                        description = desc,
                        priority = priority,
                        phase = phase,
                        impactedFiles = files
                    )
                )
                editingTask = null
            }
        )
    }

    // --- AI Plan Generator Dialog ---
    if (showAiPlanGeneratorDialog) {
        AiPlanGeneratorDialog(
            currentProjectName = currentProject?.name ?: "Current Workspace",
            onDismiss = { showAiPlanGeneratorDialog = false },
            onGenerate = { customGoal ->
                onRegeneratePlan(customGoal)
                showAiPlanGeneratorDialog = false
            }
        )
    }
}

@Composable
fun PlanTaskCard(
    task: PlanTaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExecuteInBuildMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground by animateColorAsState(
        targetValue = if (task.isCompleted) OpenCodeSurface.copy(alpha = 0.7f) else OpenCodeSurfaceElevated,
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("plan_task_item_${task.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) OpenCodeBorder.copy(alpha = 0.4f) else getPriorityColor(task.priority).copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Row: Checkbox + Title + Priority Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = OpenCodeMint,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 2.dp)
                        .testTag("plan_task_checkbox_${task.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Phase badge + Priority Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = OpenCodeSurfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = task.phase,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        PriorityIndicatorBadge(priority = task.priority)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Task Title
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    // Task Description
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    // Impacted Files Tag
                    if (task.impactedFiles.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OpenCodeCyan.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = OpenCodeCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.impactedFiles,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = OpenCodeCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = OpenCodeBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Actions Row: Execute in Build Mode & Edit / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // "⚡ Execute in Build Mode" Action Button
                Surface(
                    color = if (task.isCompleted) OpenCodeSurfaceVariant else OpenCodeMint.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        if (task.isCompleted) OpenCodeBorder else OpenCodeMint.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clickable { onExecuteInBuildMode() }
                        .testTag("execute_task_button_${task.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Execute in Build Mode",
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else OpenCodeMint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (task.isCompleted) "Re-Run in Build Mode" else "Execute with Agent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else OpenCodeMint
                        )
                    }
                }

                // Edit & Delete Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = OpenCodeRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityIndicatorBadge(
    priority: PlanTaskPriority,
    modifier: Modifier = Modifier
) {
    val color = getPriorityColor(priority)
    val text = when (priority) {
        PlanTaskPriority.CRITICAL -> "CRITICAL"
        PlanTaskPriority.HIGH -> "HIGH"
        PlanTaskPriority.MEDIUM -> "MEDIUM"
        PlanTaskPriority.LOW -> "LOW"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}

@Composable
fun PriorityCountPill(
    priority: PlanTaskPriority,
    count: Int,
    modifier: Modifier = Modifier
) {
    val color = getPriorityColor(priority)
    Surface(
        color = OpenCodeSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (count > 0) color.copy(alpha = 0.4f) else OpenCodeBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${priority.name}: $count",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) OpenCodeCyan.copy(alpha = 0.2f) else OpenCodeSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (selected) OpenCodeCyan else OpenCodeBorder
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

fun getPriorityColor(priority: PlanTaskPriority): Color {
    return when (priority) {
        PlanTaskPriority.CRITICAL -> OpenCodeRed
        PlanTaskPriority.HIGH -> OpenCodeAmber
        PlanTaskPriority.MEDIUM -> OpenCodeCyan
        PlanTaskPriority.LOW -> OpenCodeMint
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTaskEditDialog(
    task: PlanTaskEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, priority: PlanTaskPriority, phase: String, files: String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: PlanTaskPriority.MEDIUM) }
    var phase by remember { mutableStateOf(task?.phase ?: "Phase 1: Architecture & Setup") }
    var files by remember { mutableStateOf(task?.impactedFiles ?: "") }

    val isEditing = task != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = OpenCodeCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Edit Workflow Task" else "Add AI Workflow Task",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Implement OAuth authentication flow") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        focusedLabelColor = OpenCodeCyan
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Strategy") },
                    placeholder = { Text("Outline technical steps, dependencies, or edge cases") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        focusedLabelColor = OpenCodeCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Priority Level:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Priority Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlanTaskPriority.values().forEach { p ->
                        val isSelected = priority == p
                        val pColor = getPriorityColor(p)
                        Surface(
                            color = if (isSelected) pColor.copy(alpha = 0.2f) else OpenCodeSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSelected) pColor else OpenCodeBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p }
                        ) {
                            Text(
                                text = p.name,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phase,
                    onValueChange = { phase = it },
                    label = { Text("Phase / Milestone") },
                    placeholder = { Text("e.g. Phase 2: Core Components") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        focusedLabelColor = OpenCodeCyan
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = files,
                    onValueChange = { files = it },
                    label = { Text("Impacted Files") },
                    placeholder = { Text("e.g. src/auth.ts, src/types.ts") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        focusedLabelColor = OpenCodeCyan
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, priority, phase, files)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OpenCodeCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "Update Task" else "Add Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = OpenCodeSurface,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AiPlanGeneratorDialog(
    currentProjectName: String,
    onDismiss: () -> Unit,
    onGenerate: (String?) -> Unit
) {
    var customGoal by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = OpenCodePurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Workflow Plan Generator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Generate a prioritized architectural roadmap for '$currentProjectName'. OpenCode will break down the workspace into actionable phases.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customGoal,
                    onValueChange = { customGoal = it },
                    label = { Text("Specific Feature Goal (Optional)") },
                    placeholder = { Text("e.g. Add Stripe subscriptions and user dashboard") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodePurple,
                        focusedLabelColor = OpenCodePurple
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(customGoal.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = OpenCodePurple, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate AI Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = OpenCodeSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
