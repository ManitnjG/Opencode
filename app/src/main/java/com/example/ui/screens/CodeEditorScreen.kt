package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkspaceFileEntity
import com.example.ui.components.WorkspaceSearchDialog
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeRed
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import com.example.ui.theme.OpenCodeSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    files: List<WorkspaceFileEntity>,
    activeFile: WorkspaceFileEntity?,
    isVimModeEnabled: Boolean,
    onSelectFile: (WorkspaceFileEntity) -> Unit,
    onUpdateFileContent: (String) -> Unit,
    onCreateNewFile: (filePath: String, language: String, content: String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onToggleVimMode: () -> Unit,
    onAskAgentAboutFile: (WorkspaceFileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFileExplorer by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showGlobalSearchDialog by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }
    var closedTabIds by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var deleteCandidateFile by remember { mutableStateOf<WorkspaceFileEntity?>(null) }

    val visibleFiles = remember(files, closedTabIds, activeFile) {
        val filtered = files.filter { it.id !in closedTabIds }
        if (filtered.isEmpty() && files.isNotEmpty()) files.take(1) else filtered
    }

    val fileContent = activeFile?.content ?: ""
    val lineCount = remember(fileContent) { fileContent.lines().size.coerceAtLeast(1) }

    if (showGlobalSearchDialog) {
        WorkspaceSearchDialog(
            files = files,
            onSelectFileAndLine = { targetFile, line ->
                onSelectFile(targetFile)
                // reopen tab if closed
                closedTabIds = closedTabIds - targetFile.id
            },
            onReplaceAll = { search, replace, matchCase ->
                // Global replace across files
                var count = 0
                for (f in files) {
                    if (f.content.contains(search, ignoreCase = !matchCase)) {
                        val newC = if (matchCase) f.content.replace(search, replace)
                        else f.content.replace(Regex(Regex.escape(search), RegexOption.IGNORE_CASE), replace)
                        if (f.id == activeFile?.id) {
                            onUpdateFileContent(newC)
                        } else {
                            onCreateNewFile(f.filePath, f.language, newC)
                        }
                        count++
                    }
                }
                Toast.makeText(context, "Replaced across $count files.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showGlobalSearchDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar: File explorer toggle, Active File Tabs, Tools (Ask Agent, Save, Find, Vim)
        Surface(
            color = OpenCodeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: File explorer toggle + Open Files tab scroll
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { showFileExplorer = !showFileExplorer },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (showFileExplorer) OpenCodeCyan.copy(alpha = 0.2f) else OpenCodeSurfaceVariant)
                            .testTag("file_explorer_toggle")
                    ) {
                        Icon(
                            imageVector = if (showFileExplorer) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = "File Explorer",
                            tint = if (showFileExplorer) OpenCodeCyan else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Open file tabs with close buttons
                    val tabScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(tabScrollState),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        visibleFiles.forEach { file ->
                            val isActive = file.id == activeFile?.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isActive) OpenCodeSurfaceElevated else OpenCodeSurfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        if (isActive) OpenCodeCyan.copy(alpha = 0.6f) else OpenCodeBorder.copy(alpha = 0.4f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSelectFile(file) }
                                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = file.filePath.substringAfterLast('/'),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // Tab close X button
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (visibleFiles.size > 1) {
                                                closedTabIds = closedTabIds + file.id
                                                if (isActive) {
                                                    val nextFile = visibleFiles.firstOrNull { it.id != file.id }
                                                    if (nextFile != null) onSelectFile(nextFile)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close tab",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right: Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Global Workspace Search
                    IconButton(
                        onClick = { showGlobalSearchDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Workspace",
                            tint = OpenCodeCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Find in current file
                    IconButton(
                        onClick = { showFindBar = !showFindBar },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FindInPage,
                            contentDescription = "Find in File",
                            tint = if (showFindBar) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Ask Agent button
                    IconButton(
                        onClick = {
                            if (activeFile != null) {
                                onAskAgentAboutFile(activeFile)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask Agent",
                            tint = OpenCodePurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Vim mode toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isVimModeEnabled) OpenCodeMint.copy(alpha = 0.2f) else OpenCodeSurfaceVariant)
                            .clickable { onToggleVimMode() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isVimModeEnabled) "VIM: ON" else "VIM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isVimModeEnabled) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Search Bar (Find in file)
        AnimatedVisibility(visible = showFindBar) {
            Surface(
                color = OpenCodeSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        placeholder = { Text("Find in current file...", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 12.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenCodeCyan,
                            unfocusedBorderColor = OpenCodeBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showFindBar = false; searchQuery = "" },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Main Editor Area + Optional File Explorer Drawer
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // File Explorer Drawer (Left Panel)
            AnimatedVisibility(visible = showFileExplorer) {
                Surface(
                    color = OpenCodeSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "WORKSPACE FILES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { showNewFileDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New File",
                                    tint = OpenCodeMint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(files, key = { it.id }) { file ->
                                val isSelected = file.id == activeFile?.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) OpenCodeCyan.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { onSelectFile(file) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = null,
                                            tint = if (isSelected) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = file.filePath,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) OpenCodeCyan else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }

                                    if (files.size > 1) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = OpenCodeRed.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { deleteCandidateFile = file }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Editor Code Surface (Line numbers gutter + Code edit area)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF070B12))
            ) {
                // Line numbers gutter
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Color(0xFF05080E))
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.End
                ) {
                    (1..lineCount).forEach { lineNum ->
                        Text(
                            text = lineNum.toString(),
                            style = MonospaceCodeStyle.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                // Code text field
                OutlinedTextField(
                    value = activeFile?.content ?: "",
                    onValueChange = { onUpdateFileContent(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_textarea"),
                    textStyle = MonospaceCodeStyle.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }

        // Status bar footer (File path, language, line count, encoding)
        Surface(
            color = OpenCodeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${activeFile?.filePath ?: "No file"} • ${activeFile?.language?.uppercase() ?: "TXT"}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OpenCodeCyan
                )

                Text(
                    text = "Lines: $lineCount • UTF-8 • ${if (isVimModeEnabled) "[NORMAL]" else "[EDIT]"}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        var newPath by remember { mutableStateOf("") }
        var newLang by remember { mutableStateOf("typescript") }

        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create Workspace File", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPath,
                        onValueChange = { newPath = it },
                        label = { Text("File Path (e.g. src/auth/login.ts)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLang,
                        onValueChange = { newLang = it },
                        label = { Text("Language (typescript, python, kotlin, etc.)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPath.isNotBlank()) {
                            onCreateNewFile(newPath, newLang, "// Created with OpenCode\n")
                            showNewFileDialog = false
                            Toast.makeText(context, "Created $newPath", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OpenCodeCyan)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (deleteCandidateFile != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidateFile = null },
            title = { Text("Delete File?", color = OpenCodeRed) },
            text = { Text("Are you sure you want to delete '${deleteCandidateFile?.filePath}' from the workspace?") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteCandidateFile?.let { onDeleteFile(it.id) }
                        deleteCandidateFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OpenCodeRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateFile = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
