package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DiagnosticItem
import com.example.data.model.DiagnosticSeverity
import com.example.data.model.TerminalLogEntity
import com.example.ui.components.TerminalConsoleView
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

@Composable
fun TerminalScreen(
    terminalLogs: List<TerminalLogEntity>,
    diagnostics: List<DiagnosticItem>,
    onExecuteCommand: (String) -> Unit,
    onFixDiagnostic: (DiagnosticItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sub tabs: Terminal vs LSP Diagnostics
        Surface(
            color = OpenCodeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = OpenCodeSurface,
                contentColor = OpenCodeCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = OpenCodeCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedSubTab == 0) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Interactive Shell",
                                fontSize = 13.sp,
                                fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (diagnostics.isNotEmpty()) OpenCodeAmber else OpenCodeMint
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LSP Diagnostics (${diagnostics.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSubTab == 1) (if (diagnostics.isNotEmpty()) OpenCodeAmber else OpenCodeMint) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        if (selectedSubTab == 0) {
            // Interactive Shell Console using dedicated TerminalConsoleView component
            TerminalConsoleView(
                terminalLogs = terminalLogs,
                onExecuteCommand = onExecuteCommand,
                onClearLogs = { onExecuteCommand("clear") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                terminalTitle = "opencode@workspace: ~/src (bash)"
            )
        } else {
            // LSP Diagnostics Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (diagnostics.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = OpenCodeMint,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All LSP Diagnostics Clean",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "0 compiler errors, 0 warnings across all workspace files.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(diagnostics, key = { it.id }) { diag ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = OpenCodeSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = when (diag.severity) {
                                                    DiagnosticSeverity.ERROR -> Icons.Default.Error
                                                    DiagnosticSeverity.WARNING -> Icons.Default.Warning
                                                    DiagnosticSeverity.INFO -> Icons.Default.Info
                                                },
                                                contentDescription = null,
                                                tint = when (diag.severity) {
                                                    DiagnosticSeverity.ERROR -> OpenCodeRed
                                                    DiagnosticSeverity.WARNING -> OpenCodeAmber
                                                    DiagnosticSeverity.INFO -> OpenCodeCyan
                                                },
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${diag.filePath}:${diag.lineNumber}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = diag.source,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = diag.message,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { onFixDiagnostic(diag) },
                                        colors = ButtonDefaults.buttonColors(containerColor = OpenCodePurple),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Fix with OpenCode Agent",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
