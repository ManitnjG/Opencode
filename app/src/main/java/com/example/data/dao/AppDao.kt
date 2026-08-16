package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AgentMessageEntity
import com.example.data.model.AgentSessionEntity
import com.example.data.model.GitSnapshotEntity
import com.example.data.model.PlanTaskEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TerminalLogEntity
import com.example.data.model.WorkspaceFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)

    // --- Workspace Files ---
    @Query("SELECT * FROM workspace_files WHERE projectId = :projectId ORDER BY filePath ASC")
    fun getFilesForProject(projectId: String): Flow<List<WorkspaceFileEntity>>

    @Query("SELECT * FROM workspace_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): WorkspaceFileEntity?

    @Query("SELECT * FROM workspace_files WHERE projectId = :projectId AND filePath = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): WorkspaceFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: WorkspaceFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<WorkspaceFileEntity>)

    @Update
    suspend fun updateFile(file: WorkspaceFileEntity)

    @Query("DELETE FROM workspace_files WHERE id = :id")
    suspend fun deleteFile(id: String)

    @Query("DELETE FROM workspace_files WHERE projectId = :projectId")
    suspend fun deleteAllFilesForProject(projectId: String)

    // --- Sessions ---
    @Query("SELECT * FROM agent_sessions WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getSessionsForProject(projectId: String): Flow<List<AgentSessionEntity>>

    @Query("SELECT * FROM agent_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): AgentSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AgentSessionEntity)

    @Update
    suspend fun updateSession(session: AgentSessionEntity)

    @Query("DELETE FROM agent_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    // --- Messages ---
    @Query("SELECT * FROM agent_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<AgentMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AgentMessageEntity)

    @Query("DELETE FROM agent_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    // --- Git Snapshots ---
    @Query("SELECT * FROM git_snapshots WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getSnapshotsForProject(projectId: String): Flow<List<GitSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: GitSnapshotEntity)

    @Query("DELETE FROM git_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: String)

    // --- Terminal Logs ---
    @Query("SELECT * FROM terminal_logs WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getTerminalLogs(projectId: String): Flow<List<TerminalLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerminalLog(log: TerminalLogEntity)

    @Query("DELETE FROM terminal_logs WHERE projectId = :projectId")
    suspend fun clearTerminalLogs(projectId: String)

    // --- Plan Tasks ---
    @Query("SELECT * FROM plan_tasks WHERE projectId = :projectId ORDER BY orderIndex ASC, createdAt ASC")
    fun getPlanTasksForProject(projectId: String): Flow<List<PlanTaskEntity>>

    @Query("SELECT * FROM plan_tasks WHERE id = :id LIMIT 1")
    suspend fun getPlanTaskById(id: String): PlanTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanTask(task: PlanTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanTasks(tasks: List<PlanTaskEntity>)

    @Update
    suspend fun updatePlanTask(task: PlanTaskEntity)

    @Query("DELETE FROM plan_tasks WHERE id = :id")
    suspend fun deletePlanTask(id: String)

    @Query("DELETE FROM plan_tasks WHERE projectId = :projectId AND isCompleted = 1")
    suspend fun deleteCompletedTasks(projectId: String)

    @Query("DELETE FROM plan_tasks WHERE projectId = :projectId")
    suspend fun deleteAllTasksForProject(projectId: String)
}
