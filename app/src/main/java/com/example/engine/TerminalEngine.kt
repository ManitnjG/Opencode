package com.example.engine

import com.example.data.model.GitSnapshotEntity
import com.example.data.model.TerminalLogEntity
import com.example.data.model.WorkspaceFileEntity
import kotlinx.coroutines.delay
import java.util.UUID

data class ShellOutput(
    val command: String,
    val output: String,
    val exitCode: Int = 0,
    val executionTimeMs: Long = 140
)

class TerminalEngine {

    suspend fun executeCommand(
        command: String,
        projectId: String,
        files: List<WorkspaceFileEntity>,
        snapshots: List<GitSnapshotEntity>
    ): ShellOutput {
        val trimmed = command.trim()
        val startTime = System.currentTimeMillis()
        delay(180) // realistic terminal processing delay

        val parts = trimmed.split(" ").filter { it.isNotBlank() }
        val rootCommand = parts.firstOrNull()?.lowercase() ?: ""

        val output = when (rootCommand) {
            "help" -> {
                """
                    OpenCode Autonomous Terminal v1.0.0
                    Available built-in commands:
                      • npm [run build | test | run dev | lint]
                      • pytest [-v | -k <filter>]
                      • git [status | diff | log | branch]
                      • cargo [check | run | test]
                      • ls [-la | -R]
                      • cat <filename>
                      • tree
                      • clear
                      • lsp [diagnostics | check]
                      • opencode [--plan | --build | --status]
                """.trimIndent()
            }

            "ls" -> {
                val isLong = parts.contains("-la") || parts.contains("-l")
                if (isLong) {
                    val header = "total ${files.size * 4}\ndrwxr-xr-x  6 opencode staff  192 Aug 16 00:00 .\ndrwxr-xr-x 12 opencode staff  384 Aug 16 00:00 .."
                    val rows = files.joinToString("\n") { file ->
                        "-rw-r--r--  1 opencode staff  ${file.content.length.toString().padStart(4, ' ')} Aug 16 00:00 ${file.filePath}"
                    }
                    "$header\n$rows"
                } else {
                    files.joinToString("  ") { it.filePath }
                }
            }

            "tree" -> {
                val sb = StringBuilder(".\n")
                files.forEachIndexed { index, file ->
                    val isLast = index == files.size - 1
                    sb.append(if (isLast) "└── " else "├── ")
                    sb.append(file.filePath).append("\n")
                }
                sb.append("\n${files.size} files, 3 directories").toString()
            }

            "cat" -> {
                val fileName = parts.getOrNull(1)
                if (fileName == null) {
                    "cat: missing file operand"
                } else {
                    val file = files.find { it.filePath.equals(fileName, ignoreCase = true) || it.filePath.endsWith(fileName, ignoreCase = true) }
                    if (file != null) {
                        file.content.lines().mapIndexed { i, line ->
                            "${(i + 1).toString().padStart(3, ' ')} | $line"
                        }.joinToString("\n")
                    } else {
                        "cat: $fileName: No such file or directory"
                    }
                }
            }

            "npm" -> {
                val sub = parts.getOrNull(1) ?: ""
                when (sub) {
                    "test" -> {
                        """
                            > opencode-workspace@1.0.0 test
                            > jest --passWithNoTests

                             PASS  src/app/page.test.tsx (1.24s)
                             PASS  src/app/api/agent/route.test.ts (0.86s)

                            Test Suites: 2 passed, 2 total
                            Tests:       6 passed, 6 total
                            Snapshots:   0 total
                            Time:        2.315 s
                            Ran all test suites.
                        """.trimIndent()
                    }
                    "run" -> {
                        val action = parts.getOrNull(2) ?: "build"
                        if (action == "build") {
                            """
                                > opencode-workspace@1.0.0 build
                                > next build

                                ▲ Next.js 15.1.0
                                - Environments: .env.local

                                ✓ Compiled successfully in 1420ms
                                ✓ Linting and checking validity of types
                                ✓ Collecting page data
                                ✓ Generating static pages (5/5)
                                ✓ Finalizing page optimization

                                Route (app)                              Size     First Load JS
                                ┌ ○ /                                    142 B          87.4 kB
                                ├ ƒ /api/agent                           0 B            87.2 kB
                                └ ○ /_not-found                          871 B          88.1 kB
                                + First Load JS shared by all            87.2 kB
                            """.trimIndent()
                        } else {
                            """
                                > opencode-workspace@1.0.0 $action
                                Ready in 450ms. Local dev server listening at http://localhost:3000
                            """.trimIndent()
                        }
                    }
                    "lint" -> {
                        "✔ No ESLint warnings or errors found across ${files.size} source files."
                    }
                    else -> "npm command '$sub' completed successfully."
                }
            }

            "pytest" -> {
                """
                    ============================= test session starts ==============================
                    platform linux -- Python 3.12.3, pytest-8.3.0, pluggy-1.5.0
                    rootdir: /workspace
                    collected 4 items

                    tests/test_main.py::test_root PASSED                                     [ 25%]
                    tests/test_main.py::test_execute PASSED                                  [ 50%]
                    tests/test_main.py::test_agent_planner PASSED                            [ 75%]
                    tests/test_main.py::test_lsp_checks PASSED                               [100%]

                    ============================== 4 passed in 0.42s ===============================
                """.trimIndent()
            }

            "git" -> {
                val sub = parts.getOrNull(1) ?: "status"
                when (sub) {
                    "status" -> {
                        """
                            On branch main
                            Your branch is up to date with 'origin/main'.

                            Changes ready for snapshot:
                              (use "opencode snapshot" to create commit)
                            ${files.take(2).joinToString("\n") { "	modified:   ${it.filePath}" }}

                            nothing added to commit but working tree clean
                        """.trimIndent()
                    }
                    "diff" -> {
                        """
                            diff --git a/src/app/page.tsx b/src/app/page.tsx
                            index e69de29..d95f3ad 100644
                            --- a/src/app/page.tsx
                            +++ b/src/app/page.tsx
                            @@ -14,6 +14,8 @@
                            + // Autonomous update by OpenCode
                            + export const runtime = 'edge';
                        """.trimIndent()
                    }
                    "log" -> {
                        if (snapshots.isEmpty()) {
                            "commit a7b1c4e (HEAD -> main)\nAuthor: OpenCode Agent <agent@opencode.ai>\nDate:   Today\n\n    Initial workspace setup"
                        } else {
                            snapshots.take(5).joinToString("\n\n") { snap ->
                                "commit ${snap.commitHash} (HEAD -> main)\nAuthor: OpenCode Agent <agent@opencode.ai>\nDate:   ${java.util.Date(snap.timestamp)}\n\n    ${snap.message}"
                            }
                        }
                    }
                    else -> "git: '$sub' is not a supported git command in sandbox."
                }
            }

            "cargo" -> {
                """
                    Compiling opencode_core v0.1.0 (/workspace)
                        Finished `dev` profile [unoptimized + debuginfo] target(s) in 0.84s
                    Running `target/debug/opencode_core`
                    [INFO] OpenCode Core Engine initialized. 0 diagnostics.
                """.trimIndent()
            }

            "clear" -> ""

            else -> {
                "[opencode:~$] Processed: $trimmed\nStatus: 0 (Execution completed successfully)"
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return ShellOutput(
            command = command,
            output = output,
            exitCode = 0,
            executionTimeMs = duration
        )
    }
}
