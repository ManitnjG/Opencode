package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.AgentMessageEntity
import com.example.data.model.AgentMode
import com.example.data.model.WorkspaceFileEntity
import com.example.ui.components.AgentMessageCard
import com.example.ui.components.AgentToolRunnerDialog
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
fun AgentChatScreen(
    messages: List<AgentMessageEntity>,
    workspaceFiles: List<WorkspaceFileEntity>,
    activeFile: WorkspaceFileEntity?,
    agentMode: AgentMode,
    isAgentThinking: Boolean,
    agentStatusStep: String,
    onSendPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedContextFile by remember { mutableStateOf<WorkspaceFileEntity?>(activeFile) }
    var contextPickerExpanded by remember { mutableStateOf(false) }
    var showToolInspectorDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    if (showToolInspectorDialog) {
        AgentToolRunnerDialog(
            files = workspaceFiles,
            onExecuteTool = { _, _ -> },
            onDismiss = { showToolInspectorDialog = false }
        )
    }

    LaunchedEffect(messages.size, isAgentThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Plan API authentication & rate limiting",
        "Add dark mode theme toggle with animations",
        "Run linter & fix all type diagnostics",
        "Generate comprehensive unit tests",
        "Refactor into modular component architecture"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Agent Action Shortcuts Bar
        Surface(
            color = OpenCodeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            val topScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(topScrollState)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Generate Test Suite
                Surface(
                    color = OpenCodeMint.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeMint.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(enabled = !isAgentThinking) {
                        onSendPrompt("Generate comprehensive unit tests with edge cases for ${activeFile?.filePath ?: "the active file"}")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = OpenCodeMint, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🧪 Generate Tests", fontSize = 11.sp, color = OpenCodeMint, fontWeight = FontWeight.SemiBold)
                    }
                }

                // AI Security & Refactor
                Surface(
                    color = OpenCodePurple.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodePurple.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(enabled = !isAgentThinking) {
                        onSendPrompt("Audit security vulnerabilities and refactor ${activeFile?.filePath ?: "workspace"} for clean code and performance")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = OpenCodePurple, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🛡️ Security & Refactor", fontSize = 11.sp, color = OpenCodePurple, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Tool Inspector
                Surface(
                    color = OpenCodeCyan.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showToolInspectorDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = OpenCodeCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🛠️ Agent Tools", fontSize = 11.sp, color = OpenCodeCyan, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Doc Generator
                Surface(
                    color = OpenCodeAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(enabled = !isAgentThinking) {
                        onSendPrompt("Generate complete documentation and type specifications for ${activeFile?.filePath ?: "all functions"}")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = OpenCodeAmber, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📝 Docstrings", fontSize = 11.sp, color = OpenCodeAmber, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        // Message list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AgentMessageCard(message = msg)
            }

            // Real-time Agent Thinking Indicator
            if (isAgentThinking) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OpenCodePurple.copy(alpha = 0.12f))
                            .border(1.dp, OpenCodePurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .testTag("agent_thinking_indicator")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = OpenCodePurple
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (agentMode == AgentMode.PLAN) "OpenCode is architecting plan..." else "OpenCode is executing build loop...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OpenCodePurple
                                )
                                Text(
                                    text = agentStatusStep.ifBlank { "Processing AST and dependencies..." },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestion Action Chips
        val chipScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScrollState)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(OpenCodeSurfaceElevated)
                        .border(1.dp, OpenCodeBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAgentThinking) {
                            onSendPrompt(prompt)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (agentMode == AgentMode.PLAN) Icons.Default.Layers else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (agentMode == AgentMode.PLAN) OpenCodeCyan else OpenCodeMint,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Bottom Input Area
        Surface(
            color = OpenCodeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Attached Context Chip (if any)
                if (selectedContextFile != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(OpenCodeCyan.copy(alpha = 0.15f))
                            .border(1.dp, OpenCodeCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File Context",
                            tint = OpenCodeCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "@${selectedContextFile?.filePath}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OpenCodeCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove file context",
                            tint = OpenCodeCyan,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { selectedContextFile = null }
                        )
                    }
                }

                // Input field + Send & Context Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Context Picker Button (`@`)
                    Box {
                        IconButton(
                            onClick = { contextPickerExpanded = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(OpenCodeSurfaceVariant)
                                .testTag("attach_context_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = "Attach File Context",
                                tint = if (selectedContextFile != null) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = contextPickerExpanded,
                            onDismissRequest = { contextPickerExpanded = false },
                            modifier = Modifier.background(OpenCodeSurface)
                        ) {
                            Text(
                                text = "REFERENCE WORKSPACE FILE (@)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            workspaceFiles.forEach { file ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = file.filePath,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedContextFile = file
                                        contextPickerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("agent_prompt_input"),
                        placeholder = {
                            Text(
                                text = if (agentMode == AgentMode.PLAN) "Ask OpenCode to plan a feature..." else "Instruct OpenCode to build, edit or fix code...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (agentMode == AgentMode.PLAN) OpenCodeCyan else OpenCodeMint,
                            unfocusedBorderColor = OpenCodeBorder,
                            focusedContainerColor = OpenCodeSurfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = OpenCodeSurfaceVariant.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (promptInput.isNotBlank() && !isAgentThinking) {
                                val fullPrompt = if (selectedContextFile != null) {
                                    "@${selectedContextFile?.filePath}: $promptInput"
                                } else {
                                    promptInput
                                }
                                onSendPrompt(fullPrompt)
                                promptInput = ""
                            }
                        },
                        enabled = promptInput.isNotBlank() && !isAgentThinking,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (promptInput.isNotBlank() && !isAgentThinking) {
                                    if (agentMode == AgentMode.PLAN) OpenCodeCyan else OpenCodeMint
                                } else {
                                    OpenCodeSurfaceVariant
                                }
                            )
                            .testTag("send_prompt_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Prompt",
                            tint = if (promptInput.isNotBlank() && !isAgentThinking) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
