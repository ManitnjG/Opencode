package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.AgentMessageEntity
import com.example.data.model.AgentSessionEntity
import com.example.data.model.GitSnapshotEntity
import com.example.data.model.PlanTaskEntity
import com.example.data.model.PlanTaskPriority
import com.example.data.model.ProjectEntity
import com.example.data.model.TerminalLogEntity
import com.example.data.model.WorkspaceFileEntity
import com.example.util.ProjectTemplates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class OpenCodeRepository(private val dao: AppDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun ensureDefaultProjects() {
        val existing = dao.getAllProjects().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultData = ProjectTemplates.getDefaultProjects()
            for ((project, files) in defaultData) {
                dao.insertProject(project)
                dao.insertFiles(files)

                // Insert initial welcoming session
                val session = AgentSessionEntity(
                    projectId = project.id,
                    title = "Initial Project Setup & Tour",
                    mode = "BUILD",
                    status = "COMPLETED"
                )
                dao.insertSession(session)

                dao.insertMessage(
                    AgentMessageEntity(
                        sessionId = session.id,
                        sender = "SYSTEM",
                        content = "OpenCode Autonomous Agent initialized for '${project.name}'. Operating in Build Mode with local LSP diagnostics and Git snapshotting enabled."
                    )
                )

                // Add initial git commit
                dao.insertSnapshot(
                    GitSnapshotEntity(
                        projectId = project.id,
                        commitHash = UUID.randomUUID().toString().take(7),
                        message = "Initial workspace commit",
                        filesChanged = files.size,
                        additions = files.sumOf { it.content.lines().size },
                        deletions = 0,
                        diffContent = "Initial commit with ${files.size} workspace files."
                    )
                )

                // Seed Plan Tasks
                seedDefaultPlanTasksForProject(project)
            }
        }
    }

    suspend fun createProject(name: String, description: String, templateType: String): ProjectEntity {
        val newProjId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = newProjId,
            name = name,
            description = description,
            templateType = templateType
        )
        dao.insertProject(project)

        val starterFiles = ProjectTemplates.getFilesForNewProject(
            projectId = newProjId,
            name = name,
            description = description,
            templateType = templateType
        )
        dao.insertFiles(starterFiles)

        val session = AgentSessionEntity(
            projectId = newProjId,
            title = "New Agent Workspace",
            mode = "BUILD"
        )
        dao.insertSession(session)

        // Seed Plan Tasks
        seedDefaultPlanTasksForProject(project)

        return project
    }

    fun getFiles(projectId: String): Flow<List<WorkspaceFileEntity>> = dao.getFilesForProject(projectId)

    suspend fun saveFile(file: WorkspaceFileEntity) = dao.insertFile(file)

    suspend fun updateFile(file: WorkspaceFileEntity) = dao.updateFile(file)

    suspend fun deleteFile(id: String) = dao.deleteFile(id)

    fun getSessions(projectId: String): Flow<List<AgentSessionEntity>> = dao.getSessionsForProject(projectId)

    suspend fun createSession(projectId: String, title: String, mode: String, model: String): AgentSessionEntity {
        val session = AgentSessionEntity(
            projectId = projectId,
            title = title,
            mode = mode,
            model = model
        )
        dao.insertSession(session)
        return session
    }

    suspend fun updateSession(session: AgentSessionEntity) = dao.updateSession(session)

    fun getMessages(sessionId: String): Flow<List<AgentMessageEntity>> = dao.getMessagesForSession(sessionId)

    suspend fun addMessage(message: AgentMessageEntity) = dao.insertMessage(message)

    fun getSnapshots(projectId: String): Flow<List<GitSnapshotEntity>> = dao.getSnapshotsForProject(projectId)

    suspend fun addSnapshot(snapshot: GitSnapshotEntity) = dao.insertSnapshot(snapshot)

    fun getTerminalLogs(projectId: String): Flow<List<TerminalLogEntity>> = dao.getTerminalLogs(projectId)

    suspend fun addTerminalLog(log: TerminalLogEntity) = dao.insertTerminalLog(log)

    suspend fun clearTerminalLogs(projectId: String) = dao.clearTerminalLogs(projectId)

    // --- Plan Tasks ---
    fun getPlanTasks(projectId: String): Flow<List<PlanTaskEntity>> = dao.getPlanTasksForProject(projectId)

    suspend fun addPlanTask(task: PlanTaskEntity) = dao.insertPlanTask(task)

    suspend fun updatePlanTask(task: PlanTaskEntity) = dao.updatePlanTask(task)

    suspend fun deletePlanTask(id: String) = dao.deletePlanTask(id)

    suspend fun deleteCompletedTasks(projectId: String) = dao.deleteCompletedTasks(projectId)

    suspend fun seedDefaultPlanTasksForProject(project: ProjectEntity) {
        val existing = dao.getPlanTasksForProject(project.id).firstOrNull()
        if (existing.isNullOrEmpty()) {
            val tasks = generateStarterPlanTasks(project)
            dao.insertPlanTasks(tasks)
        }
    }

    suspend fun regenerateAiPlan(project: ProjectEntity, customGoal: String? = null): List<PlanTaskEntity> {
        dao.deleteAllTasksForProject(project.id)
        val newTasks = generateStarterPlanTasks(project, customGoal)
        dao.insertPlanTasks(newTasks)
        return newTasks
    }

    private fun generateStarterPlanTasks(project: ProjectEntity, customGoal: String? = null): List<PlanTaskEntity> {
        val pid = project.id
        val template = project.templateType.lowercase()

        return when {
            template.contains("next") -> listOf(
                PlanTaskEntity(
                    projectId = pid,
                    title = "Analyze project architecture and define API boundary",
                    description = "Inspect Next.js App Router directories, verify Server Component boundaries, and configure strict TypeScript interfaces.",
                    priority = PlanTaskPriority.CRITICAL,
                    isCompleted = true,
                    phase = "Phase 1: Architecture & Setup",
                    impactedFiles = "src/app/page.tsx, src/types.ts",
                    orderIndex = 0
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Implement dynamic streaming dashboard UI with Tailwind",
                    description = "Construct responsive metrics cards, real-time agent execution visualizer, and dark-themed monospaced code viewer components.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = true,
                    phase = "Phase 2: Core Components",
                    impactedFiles = "src/app/page.tsx, tailwind.config.js",
                    orderIndex = 1
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Connect API route handlers with input validation",
                    description = "Build secure server endpoints for workspace operations with JSON schema parsing and error handling middleware.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 2: Core Components",
                    impactedFiles = "src/app/api/tasks/route.ts",
                    orderIndex = 2
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Write Jest / Vitest unit tests for state reducers",
                    description = "Add unit tests covering edge cases in state transitions, optimistic UI updates, and API failure fallbacks.",
                    priority = PlanTaskPriority.MEDIUM,
                    isCompleted = false,
                    phase = "Phase 3: Testing & Security",
                    impactedFiles = "tests/dashboard.test.tsx",
                    orderIndex = 3
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Optimize Next.js build bundle & verify Core Web Vitals",
                    description = "Audit bundle sizes with dynamic imports, verify zero hydration mismatches, and run production build check.",
                    priority = PlanTaskPriority.LOW,
                    isCompleted = false,
                    phase = "Phase 4: Optimization & Docs",
                    impactedFiles = "next.config.js, README.md",
                    orderIndex = 4
                )
            )
            template.contains("fastapi") || template.contains("python") -> listOf(
                PlanTaskEntity(
                    projectId = pid,
                    title = "Set up Pydantic v2 schemas and async database engine",
                    description = "Define typed domain entities with field validations, serialization rules, and async session factories.",
                    priority = PlanTaskPriority.CRITICAL,
                    isCompleted = true,
                    phase = "Phase 1: Architecture & Setup",
                    impactedFiles = "main.py, models.py",
                    orderIndex = 0
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Implement RESTful CRUD endpoints with dependency injection",
                    description = "Create FastAPI routers for resources, authentication dependencies, and structured exception handlers.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 2: Core Components",
                    impactedFiles = "main.py, routers/items.py",
                    orderIndex = 1
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Write Pytest test suite with AsyncClient fixtures",
                    description = "Implement integration tests with mock sessions, testing auth rejection, concurrency, and validation errors.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 3: Testing & Security",
                    impactedFiles = "tests/test_api.py",
                    orderIndex = 2
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Configure OpenAPI metadata and Swagger UI documentation",
                    description = "Add endpoint tags, response models, parameter descriptions, and export OpenAPI 3.1 JSON specification.",
                    priority = PlanTaskPriority.LOW,
                    isCompleted = false,
                    phase = "Phase 4: Optimization & Docs",
                    impactedFiles = "main.py, README.md",
                    orderIndex = 3
                )
            )
            template.contains("compose") || template.contains("kotlin") -> listOf(
                PlanTaskEntity(
                    projectId = pid,
                    title = "Define Clean Architecture layers & Compose UI state models",
                    description = "Structure domain repositories, Room DAOs, and sealed UI State hierarchies for deterministic rendering.",
                    priority = PlanTaskPriority.CRITICAL,
                    isCompleted = true,
                    phase = "Phase 1: Architecture & Setup",
                    impactedFiles = "ui/OpenCodeApp.kt, data/model/Entities.kt",
                    orderIndex = 0
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Build Material 3 Composables with edge-to-edge support",
                    description = "Implement responsive cards, animatable checklists, priority chips, and monospaced typography themes.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 2: Core Components",
                    impactedFiles = "ui/screens/PlanModeScreen.kt",
                    orderIndex = 1
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Implement ViewModel StateFlow pipelines & Room persistence",
                    description = "Wire Kotlin Coroutines and StateFlow to maintain reactive state across tab switches and lifecycle events.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 2: Core Components",
                    impactedFiles = "ui/OpenCodeViewModel.kt",
                    orderIndex = 2
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Add Robolectric unit tests for business logic & state flows",
                    description = "Test repository caching, task toggling, priority ordering, and AI plan generation logic.",
                    priority = PlanTaskPriority.MEDIUM,
                    isCompleted = false,
                    phase = "Phase 3: Testing & Security",
                    impactedFiles = "test/ExampleRobolectricTest.kt",
                    orderIndex = 3
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Verify accessibility touch targets & dark theme contrast",
                    description = "Audit 48dp minimum touch boundaries, high-contrast text color tokens, and smooth Compose transitions.",
                    priority = PlanTaskPriority.LOW,
                    isCompleted = false,
                    phase = "Phase 4: Optimization & Docs",
                    impactedFiles = "ui/theme/Theme.kt",
                    orderIndex = 4
                )
            )
            else -> listOf(
                PlanTaskEntity(
                    projectId = pid,
                    title = "Initialize project skeleton and dependency configurations",
                    description = "Verify package manifests, compile targets, and set up environment variable configuration.",
                    priority = PlanTaskPriority.CRITICAL,
                    isCompleted = true,
                    phase = "Phase 1: Architecture & Setup",
                    impactedFiles = "package.json / go.mod / Cargo.toml",
                    orderIndex = 0
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Implement core business domain logic and services",
                    description = "Write modular service handlers with strict error handling, logging, and state synchronization.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 2: Core Components",
                    impactedFiles = "src/service.ts / main.go",
                    orderIndex = 1
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Write automated unit test suite covering key edge cases",
                    description = "Set up testing runner and assert success/failure branches for all critical service methods.",
                    priority = PlanTaskPriority.HIGH,
                    isCompleted = false,
                    phase = "Phase 3: Testing & Security",
                    impactedFiles = "tests/service.test.ts",
                    orderIndex = 2
                ),
                PlanTaskEntity(
                    projectId = pid,
                    title = "Generate project documentation, API contracts, and guides",
                    description = "Write markdown guides, usage instructions, and deployment steps in README.md and AGENTS.md.",
                    priority = PlanTaskPriority.LOW,
                    isCompleted = false,
                    phase = "Phase 4: Optimization & Docs",
                    impactedFiles = "README.md, AGENTS.md",
                    orderIndex = 3
                )
            )
        }
    }

    suspend fun updateProjectRules(project: ProjectEntity, rules: String) {
        val updated = project.copy(agentsRules = rules)
        dao.updateProject(updated)

        // Also sync with AGENTS.md file if present
        val agentsFile = dao.getFileByPath(project.id, "AGENTS.md")
        if (agentsFile != null) {
            dao.updateFile(agentsFile.copy(content = rules, updatedAt = System.currentTimeMillis()))
        }
    }
}
