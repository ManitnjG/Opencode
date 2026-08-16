package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentMode
import com.example.data.model.ProjectEntity
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenCodeTopBar(
    currentProject: ProjectEntity?,
    allProjects: List<ProjectEntity>,
    agentMode: AgentMode,
    selectedModel: String,
    onSelectProject: (ProjectEntity) -> Unit,
    onNewProjectClick: () -> Unit,
    onModeChange: (AgentMode) -> Unit,
    onModelChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var projectMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = OpenCodeSurface,
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Top Row: Brand & Project Selector + Model Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Logo & Project dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { projectMenuExpanded = true }
                        .clip(RoundedCornerShape(8.dp))
                        .background(OpenCodeSurfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("project_selector_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(OpenCodeCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ">_",
                            color = OpenCodeCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "OpenCode",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Text(
                                text = currentProject?.name ?: "Select Project",
                                color = OpenCodeCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Project",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = projectMenuExpanded,
                        onDismissRequest = { projectMenuExpanded = false },
                        modifier = Modifier.background(OpenCodeSurface)
                    ) {
                        Text(
                            text = "PROJECT WORKSPACES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        allProjects.forEach { proj ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (proj.id == currentProject?.id) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = proj.name,
                                                fontWeight = if (proj.id == currentProject?.id) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = proj.templateType.uppercase(),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectProject(proj)
                                    projectMenuExpanded = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "New Project",
                                        tint = OpenCodeMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+ Create New Project",
                                        color = OpenCodeMint,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            onClick = {
                                projectMenuExpanded = false
                                onNewProjectClick()
                            }
                        )
                    }
                }

                // Right: Model Selector Pill
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(OpenCodePurple.copy(alpha = 0.15f))
                            .border(1.dp, OpenCodePurple.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { modelMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("model_selector_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Model",
                            tint = OpenCodePurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedModel) {
                                "gemini-3.5-flash" -> "Gemini 2.5 Flash"
                                "gemini-3.1-pro-preview" -> "Gemini Pro"
                                "claude-3.7-sonnet" -> "Claude 3.7 Sonnet"
                                "gpt-4o" -> "GPT-4o"
                                else -> "Ollama Local"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = OpenCodePurple
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = OpenCodePurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false },
                        modifier = Modifier.background(OpenCodeSurface)
                    ) {
                        listOf(
                            "gemini-3.5-flash" to "Gemini 2.5 Flash (Fast & Recommended)",
                            "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (Deep STEM Reasoning)",
                            "claude-3.7-sonnet" to "Claude 3.7 Sonnet (Coding Agent)",
                            "gpt-4o" to "GPT-4o (Omni Engine)",
                            "ollama-local" to "Ollama DeepSeek-R1 (Local On-Device)"
                        ).forEach { (modelKey, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            color = if (modelKey == selectedModel) OpenCodePurple else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (modelKey == selectedModel) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (modelKey == selectedModel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = OpenCodePurple,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onModelChange(modelKey)
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Dual Mode Switcher [ Plan | Build ] & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mode Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(OpenCodeSurfaceVariant)
                        .border(1.dp, OpenCodeBorder, RoundedCornerShape(18.dp))
                        .padding(2.dp)
                ) {
                    // Plan Mode Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (agentMode == AgentMode.PLAN) OpenCodeCyan.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .clickable { onModeChange(AgentMode.PLAN) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("mode_plan_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Plan Mode",
                            tint = if (agentMode == AgentMode.PLAN) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PLAN",
                            fontSize = 11.sp,
                            fontWeight = if (agentMode == AgentMode.PLAN) FontWeight.Bold else FontWeight.Normal,
                            color = if (agentMode == AgentMode.PLAN) OpenCodeCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Build Mode Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (agentMode == AgentMode.BUILD) OpenCodeMint.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .clickable { onModeChange(AgentMode.BUILD) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("mode_build_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Build Mode",
                            tint = if (agentMode == AgentMode.BUILD) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BUILD",
                            fontSize = 11.sp,
                            fontWeight = if (agentMode == AgentMode.BUILD) FontWeight.Bold else FontWeight.Normal,
                            color = if (agentMode == AgentMode.BUILD) OpenCodeMint else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mode Explanation / Status Info
                Text(
                    text = if (agentMode == AgentMode.PLAN) "Plan: Read-only blueprint" else "Build: Live code generation & git edits",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
