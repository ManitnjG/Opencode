package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.ProjectEntity
import com.example.data.model.WorkspaceFileEntity
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import com.example.ui.theme.OpenCodeSurfaceVariant

@Composable
fun RulesSettingsScreen(
    currentProject: ProjectEntity?,
    workspaceFiles: List<WorkspaceFileEntity>,
    snapshotCount: Int,
    onSaveRules: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rulesText by remember(currentProject?.agentsRules) {
        mutableStateOf(currentProject?.agentsRules ?: "")
    }
    val scrollState = rememberScrollState()

    val totalCodeLines = remember(workspaceFiles) {
        workspaceFiles.sumOf { it.content.lines().size }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // Section: Workspace Stats
        Text(
            text = "WORKSPACE OVERVIEW",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "Files",
                value = workspaceFiles.size.toString(),
                accent = OpenCodeCyan,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Code Lines",
                value = totalCodeLines.toString(),
                accent = OpenCodeMint,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Snapshots",
                value = snapshotCount.toString(),
                accent = OpenCodePurple,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: AGENTS.md Rules
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AGENTS.md System Guidelines",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Persistent instructions ingested on every agent call",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    onSaveRules(rulesText)
                    Toast.makeText(context, "AGENTS.md rules saved successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OpenCodeCyan),
                modifier = Modifier.testTag("save_rules_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = Color.Black,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Rules", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = rulesText,
            onValueChange = { rulesText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("agents_rules_input"),
            textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OpenCodeCyan,
                unfocusedBorderColor = OpenCodeBorder,
                focusedContainerColor = Color(0xFF070B12),
                unfocusedContainerColor = Color(0xFF070B12)
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Autonomous Agent Architecture
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = OpenCodeSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = OpenCodePurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About OpenCode Autonomous Agent",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val features = listOf(
                    "Dual Plan & Build execution modes for safe architecture exploration.",
                    "Real-time unified diff generator tracking atomic additions and deletions.",
                    "Local Room SQLite workspace persistence and Git commit rollback.",
                    "Built-in interactive shell sandbox with npm, pytest, and cargo runners.",
                    "Integrated LSP diagnostics analyzing syntax and type integrity."
                )

                features.forEach { feat ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = OpenCodeMint,
                            modifier = Modifier.size(15.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feat,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = OpenCodeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accent
            )
        }
    }
}
