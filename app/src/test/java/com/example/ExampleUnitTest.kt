package com.example

import com.example.data.model.WorkspaceFileEntity
import com.example.engine.TerminalEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun terminalEngine_executesHelpAndTree() = runBlocking {
        val engine = TerminalEngine()
        val dummyFiles = listOf(
            WorkspaceFileEntity(projectId = "p1", filePath = "src/index.ts", content = "console.log('hi')", language = "typescript")
        )
        val helpOutput = engine.executeCommand("help", "p1", dummyFiles, emptyList())
        assertTrue(helpOutput.output.contains("OpenCode Autonomous Terminal"))
        assertEquals(0, helpOutput.exitCode)

        val treeOutput = engine.executeCommand("tree", "p1", dummyFiles, emptyList())
        assertTrue(treeOutput.output.contains("src/index.ts"))
    }
}
