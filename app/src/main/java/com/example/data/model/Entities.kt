package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val templateType: String = "nextjs", // nextjs, fastapi, compose, rust, custom
    val createdAt: Long = System.currentTimeMillis(),
    val agentsRules: String = """
        # Project Guidelines
        - Follow strict TypeScript type checking.
        - Write unit tests for all core business logic.
        - In Plan Mode: draft architecture and impacted files before modifying.
        - In Build Mode: make minimal atomic edits and run linter verification.
    """.trimIndent()
)

@Entity(tableName = "workspace_files")
data class WorkspaceFileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val filePath: String,
    val content: String,
    val language: String, // typescript, python, kotlin, rust, markdown, json, bash
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_sessions")
data class AgentSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val mode: String = "BUILD", // PLAN or BUILD
    val status: String = "IDLE", // IDLE, PLANNING, EXECUTING, COMPLETED, ERROR
    val model: String = "gemini-3.5-flash",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_messages")
data class AgentMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val sender: String, // USER, AGENT, SYSTEM, TOOL
    val content: String,
    val reasoning: String? = null,
    val toolCallsJson: String? = null,
    val diffSnapshotJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "git_snapshots")
data class GitSnapshotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val commitHash: String,
    val message: String,
    val filesChanged: Int = 1,
    val additions: Int = 0,
    val deletions: Int = 0,
    val diffContent: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "terminal_logs")
data class TerminalLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val command: String,
    val output: String,
    val exitCode: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class ToolExecution(
    val toolName: String,
    val input: String,
    val output: String,
    val isSuccess: Boolean = true,
    val durationMs: Long = 120
)

data class DiffItem(
    val filePath: String,
    val oldContent: String,
    val newContent: String,
    val additions: Int,
    val deletions: Int,
    val diffLines: List<DiffLine>
)

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

enum class DiffLineType {
    SAME, ADD, DELETE, HEADER
}

enum class AgentMode {
    PLAN, BUILD
}

data class DiagnosticItem(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val lineNumber: Int,
    val severity: DiagnosticSeverity,
    val message: String,
    val source: String = "LSP / TypeScript Compiler"
)

enum class DiagnosticSeverity {
    ERROR, WARNING, INFO
}

enum class PlanTaskPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

@Entity(tableName = "plan_tasks")
data class PlanTaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val priority: PlanTaskPriority = PlanTaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val phase: String = "Phase 1: Architecture & Setup",
    val impactedFiles: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
