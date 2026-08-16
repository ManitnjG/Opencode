package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitSnapshotEntity
import com.example.ui.components.GitBranchGraphView
import com.example.ui.theme.DiffAddBackground
import com.example.ui.theme.DiffAddText
import com.example.ui.theme.DiffDeleteBackground
import com.example.ui.theme.DiffDeleteText
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import com.example.ui.theme.OpenCodeSurfaceVariant
import java.util.Date

@Composable
fun GitDiffsScreen(
    snapshots: List<GitSnapshotEntity>,
    onRollbackSnapshot: (GitSnapshotEntity) -> Unit,
    onTakeSnapshot: (message: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSnapshot by remember { mutableStateOf(snapshots.firstOrNull()) }
    var rollbackConfirmSnapshot by remember { mutableStateOf<GitSnapshotEntity?>(null) }
    var viewMode by remember { mutableStateOf("diff") } // "diff" or "graph"
    var showTakeSnapshotDialog by remember { mutableStateOf(false) }
    var manualSnapshotMessage by remember { mutableStateOf("") }

    // Auto select first snapshot if null
    if (selectedSnapshot == null && snapshots.isNotEmpty()) {
        selectedSnapshot = snapshots.first()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = OpenCodeSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = OpenCodePurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Snapshots & Diffs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Manual snapshot creation button
                    Surface(
                        color = OpenCodeCyan.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { showTakeSnapshotDialog = true }
                            .testTag("take_snapshot_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Capture Snapshot",
                                tint = OpenCodeCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Capture",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OpenCodeCyan
                            )
                        }
                    }

                    // View mode toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OpenCodeSurfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (viewMode == "diff") OpenCodePurple else Color.Transparent)
                                .clickable { viewMode = "diff" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Diffs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (viewMode == "diff") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (viewMode == "graph") OpenCodePurple else Color.Transparent)
                                .clickable { viewMode = "graph" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountTree, contentDescription = null, tint = if (viewMode == "graph") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Graph", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (viewMode == "graph") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        if (snapshots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Commit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Git Snapshots Yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build mode agent actions automatically record atomic Git snapshots.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (viewMode == "graph") {
            GitBranchGraphView(
                snapshots = snapshots,
                selectedSnapshot = selectedSnapshot,
                onSelectSnapshot = { selectedSnapshot = it },
                onRequestRollback = { rollbackConfirmSnapshot = it }
            )
        } else {
            // Split or Vertical list: Top is Timeline of Snapshots, Bottom is the Unified Diff Viewer
            Column(modifier = Modifier.fillMaxSize()) {
                // Snapshot selector list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    items(snapshots, key = { it.id }) { snap ->
                        val isSelected = snap.id == selectedSnapshot?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedSnapshot = snap },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OpenCodeSurfaceElevated else OpenCodeSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) OpenCodeCyan else OpenCodeBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) OpenCodeCyan.copy(alpha = 0.2f) else OpenCodeSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Commit,
                                            contentDescription = null,
                                            tint = if (isSelected) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = snap.commitHash,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = OpenCodeCyan
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = Date(snap.timestamp).toString().take(19),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = snap.message,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+${snap.additions}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DiffAddText
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "-${snap.deletions}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DiffDeleteText
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedButton(
                                        onClick = { rollbackConfirmSnapshot = snap },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restore,
                                            contentDescription = "Rollback",
                                            tint = OpenCodeAmber,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Revert", fontSize = 10.sp, color = OpenCodeAmber)
                                    }
                                }
                            }
                        }
                    }
                }

                // Diff Viewer for selected snapshot
                Surface(
                    color = Color(0xFF070B12),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.55f)
                ) {
                    val activeSnap = selectedSnapshot
                    if (activeSnap != null) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "UNIFIED DIFF: ${activeSnap.commitHash}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = OpenCodeCyan
                                )
                                Text(
                                    text = "${activeSnap.filesChanged} file(s) changed (+${activeSnap.additions} -${activeSnap.deletions})",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                            ) {
                                activeSnap.diffContent.lines().forEach { line ->
                                    val isAdd = line.startsWith("+")
                                    val isDel = line.startsWith("-")
                                    val isHeader = line.startsWith("@") || line.startsWith("---") || line.startsWith("+++")

                                    val bg = when {
                                        isAdd -> DiffAddBackground
                                        isDel -> DiffDeleteBackground
                                        else -> Color.Transparent
                                    }

                                    val textCol = when {
                                        isAdd -> DiffAddText
                                        isDel -> DiffDeleteText
                                        isHeader -> OpenCodeCyan
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(bg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = line,
                                            style = MonospaceCodeStyle.copy(
                                                fontSize = 11.sp,
                                                color = textCol
                                            )
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

    // Rollback confirmation dialog
    if (rollbackConfirmSnapshot != null) {
        AlertDialog(
            onDismissRequest = { rollbackConfirmSnapshot = null },
            title = { Text("Rollback to Snapshot?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Revert project workspace to commit '${rollbackConfirmSnapshot?.commitHash}': ${rollbackConfirmSnapshot?.message}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        rollbackConfirmSnapshot?.let { onRollbackSnapshot(it) }
                        rollbackConfirmSnapshot = null
                        Toast.makeText(context, "Rolled back to snapshot successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OpenCodeAmber)
                ) {
                    Text("Confirm Revert", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rollbackConfirmSnapshot = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Capture Snapshot dialog
    if (showTakeSnapshotDialog) {
        AlertDialog(
            onDismissRequest = {
                showTakeSnapshotDialog = false
                manualSnapshotMessage = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = OpenCodeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capture Git Snapshot", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Save the current project state and file changes to the local Room database as an atomic Git snapshot.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualSnapshotMessage,
                        onValueChange = { manualSnapshotMessage = it },
                        label = { Text("Commit / Snapshot Message") },
                        placeholder = { Text("e.g. Added auth handlers & UI improvements") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_snapshot_message_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = manualSnapshotMessage.ifBlank { "Manual project snapshot" }
                        onTakeSnapshot(msg)
                        showTakeSnapshotDialog = false
                        manualSnapshotMessage = ""
                        Toast.makeText(context, "Captured snapshot: '$msg'", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OpenCodeCyan)
                ) {
                    Text("Save Snapshot", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTakeSnapshotDialog = false
                    manualSnapshotMessage = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
