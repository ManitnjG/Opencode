package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AgentMessageEntity
import com.example.data.model.AgentMode
import com.example.data.model.AgentSessionEntity
import com.example.data.model.DiagnosticItem
import com.example.data.model.GitSnapshotEntity
import com.example.data.model.PlanTaskEntity
import com.example.data.model.PlanTaskPriority
import com.example.data.model.ProjectEntity
import com.example.data.model.TerminalLogEntity
import com.example.data.model.WorkspaceFileEntity
import com.example.data.repository.OpenCodeRepository
import com.example.engine.GeminiAgentEngine
import com.example.engine.LspEngine
import com.example.engine.TerminalEngine
import com.example.util.DiffUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class AppTab {
    PLAN_MODE,
    AGENT_CHAT,
    CODE_EDITOR,
    GIT_DIFFS,
    TERMINAL,
    RULES_SETTINGS
}

class OpenCodeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OpenCodeRepository
    private val agentEngine = GeminiAgentEngine()
    private val terminalEngine = TerminalEngine()
    private val lspEngine = LspEngine()

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _allProjects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val allProjects: StateFlow<List<ProjectEntity>> = _allProjects.asStateFlow()

    private val _workspaceFiles = MutableStateFlow<List<WorkspaceFileEntity>>(emptyList())
    val workspaceFiles: StateFlow<List<WorkspaceFileEntity>> = _workspaceFiles.asStateFlow()

    private val _activeFile = MutableStateFlow<WorkspaceFileEntity?>(null)
    val activeFile: StateFlow<WorkspaceFileEntity?> = _activeFile.asStateFlow()

    private val _planTasks = MutableStateFlow<List<PlanTaskEntity>>(emptyList())
    val planTasks: StateFlow<List<PlanTaskEntity>> = _planTasks.asStateFlow()

    private val _currentSession = MutableStateFlow<AgentSessionEntity?>(null)
    val currentSession: StateFlow<AgentSessionEntity?> = _currentSession.asStateFlow()

    private val _allSessions = MutableStateFlow<List<AgentSessionEntity>>(emptyList())
    val allSessions: StateFlow<List<AgentSessionEntity>> = _allSessions.asStateFlow()

    private val _messages = MutableStateFlow<List<AgentMessageEntity>>(emptyList())
    val messages: StateFlow<List<AgentMessageEntity>> = _messages.asStateFlow()

    private val _snapshots = MutableStateFlow<List<GitSnapshotEntity>>(emptyList())
    val snapshots: StateFlow<List<GitSnapshotEntity>> = _snapshots.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLogEntity>>(emptyList())
    val terminalLogs: StateFlow<List<TerminalLogEntity>> = _terminalLogs.asStateFlow()

    private val _diagnostics = MutableStateFlow<List<DiagnosticItem>>(emptyList())
    val diagnostics: StateFlow<List<DiagnosticItem>> = _diagnostics.asStateFlow()

    private val _agentMode = MutableStateFlow(AgentMode.BUILD)
    val agentMode: StateFlow<AgentMode> = _agentMode.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isAgentThinking = MutableStateFlow(false)
    val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

    private val _agentStatusStep = MutableStateFlow("")
    val agentStatusStep: StateFlow<String> = _agentStatusStep.asStateFlow()

    private val _activeTab = MutableStateFlow(AppTab.AGENT_CHAT)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _isVimModeEnabled = MutableStateFlow(false)
    val isVimModeEnabled: StateFlow<Boolean> = _isVimModeEnabled.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = OpenCodeRepository(db.appDao())

        viewModelScope.launch {
            repository.ensureDefaultProjects()

            repository.allProjects.collect { projects ->
                _allProjects.value = projects
                if (_currentProject.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        _currentProject.value = project
        viewModelScope.launch {
            // Collect files
            launch {
                repository.getFiles(project.id).collect { files ->
                    _workspaceFiles.value = files
                    if (_activeFile.value == null || !_workspaceFiles.value.any { it.id == _activeFile.value?.id }) {
                        _activeFile.value = files.firstOrNull { !it.filePath.endsWith(".md") } ?: files.firstOrNull()
                    }
                    _diagnostics.value = lspEngine.analyzeDiagnostics(files)
                }
            }

            // Collect sessions
            launch {
                repository.getSessions(project.id).collect { sessions ->
                    _allSessions.value = sessions
                    if (sessions.isNotEmpty()) {
                        val session = sessions.first()
                        _currentSession.value = session
                        listenToMessages(session.id)
                    } else {
                        val newSession = repository.createSession(
                            projectId = project.id,
                            title = "Autonomous Coding Session",
                            mode = _agentMode.value.name,
                            model = _selectedModel.value
                        )
                        _currentSession.value = newSession
                        listenToMessages(newSession.id)
                    }
                }
            }

            // Collect snapshots
            launch {
                repository.getSnapshots(project.id).collect { snaps ->
                    _snapshots.value = snaps
                }
            }

            // Collect terminal logs
            launch {
                repository.getTerminalLogs(project.id).collect { logs ->
                    _terminalLogs.value = logs
                }
            }

            // Collect plan tasks
            launch {
                repository.getPlanTasks(project.id).collect { tasks ->
                    _planTasks.value = tasks
                }
            }
        }
    }

    private fun listenToMessages(sessionId: String) {
        viewModelScope.launch {
            repository.getMessages(sessionId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun setActiveTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun setAgentMode(mode: AgentMode) {
        _agentMode.value = mode
        val session = _currentSession.value
        if (session != null) {
            viewModelScope.launch {
                repository.updateSession(session.copy(mode = mode.name, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun toggleVimMode() {
        _isVimModeEnabled.value = !_isVimModeEnabled.value
    }

    fun selectActiveFile(file: WorkspaceFileEntity) {
        _activeFile.value = file
    }

    fun updateActiveFileContent(newContent: String) {
        val file = _activeFile.value ?: return
        val updated = file.copy(content = newContent, updatedAt = System.currentTimeMillis())
        _activeFile.value = updated
        viewModelScope.launch {
            repository.updateFile(updated)
        }
    }

    fun createNewFile(filePath: String, language: String, content: String = "") {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val newFile = WorkspaceFileEntity(
                projectId = proj.id,
                filePath = filePath,
                language = language,
                content = content
            )
            repository.saveFile(newFile)
            _activeFile.value = newFile
        }
    }

    fun deleteWorkspaceFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            if (_activeFile.value?.id == fileId) {
                _activeFile.value = _workspaceFiles.value.firstOrNull { it.id != fileId }
            }
        }
    }

    fun createNewProject(name: String, description: String, templateType: String) {
        viewModelScope.launch {
            val project = repository.createProject(name, description, templateType)
            selectProject(project)
        }
    }

    fun sendAgentPrompt(prompt: String) {
        val project = _currentProject.value ?: return
        val session = _currentSession.value ?: return
        if (prompt.isBlank() || _isAgentThinking.value) return

        viewModelScope.launch {
            _isAgentThinking.value = true
            _agentStatusStep.value = "Ingesting prompt & preparing agent workspace..."

            // Record user message
            val userMsg = AgentMessageEntity(
                sessionId = session.id,
                sender = "USER",
                content = prompt
            )
            repository.addMessage(userMsg)

            try {
                val result = agentEngine.executeTask(
                    prompt = prompt,
                    mode = _agentMode.value,
                    modelName = _selectedModel.value,
                    projectRules = project.agentsRules,
                    files = _workspaceFiles.value,
                    activeFile = _activeFile.value,
                    onStepUpdate = { step -> _agentStatusStep.value = step }
                )

                // Update files in Room if Build mode applied edits
                for (updated in result.updatedFiles) {
                    repository.updateFile(updated)
                    if (_activeFile.value?.filePath == updated.filePath) {
                        _activeFile.value = updated
                    }
                }

                // Add snapshot if commit message present
                if (result.snapshotCommitMessage != null && result.diffItems.isNotEmpty()) {
                    val diff = result.diffItems.first()
                    val snapshot = GitSnapshotEntity(
                        projectId = project.id,
                        commitHash = UUID.randomUUID().toString().take(7),
                        message = result.snapshotCommitMessage,
                        filesChanged = result.updatedFiles.size.coerceAtLeast(1),
                        additions = diff.additions,
                        deletions = diff.deletions,
                        diffContent = DiffUtils.formatUnifiedDiff(diff)
                    )
                    repository.addSnapshot(snapshot)
                }

                // Serialize tool calls to JSON for UI cards
                val toolsJson = JSONArray().apply {
                    result.toolExecutions.forEach { t ->
                        put(JSONObject().apply {
                            put("tool", t.toolName)
                            put("input", t.input)
                            put("output", t.output)
                            put("success", t.isSuccess)
                            put("duration", "${t.durationMs}ms")
                        })
                    }
                }.toString()

                val diffJson = if (result.diffItems.isNotEmpty()) {
                    val d = result.diffItems.first()
                    JSONObject().apply {
                        put("file", d.filePath)
                        put("additions", d.additions)
                        put("deletions", d.deletions)
                        put("diff", DiffUtils.formatUnifiedDiff(d))
                    }.toString()
                } else null

                val agentMsg = AgentMessageEntity(
                    sessionId = session.id,
                    sender = "AGENT",
                    content = result.finalContent,
                    reasoning = result.reasoning,
                    toolCallsJson = toolsJson,
                    diffSnapshotJson = diffJson
                )
                repository.addMessage(agentMsg)

            } catch (e: Exception) {
                val errorMsg = AgentMessageEntity(
                    sessionId = session.id,
                    sender = "SYSTEM",
                    content = "⚠️ Agent encounter: ${e.localizedMessage ?: "Unknown error occurred"}"
                )
                repository.addMessage(errorMsg)
            } finally {
                _isAgentThinking.value = false
                _agentStatusStep.value = ""
            }
        }
    }

    fun executeTerminalCommand(command: String) {
        val project = _currentProject.value ?: return
        if (command.isBlank()) return

        viewModelScope.launch {
            if (command.trim().equals("clear", ignoreCase = true)) {
                repository.clearTerminalLogs(project.id)
                return@launch
            }

            val out = terminalEngine.executeCommand(
                command = command,
                projectId = project.id,
                files = _workspaceFiles.value,
                snapshots = _snapshots.value
            )

            val log = TerminalLogEntity(
                projectId = project.id,
                command = out.command,
                output = out.output,
                exitCode = out.exitCode
            )
            repository.addTerminalLog(log)
        }
    }

    fun rollbackToSnapshot(snapshot: GitSnapshotEntity) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            // Restore snapshot notification
            val session = _currentSession.value ?: return@launch
            repository.addMessage(
                AgentMessageEntity(
                    sessionId = session.id,
                    sender = "SYSTEM",
                    content = "⏪ Rolled back project state to snapshot: '${snapshot.message}' (${snapshot.commitHash})"
                )
            )

            repository.addTerminalLog(
                TerminalLogEntity(
                    projectId = project.id,
                    command = "git checkout ${snapshot.commitHash}",
                    output = "HEAD is now at ${snapshot.commitHash} ${snapshot.message}",
                    exitCode = 0
                )
            )
        }
    }

    fun fixDiagnosticWithAgent(diag: DiagnosticItem) {
        _activeTab.value = AppTab.AGENT_CHAT
        sendAgentPrompt("Fix diagnostic error in ${diag.filePath} at line ${diag.lineNumber}: ${diag.message} (${diag.source})")
    }

    fun updateProjectRules(newRules: String) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            repository.updateProjectRules(project, newRules)
            _currentProject.value = project.copy(agentsRules = newRules)
        }
    }

    fun togglePlanTaskCompletion(task: PlanTaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updatePlanTask(updated)
        }
    }

    fun addPlanTask(
        title: String,
        description: String,
        priority: PlanTaskPriority,
        phase: String,
        impactedFiles: String
    ) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val nextIndex = (_planTasks.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
            val newTask = PlanTaskEntity(
                projectId = project.id,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                phase = phase.ifBlank { "Phase 1: Architecture & Setup" },
                impactedFiles = impactedFiles.trim(),
                orderIndex = nextIndex
            )
            repository.addPlanTask(newTask)
        }
    }

    fun updatePlanTask(task: PlanTaskEntity) {
        viewModelScope.launch {
            repository.updatePlanTask(task)
        }
    }

    fun deletePlanTask(taskId: String) {
        viewModelScope.launch {
            repository.deletePlanTask(taskId)
        }
    }

    fun deleteCompletedTasks() {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            repository.deleteCompletedTasks(project.id)
        }
    }

    fun regenerateAiPlan(customGoal: String? = null) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            repository.regenerateAiPlan(project, customGoal)
        }
    }

    fun executePlanTaskInBuildMode(task: PlanTaskEntity) {
        setAgentMode(AgentMode.BUILD)
        _activeTab.value = AppTab.AGENT_CHAT
        val prompt = buildString {
            append("Execute autonomous task from Plan Mode:\n")
            append("• Task: ${task.title}\n")
            if (task.description.isNotBlank()) {
                append("• Description: ${task.description}\n")
            }
            append("• Phase: ${task.phase}\n")
            append("• Priority: ${task.priority.name}\n")
            if (task.impactedFiles.isNotBlank()) {
                append("• Target files: ${task.impactedFiles}\n")
            }
            append("\nPlease make the necessary file updates, generate git snapshot diffs, and verify linter/tests.")
        }
        sendAgentPrompt(prompt)
    }

    fun createManualSnapshot(message: String) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val hash = UUID.randomUUID().toString().substring(0, 7)
            val currentFiles = _workspaceFiles.value
            val diffBuilder = StringBuilder()
            currentFiles.take(5).forEach { f ->
                diffBuilder.append("--- a/${f.filePath}\n+++ b/${f.filePath}\n@@ -1,5 +1,5 @@\n// Snapshot recorded at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}\n")
            }
            val newSnapshot = GitSnapshotEntity(
                projectId = project.id,
                commitHash = hash,
                message = message.ifBlank { "Manual project snapshot" },
                filesChanged = if (currentFiles.isNotEmpty()) currentFiles.size else 1,
                additions = (1..15).random(),
                deletions = (0..5).random(),
                diffContent = diffBuilder.toString()
            )
            repository.addSnapshot(newSnapshot)
        }
    }
}
