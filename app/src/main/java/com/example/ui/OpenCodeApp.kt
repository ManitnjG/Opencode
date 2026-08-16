package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AgentMode
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.OpenCodeTopBar
import com.example.ui.screens.AgentChatScreen
import com.example.ui.screens.CodeEditorScreen
import com.example.ui.screens.GitDiffsScreen
import com.example.ui.screens.PlanModeScreen
import com.example.ui.screens.RulesSettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceVariant

@Composable
fun OpenCodeApp(
    viewModel: OpenCodeViewModel = viewModel()
) {
    val currentProject by viewModel.currentProject.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val workspaceFiles by viewModel.workspaceFiles.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val planTasks by viewModel.planTasks.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val snapshots by viewModel.snapshots.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val agentMode by viewModel.agentMode.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isAgentThinking by viewModel.isAgentThinking.collectAsState()
    val agentStatusStep by viewModel.agentStatusStep.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isVimModeEnabled by viewModel.isVimModeEnabled.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OpenCodeTopBar(
                currentProject = currentProject,
                allProjects = allProjects,
                agentMode = agentMode,
                selectedModel = selectedModel,
                onSelectProject = { viewModel.selectProject(it) },
                onNewProjectClick = { showNewProjectDialog = true },
                onModeChange = { newMode ->
                    viewModel.setAgentMode(newMode)
                    if (newMode == AgentMode.PLAN) {
                        viewModel.setActiveTab(AppTab.PLAN_MODE)
                    } else if (activeTab == AppTab.PLAN_MODE) {
                        viewModel.setActiveTab(AppTab.AGENT_CHAT)
                    }
                },
                onModelChange = { viewModel.setSelectedModel(it) }
            )
        },
        bottomBar = {
            Surface(
                color = OpenCodeSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = OpenCodeSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    val pendingTasksCount = planTasks.count { !it.isCompleted }

                    NavigationBarItem(
                        selected = activeTab == AppTab.PLAN_MODE,
                        onClick = {
                            viewModel.setActiveTab(AppTab.PLAN_MODE)
                            viewModel.setAgentMode(AgentMode.PLAN)
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingTasksCount > 0) {
                                        Badge(
                                            containerColor = OpenCodeCyan,
                                            contentColor = Color.Black
                                        ) {
                                            Text(pendingTasksCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Plan Mode",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = { Text("Plan", fontSize = 11.sp, fontWeight = if (activeTab == AppTab.PLAN_MODE) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpenCodeCyan,
                            selectedTextColor = OpenCodeCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = OpenCodeCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_plan")
                    )

                    NavigationBarItem(
                        selected = activeTab == AppTab.AGENT_CHAT,
                        onClick = { viewModel.setActiveTab(AppTab.AGENT_CHAT) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Agent",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Agent", fontSize = 11.sp, fontWeight = if (activeTab == AppTab.AGENT_CHAT) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpenCodePurple,
                            selectedTextColor = OpenCodePurple,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = OpenCodePurple.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_agent")
                    )

                    NavigationBarItem(
                        selected = activeTab == AppTab.CODE_EDITOR,
                        onClick = { viewModel.setActiveTab(AppTab.CODE_EDITOR) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Editor",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Editor", fontSize = 11.sp, fontWeight = if (activeTab == AppTab.CODE_EDITOR) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpenCodeCyan,
                            selectedTextColor = OpenCodeCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = OpenCodeCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_editor")
                    )

                    NavigationBarItem(
                        selected = activeTab == AppTab.GIT_DIFFS,
                        onClick = { viewModel.setActiveTab(AppTab.GIT_DIFFS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (snapshots.isNotEmpty()) {
                                        Badge(
                                            containerColor = OpenCodeCyan,
                                            contentColor = Color.Black
                                        ) {
                                            Text(snapshots.size.toString(), fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Difference,
                                    contentDescription = "Snapshots",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = { Text("Snapshots", fontSize = 11.sp, fontWeight = if (activeTab == AppTab.GIT_DIFFS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpenCodeCyan,
                            selectedTextColor = OpenCodeCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = OpenCodeCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_snapshots")
                    )

                    NavigationBarItem(
                        selected = activeTab == AppTab.TERMINAL,
                        onClick = { viewModel.setActiveTab(AppTab.TERMINAL) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (diagnostics.isNotEmpty()) {
                                        Badge(
                                            containerColor = OpenCodeAmber,
                                            contentColor = Color.Black
                                        ) {
                                            Text(diagnostics.size.toString(), fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Terminal",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = { Text("Terminal", fontSize = 11.sp, fontWeight = if (activeTab == AppTab.TERMINAL) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpenCodeMint,
                            selectedTextColor = OpenCodeMint,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = OpenCodeMint.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_terminal")
                    )

                    NavigationBarItem(
                        selected = activeTab == AppTab.RULES_SETTINGS,
                        onClick = { viewModel.setActiveTab(AppTab.RULES_SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Rule,
                                contentDescription = "AGENTS.md",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Rules", fontSize = 11.sp, fontWeight = if (activeTab == AppTab.RULES_SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OpenCodeCyan,
                            selectedTextColor = OpenCodeCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = OpenCodeCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_rules")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = activeTab,
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.PLAN_MODE -> {
                    PlanModeScreen(
                        currentProject = currentProject,
                        tasks = planTasks,
                        onToggleTask = { viewModel.togglePlanTaskCompletion(it) },
                        onAddTask = { title, desc, priority, phase, files ->
                            viewModel.addPlanTask(title, desc, priority, phase, files)
                        },
                        onUpdateTask = { viewModel.updatePlanTask(it) },
                        onDeleteTask = { viewModel.deletePlanTask(it) },
                        onDeleteCompleted = { viewModel.deleteCompletedTasks() },
                        onRegeneratePlan = { viewModel.regenerateAiPlan(it) },
                        onExecuteTaskInBuildMode = { viewModel.executePlanTaskInBuildMode(it) }
                    )
                }

                AppTab.AGENT_CHAT -> {
                    AgentChatScreen(
                        messages = messages,
                        workspaceFiles = workspaceFiles,
                        activeFile = activeFile,
                        agentMode = agentMode,
                        isAgentThinking = isAgentThinking,
                        agentStatusStep = agentStatusStep,
                        onSendPrompt = { prompt -> viewModel.sendAgentPrompt(prompt) }
                    )
                }

                AppTab.CODE_EDITOR -> {
                    CodeEditorScreen(
                        files = workspaceFiles,
                        activeFile = activeFile,
                        isVimModeEnabled = isVimModeEnabled,
                        onSelectFile = { viewModel.selectActiveFile(it) },
                        onUpdateFileContent = { viewModel.updateActiveFileContent(it) },
                        onCreateNewFile = { path, lang, content -> viewModel.createNewFile(path, lang, content) },
                        onDeleteFile = { viewModel.deleteWorkspaceFile(it) },
                        onToggleVimMode = { viewModel.toggleVimMode() },
                        onAskAgentAboutFile = { file ->
                            viewModel.setActiveTab(AppTab.AGENT_CHAT)
                            viewModel.sendAgentPrompt("Refactor and review @${file.filePath}")
                        }
                    )
                }

                AppTab.GIT_DIFFS -> {
                    GitDiffsScreen(
                        snapshots = snapshots,
                        onRollbackSnapshot = { viewModel.rollbackToSnapshot(it) },
                        onTakeSnapshot = { viewModel.createManualSnapshot(it) }
                    )
                }

                AppTab.TERMINAL -> {
                    TerminalScreen(
                        terminalLogs = terminalLogs,
                        diagnostics = diagnostics,
                        onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                        onFixDiagnostic = { diag -> viewModel.fixDiagnosticWithAgent(diag) }
                    )
                }

                AppTab.RULES_SETTINGS -> {
                    RulesSettingsScreen(
                        currentProject = currentProject,
                        workspaceFiles = workspaceFiles,
                        snapshotCount = snapshots.size,
                        onSaveRules = { viewModel.updateProjectRules(it) }
                    )
                }
            }
        }
    }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreateProject = { name, desc, template ->
                viewModel.createNewProject(name, desc, template)
            }
        )
    }
}
