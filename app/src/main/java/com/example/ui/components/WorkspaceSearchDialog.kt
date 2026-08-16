package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkspaceFileEntity
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

data class SearchResultItem(
    val file: WorkspaceFileEntity,
    val lineIndex: Int,
    val lineNumber: Int,
    val lineText: String,
    val matchStart: Int,
    val matchEnd: Int
)

@Composable
fun WorkspaceSearchDialog(
    files: List<WorkspaceFileEntity>,
    onSelectFileAndLine: (WorkspaceFileEntity, Int) -> Unit,
    onReplaceAll: (search: String, replace: String, matchCase: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var showReplaceMode by remember { mutableStateOf(false) }

    val searchResults by remember(searchQuery, matchCase, files) {
        derivedStateOf {
            if (searchQuery.isBlank()) emptyList()
            else {
                val results = mutableListOf<SearchResultItem>()
                for (file in files) {
                    val lines = file.content.lines()
                    for ((idx, line) in lines.withIndex()) {
                        var startIndex = 0
                        while (true) {
                            val foundIndex = line.indexOf(searchQuery, startIndex, ignoreCase = !matchCase)
                            if (foundIndex == -1) break
                            results.add(
                                SearchResultItem(
                                    file = file,
                                    lineIndex = idx,
                                    lineNumber = idx + 1,
                                    lineText = line,
                                    matchStart = foundIndex,
                                    matchEnd = foundIndex + searchQuery.length
                                )
                            )
                            startIndex = foundIndex + searchQuery.length
                        }
                    }
                }
                results
            }
        }
    }

    val distinctFileCount = remember(searchResults) {
        searchResults.map { it.file.id }.distinct().size
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (showReplaceMode) Icons.Default.FindReplace else Icons.Default.Search,
                        contentDescription = null,
                        tint = OpenCodeCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showReplaceMode) "Workspace Search & Replace" else "Global Workspace Search",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search text across all files...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_search_input"),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        unfocusedBorderColor = OpenCodeBorder
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Optional Replace Input
                if (showReplaceMode) {
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        label = { Text("Replace with...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workspace_replace_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenCodeMint,
                            unfocusedBorderColor = OpenCodeBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = matchCase,
                            onCheckedChange = { matchCase = it },
                            colors = CheckboxDefaults.colors(checkedColor = OpenCodeCyan)
                        )
                        Text("Match case", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showReplaceMode = !showReplaceMode }) {
                            Text(
                                text = if (showReplaceMode) "Hide Replace" else "Enable Replace",
                                fontSize = 11.sp,
                                color = OpenCodeCyan
                            )
                        }

                        if (showReplaceMode && searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    if (searchQuery.isNotEmpty()) {
                                        onReplaceAll(searchQuery, replaceQuery, matchCase)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OpenCodeMint),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("replace_all_button")
                            ) {
                                Text("Replace All", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Match stats
                Surface(
                    color = OpenCodeSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${searchResults.size} matches found across $distinctFileCount files",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (searchResults.isNotEmpty()) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Results list
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Type a keyword to search across the entire project." else "No matches found.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults) { result ->
                            Surface(
                                color = OpenCodeSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectFileAndLine(result.file, result.lineNumber)
                                        onDismiss()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = OpenCodeCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = result.file.filePath,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Text(
                                            text = "Line ${result.lineNumber}",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = OpenCodeAmber
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Line snippet with highlighted match
                                    val annotatedLine = buildAnnotatedString {
                                        val line = result.lineText.trim()
                                        val trimmedOffset = result.lineText.indexOf(line)
                                        val relStart = (result.matchStart - trimmedOffset).coerceIn(0, line.length)
                                        val relEnd = (result.matchEnd - trimmedOffset).coerceIn(0, line.length)

                                        if (relStart > 0) {
                                            append(line.substring(0, relStart))
                                        }
                                        withStyle(
                                            SpanStyle(
                                                background = OpenCodeAmber.copy(alpha = 0.35f),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append(line.substring(relStart, relEnd))
                                        }
                                        if (relEnd < line.length) {
                                            append(line.substring(relEnd))
                                        }
                                    }

                                    Text(
                                        text = annotatedLine,
                                        style = MonospaceCodeStyle.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = OpenCodeCyan)
            }
        }
    )
}
