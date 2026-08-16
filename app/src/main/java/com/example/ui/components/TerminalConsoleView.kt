package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TerminalLogEntity
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeRed
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TerminalLogLevelFilter(val label: String) {
    ALL("All Output"),
    INFO("Info"),
    SUCCESS("Success"),
    WARNINGS("Warnings"),
    ERRORS("Errors")
}

/**
 * Production-ready terminal-like UI component that displays execution logs
 * and system output using a monospaced font in a sleek dark-themed container.
 */
@Composable
fun TerminalConsoleView(
    terminalLogs: List<TerminalLogEntity>,
    onExecuteCommand: (String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
    terminalTitle: String = "opencode@workspace: ~/src (bash)",
    isRunning: Boolean = false,
    listState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var commandInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showTimestamps by remember { mutableStateOf(true) }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    var selectedLevelFilter by remember { mutableStateOf(TerminalLogLevelFilter.ALL) }
    var activeTerminalTab by remember { mutableStateOf("bash") }
    var showEnvDialog by remember { mutableStateOf(false) }
    var envVars by remember { mutableStateOf(listOf("NODE_ENV" to "development", "PORT" to "3000", "LOG_LEVEL" to "debug")) }

    if (showEnvDialog) {
        TerminalEnvManagerDialog(
            initialEnvVars = envVars,
            onSaveEnvVars = { updated ->
                envVars = updated
                Toast.makeText(context, "Saved ${updated.size} environment variables", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEnvDialog = false }
        )
    }

    // Blinking cursor transition for realistic terminal feel
    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    // Auto scroll when new logs arrive
    LaunchedEffect(terminalLogs.size, autoScrollEnabled) {
        if (autoScrollEnabled && terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    // Filter logs based on search query and log level
    val filteredLogs = remember(terminalLogs, searchQuery, selectedLevelFilter) {
        terminalLogs.filter { log ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                log.command.contains(searchQuery, ignoreCase = true) ||
                        log.output.contains(searchQuery, ignoreCase = true)
            }

            val matchesLevel = when (selectedLevelFilter) {
                TerminalLogLevelFilter.ALL -> true
                TerminalLogLevelFilter.INFO -> log.exitCode == 0 && !log.output.contains("error", ignoreCase = true)
                TerminalLogLevelFilter.SUCCESS -> log.output.contains("PASS", ignoreCase = true) ||
                        log.output.contains("success", ignoreCase = true) ||
                        log.output.contains("Done in", ignoreCase = true)
                TerminalLogLevelFilter.WARNINGS -> log.output.contains("warn", ignoreCase = true)
                TerminalLogLevelFilter.ERRORS -> log.exitCode != 0 ||
                        log.output.contains("error", ignoreCase = true) ||
                        log.output.contains("failed", ignoreCase = true)
            }

            matchesSearch && matchesLevel
        }
    }

    val quickCommands = listOf(
        "npm run build",
        "npm test",
        "pytest",
        "git status",
        "git log -n 3",
        "tree",
        "cargo check",
        "lsp diagnostics",
        "clear"
    )

    // Container with dark background and subtle border
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, OpenCodeBorder, RoundedCornerShape(12.dp)),
        color = Color(0xFF080C14) // Deep obsidian dark theme container
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. Terminal Window Chrome Header ---
            TerminalWindowHeader(
                title = terminalTitle,
                isRunning = isRunning,
                logCount = terminalLogs.size,
                showTimestamps = showTimestamps,
                autoScrollEnabled = autoScrollEnabled,
                showSearchBar = showSearchBar,
                onToggleTimestamps = { showTimestamps = !showTimestamps },
                onToggleAutoScroll = { autoScrollEnabled = !autoScrollEnabled },
                onToggleSearchBar = {
                    showSearchBar = !showSearchBar
                    if (!showSearchBar) searchQuery = ""
                },
                onClearLogs = onClearLogs,
                onCopyAllLogs = {
                    val fullLogText = terminalLogs.joinToString("\n\n") { log ->
                        "[$ ${log.command}]\n${log.output}"
                    }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Terminal Logs", fullLogText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Terminal logs copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )

            // --- Multi-Terminal Tabs Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090D17))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "bash" to "1: bash",
                        "tests" to "2: tests & build",
                        "server" to "3: server"
                    )

                    tabs.forEach { (tabId, label) ->
                        val isCurrent = activeTerminalTab == tabId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCurrent) OpenCodeSurfaceElevated else Color.Transparent)
                                .border(1.dp, if (isCurrent) OpenCodeCyan else OpenCodeBorder.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .clickable { activeTerminalTab = tabId }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) OpenCodeMint else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // .env config button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OpenCodeAmber.copy(alpha = 0.15f))
                        .border(1.dp, OpenCodeAmber.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { showEnvDialog = true }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = OpenCodeAmber, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(".env", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = OpenCodeAmber)
                    }
                }
            }

            // --- 2. Optional In-Terminal Search Bar ---
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OpenCodeSurface)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OpenCodeCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter execution logs & stderr...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("terminal_search_input"),
                        textStyle = MonospaceCodeStyle.copy(fontSize = 11.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "${filteredLogs.size} matches",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = OpenCodeCyan,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // --- 3. Filter Chips (All, Info, Success, Warnings, Errors) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color(0xFF0D121F))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILTER:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TerminalLogLevelFilter.values().forEach { filter ->
                    val isSelected = selectedLevelFilter == filter
                    val badgeColor = when (filter) {
                        TerminalLogLevelFilter.ALL -> OpenCodeCyan
                        TerminalLogLevelFilter.INFO -> Color(0xFF60A5FA)
                        TerminalLogLevelFilter.SUCCESS -> OpenCodeMint
                        TerminalLogLevelFilter.WARNINGS -> OpenCodeAmber
                        TerminalLogLevelFilter.ERRORS -> OpenCodeRed
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) badgeColor else OpenCodeBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { selectedLevelFilter = filter }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- 4. Main Terminal Output Console Area ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF06090E))
            ) {
                if (filteredLogs.isEmpty()) {
                    TerminalEmptyBanner(searchActive = searchQuery.isNotBlank())
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("terminal_output_log_list")
                    ) {
                        item {
                            TerminalSystemWelcomeMessage()
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(filteredLogs, key = { it.id }) { log ->
                            TerminalLogItemView(
                                log = log,
                                showTimestamps = showTimestamps,
                                searchQuery = searchQuery
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Bottom active cursor prompt
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "opencode@workspace:~$ ",
                                    style = MonospaceCodeStyle.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OpenCodeMint
                                    )
                                )
                                Text(
                                    text = "█",
                                    color = OpenCodeMint.copy(alpha = cursorAlpha),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // --- 5. Quick Command Suggestion Ribbon ---
            val chipScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState)
                    .background(OpenCodeSurface)
                    .border(androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RUN:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = OpenCodeCyan
                )

                quickCommands.forEach { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OpenCodeSurfaceElevated)
                            .border(1.dp, OpenCodeBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                if (cmd == "clear") {
                                    onClearLogs()
                                } else {
                                    onExecuteCommand(cmd)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("quick_cmd_${cmd.replace(" ", "_")}")
                    ) {
                        Text(
                            text = cmd,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = when (cmd) {
                                "clear" -> OpenCodeAmber
                                "npm run build", "cargo check" -> OpenCodeMint
                                "pytest", "npm test" -> OpenCodeCyan
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            // --- 6. Terminal Input Execution Bar ---
            Surface(
                color = Color(0xFF0D121F),
                border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$ ",
                        style = MonospaceCodeStyle.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OpenCodeMint
                        )
                    )

                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_command_input"),
                        placeholder = {
                            Text(
                                text = "Enter shell command (npm test, git diff, ls -la)...",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp, color = Color.White),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commandInput.isNotBlank()) {
                                    onExecuteCommand(commandInput)
                                    commandInput = ""
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    IconButton(
                        onClick = {
                            if (commandInput.isNotBlank()) {
                                onExecuteCommand(commandInput)
                                commandInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (commandInput.isNotBlank()) OpenCodeCyan else OpenCodeSurfaceElevated)
                            .testTag("terminal_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Execute Command",
                            tint = if (commandInput.isNotBlank()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Terminal Chrome Header with Unix traffic lights and status badges.
 */
@Composable
private fun TerminalWindowHeader(
    title: String,
    isRunning: Boolean,
    logCount: Int,
    showTimestamps: Boolean,
    autoScrollEnabled: Boolean,
    showSearchBar: Boolean,
    onToggleTimestamps: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onToggleSearchBar: () -> Unit,
    onClearLogs: () -> Unit,
    onCopyAllLogs: () -> Unit
) {
    Surface(
        color = Color(0xFF0F1523),
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Traffic light control dots + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Red dot (Clear)
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(OpenCodeRed)
                        .clickable { onClearLogs() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Yellow dot
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(OpenCodeAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Green dot
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(OpenCodeMint)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = OpenCodeCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                if (isRunning) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(OpenCodeAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "● RUNNING",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = OpenCodeAmber
                        )
                    }
                }
            }

            // Right: Terminal controls (Timestamps, Search, Auto-Scroll, Copy, Clear)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Timestamps Toggle
                IconButton(
                    onClick = onToggleTimestamps,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Toggle Timestamps",
                        tint = if (showTimestamps) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Search Toggle
                IconButton(
                    onClick = onToggleSearchBar,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Terminal",
                        tint = if (showSearchBar) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Auto Scroll Toggle
                IconButton(
                    onClick = onToggleAutoScroll,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Auto Scroll",
                        tint = if (autoScrollEnabled) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Copy All Logs
                IconButton(
                    onClick = onCopyAllLogs,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Logs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Clear Terminal
                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clear Terminal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual Terminal Log row displaying the executed prompt, duration, exit code,
 * and syntax-highlighted system output.
 */
@Composable
private fun TerminalLogItemView(
    log: TerminalLogEntity,
    showTimestamps: Boolean,
    searchQuery: String
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { timeFormatter.format(Date(log.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // --- Command Prompt Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (showTimestamps) {
                    Text(
                        text = "[$formattedTime] ",
                        style = MonospaceCodeStyle.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    )
                }

                Text(
                    text = "opencode@workspace:~$ ",
                    style = MonospaceCodeStyle.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OpenCodeMint
                    )
                )

                Text(
                    text = log.command,
                    style = MonospaceCodeStyle.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Exit code status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (log.exitCode == 0) OpenCodeMint.copy(alpha = 0.15f) else OpenCodeRed.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        if (log.exitCode == 0) OpenCodeMint.copy(alpha = 0.5f) else OpenCodeRed.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (log.exitCode == 0) "exit 0" else "exit ${log.exitCode}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (log.exitCode == 0) OpenCodeMint else OpenCodeRed
                )
            }
        }

        // --- Execution System Output ---
        if (log.output.isNotBlank()) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0B101B))
                    .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                RenderHighlightedOutput(
                    output = log.output,
                    exitCode = log.exitCode,
                    searchQuery = searchQuery
                )
            }
        }
    }
}

/**
 * Intelligent syntax highlighting for stdout / stderr terminal streams.
 */
@Composable
private fun RenderHighlightedOutput(
    output: String,
    exitCode: Int,
    searchQuery: String
) {
    val lines = output.lines()

    Column {
        lines.forEach { rawLine ->
            val annotated = buildAnnotatedString {
                val trimmed = rawLine.trim()

                val baseColor = when {
                    trimmed.startsWith("ERROR", ignoreCase = true) ||
                            trimmed.startsWith("FAIL", ignoreCase = true) ||
                            trimmed.contains("error:", ignoreCase = true) ||
                            trimmed.contains("exception", ignoreCase = true) ||
                            exitCode != 0 -> OpenCodeRed

                    trimmed.startsWith("WARN", ignoreCase = true) ||
                            trimmed.contains("warning:", ignoreCase = true) -> OpenCodeAmber

                    trimmed.startsWith("PASS", ignoreCase = true) ||
                            trimmed.contains("Compiled successfully", ignoreCase = true) ||
                            trimmed.contains("Done in", ignoreCase = true) ||
                            trimmed.contains("test passed", ignoreCase = true) -> OpenCodeMint

                    trimmed.startsWith(">") || trimmed.startsWith("$") -> OpenCodeCyan

                    trimmed.startsWith("├──") || trimmed.startsWith("└──") || trimmed.startsWith("│") -> OpenCodePurple

                    else -> Color(0xFFD1D5DB)
                }

                // If search query is present, highlight matches
                if (searchQuery.isNotBlank() && rawLine.contains(searchQuery, ignoreCase = true)) {
                    var startIndex = 0
                    while (startIndex < rawLine.length) {
                        val matchIndex = rawLine.indexOf(searchQuery, startIndex, ignoreCase = true)
                        if (matchIndex == -1) {
                            withStyle(SpanStyle(color = baseColor)) {
                                append(rawLine.substring(startIndex))
                            }
                            break
                        }

                        if (matchIndex > startIndex) {
                            withStyle(SpanStyle(color = baseColor)) {
                                append(rawLine.substring(startIndex, matchIndex))
                            }
                        }

                        withStyle(
                            SpanStyle(
                                color = Color.Black,
                                background = OpenCodeAmber,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(rawLine.substring(matchIndex, matchIndex + searchQuery.length))
                        }

                        startIndex = matchIndex + searchQuery.length
                    }
                } else {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(rawLine)
                    }
                }
            }

            Text(
                text = annotated,
                style = MonospaceCodeStyle.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

/**
 * Terminal ASCII welcome splash.
 */
@Composable
private fun TerminalSystemWelcomeMessage() {
    Column {
        Text(
            text = "┌──────────────────────────────────────────────────────────┐",
            style = MonospaceCodeStyle.copy(fontSize = 10.sp, color = OpenCodeCyan.copy(alpha = 0.5f))
        )
        Text(
            text = "│  OpenCode Autonomous Shell Execution Engine v1.0.0      │",
            style = MonospaceCodeStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OpenCodeCyan)
        )
        Text(
            text = "│  Linux 6.6.1-x86_64 • Monospace stdout/stderr console    │",
            style = MonospaceCodeStyle.copy(fontSize = 10.sp, color = Color(0xFF94A3B8))
        )
        Text(
            text = "└──────────────────────────────────────────────────────────┘",
            style = MonospaceCodeStyle.copy(fontSize = 10.sp, color = OpenCodeCyan.copy(alpha = 0.5f))
        )
    }
}

/**
 * Empty placeholder when search returns no logs.
 */
@Composable
private fun TerminalEmptyBanner(searchActive: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = OpenCodeCyan.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (searchActive) "No logs matching current filter" else "Terminal output buffer is empty",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (searchActive) "Clear the search query above" else "Run a command or launch tests from below",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF64748B)
            )
        }
    }
}
