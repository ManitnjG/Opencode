package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppDao
import com.example.data.model.AgentMessageEntity
import com.example.data.model.AgentSessionEntity
import com.example.data.model.GitSnapshotEntity
import com.example.data.model.PlanTaskEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TerminalLogEntity
import com.example.data.model.WorkspaceFileEntity

@Database(
    entities = [
        ProjectEntity::class,
        WorkspaceFileEntity::class,
        AgentSessionEntity::class,
        AgentMessageEntity::class,
        GitSnapshotEntity::class,
        TerminalLogEntity::class,
        PlanTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "opencode_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
