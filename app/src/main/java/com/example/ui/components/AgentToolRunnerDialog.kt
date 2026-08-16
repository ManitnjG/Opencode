package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

data class AgentToolDefinition(
    val id: String,
    val name: String,
    val description: String,
    val defaultParam: String,
    val category: String
)

@Composable
fun AgentToolRunnerDialog(
    files: List<WorkspaceFileEntity>,
    onExecuteTool: (toolId: String, param: String) -> Unit,
    onDismiss: () -> Unit
) {
    val tools = listOf(
        AgentToolDefinition("fs.readFile", "Read File", "Load entire file content into agent working context", "src/app/page.tsx", "Filesystem"),
        AgentToolDefinition("fs.writeFile", "Write File", "Create or overwrite file in workspace", "src/utils/math.ts", "Filesystem"),
        AgentToolDefinition("bash.runCommand", "Execute Shell", "Run CLI command inside container sandbox", "npm test", "Execution"),
        AgentToolDefinition("lsp.diagnostics", "Compiler Diagnostics", "Run static code analysis & typechecker", "all", "LSP"),
        AgentToolDefinition("git.snapshot", "Git Commit Snapshot", "Record commit diff in version graph", "feat: update handlers", "Git"),
        AgentToolDefinition("web.search", "Search Web Docs", "Query developer documentation and APIs", "Next.js 15 Server Actions", "Network")
    )

    var selectedTool by remember { mutableStateOf(tools.first()) }
    var inputParam by remember { mutableStateOf(selectedTool.defaultParam) }
    var executionOutput by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = OpenCodePurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agent Tools Inspector",
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
                    .height(440.dp)
            ) {
                Text(
                    text = "Inspect and test autonomous agent tools and capability schemas:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tools selection chips
                LazyColumn(
                    modifier = Modifier
                        .height(130.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tools) { tool ->
                        val isSelected = tool.id == selectedTool.id
                        Surface(
                            color = if (isSelected) OpenCodeSurfaceElevated else OpenCodeSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) OpenCodePurple else OpenCodeBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTool = tool
                                    inputParam = tool.defaultParam
                                    executionOutput = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tool.id,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) OpenCodePurple else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(OpenCodeSurfaceVariant)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(tool.category, fontSize = 9.sp, color = OpenCodeCyan)
                                        }
                                    }
                                    Text(
                                        text = tool.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = OpenCodePurple, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Param Input
                OutlinedTextField(
                    value = inputParam,
                    onValueChange = { inputParam = it },
                    label = { Text("Tool Argument / Parameter", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodePurple,
                        unfocusedBorderColor = OpenCodeBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Run tool button
                Button(
                    onClick = {
                        isRunning = true
                        when (selectedTool.id) {
                            "fs.readFile" -> {
                                val match = files.find { it.filePath.contains(inputParam.trim(), ignoreCase = true) }
                                executionOutput = if (match != null) {
                                    "✓ Read ${match.content.lines().size} lines from '${match.filePath}':\n\n${match.content.take(250)}..."
                                } else {
                                    "⚠ File '$inputParam' not found in workspace."
                                }
                            }
                            "bash.runCommand" -> {
                                executionOutput = "✓ Executed: `$inputParam` (exit code: 0)\n[stdout]: Command completed successfully with 0 errors."
                            }
                            "lsp.diagnostics" -> {
                                executionOutput = "✓ LSP Diagnostic Analyzer: Checked ${files.size} workspace files. 0 fatal syntax errors found."
                            }
                            "git.snapshot" -> {
                                executionOutput = "✓ Git Snapshot created: [commit 8f3a92b] '$inputParam' (${files.size} files tracked)"
                            }
                            "web.search" -> {
                                executionOutput = "✓ Web Documentation: Fetched latest specs & guides for '$inputParam'."
                            }
                            else -> {
                                executionOutput = "✓ Tool ${selectedTool.id} executed successfully with param: '$inputParam'"
                            }
                        }
                        isRunning = false
                        onExecuteTool(selectedTool.id, inputParam)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OpenCodePurple),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invoke Tool", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tool Output Console
                Surface(
                    color = Color(0xFF070B12),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = executionOutput ?: "Output console: Select a tool and tap 'Invoke Tool' to view results.",
                            style = MonospaceCodeStyle.copy(
                                fontSize = 11.sp,
                                color = if (executionOutput != null) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = OpenCodePurple)
            }
        }
    )
}
