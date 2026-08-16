package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PlanTaskEntity
import com.example.data.model.PlanTaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OpenCode", appName)
  }

  @Test
  fun `plan task entity default values and completion toggle`() {
    val task = PlanTaskEntity(
      projectId = "proj_123",
      title = "Implement OAuth flow",
      priority = PlanTaskPriority.CRITICAL
    )
    assertEquals(PlanTaskPriority.CRITICAL, task.priority)
    assertFalse(task.isCompleted)

    val completedTask = task.copy(isCompleted = true)
    assertTrue(completedTask.isCompleted)
  }

  @Test
  fun `git snapshot entity tracks additions deletions and diff content`() {
    val snapshot = com.example.data.model.GitSnapshotEntity(
      projectId = "proj_123",
      commitHash = "a1b2c3d",
      message = "Initial workspace commit",
      filesChanged = 3,
      additions = 42,
      deletions = 5,
      diffContent = "+++ a/src/index.ts\n+ console.log('hello');"
    )
    assertEquals("a1b2c3d", snapshot.commitHash)
    assertEquals("Initial workspace commit", snapshot.message)
    assertEquals(3, snapshot.filesChanged)
    assertEquals(42, snapshot.additions)
    assertEquals(5, snapshot.deletions)
  }

  @Test
  fun `plan mode tasks filter by title and priority`() {
    val tasks = listOf(
      PlanTaskEntity(id = "1", projectId = "p1", title = "Setup Room Database", priority = PlanTaskPriority.CRITICAL),
      PlanTaskEntity(id = "2", projectId = "p1", title = "Create UI Views", priority = PlanTaskPriority.MEDIUM),
      PlanTaskEntity(id = "3", projectId = "p1", title = "Add High Priority Tests", priority = PlanTaskPriority.HIGH)
    )

    // Filter by title keyword
    val titleFiltered = tasks.filter { it.title.contains("Database", ignoreCase = true) }
    assertEquals(1, titleFiltered.size)
    assertEquals("Setup Room Database", titleFiltered.first().title)

    // Filter by priority tag
    val priorityFiltered = tasks.filter { it.priority.name.equals("CRITICAL", ignoreCase = true) }
    assertEquals(1, priorityFiltered.size)
    assertEquals(PlanTaskPriority.CRITICAL, priorityFiltered.first().priority)
  }
}
