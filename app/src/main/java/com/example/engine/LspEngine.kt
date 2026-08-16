package com.example.engine

import com.example.data.model.DiagnosticItem
import com.example.data.model.DiagnosticSeverity
import com.example.data.model.WorkspaceFileEntity
import java.util.UUID

class LspEngine {

    fun analyzeDiagnostics(files: List<WorkspaceFileEntity>): List<DiagnosticItem> {
        val diagnostics = mutableListOf<DiagnosticItem>()

        for (file in files) {
            val lines = file.content.lines()

            // Check for unclosed brackets or syntax quirks
            var openBraces = 0
            var openParens = 0

            lines.forEachIndexed { idx, line ->
                openBraces += line.count { it == '{' } - line.count { it == '}' }
                openParens += line.count { it == '(' } - line.count { it == ')' }

                // Check for console.log or print statements (code smell warning)
                if (line.contains("console.log(") && file.language in listOf("typescript", "javascript")) {
                    diagnostics.add(
                        DiagnosticItem(
                            id = UUID.randomUUID().toString(),
                            filePath = file.filePath,
                            lineNumber = idx + 1,
                            severity = DiagnosticSeverity.INFO,
                            message = "Avoid raw console.log in production build; prefer structured logger.",
                            source = "ESLint (no-console)"
                        )
                    )
                }

                // Check for TODOs
                if (line.contains("TODO", ignoreCase = true)) {
                    diagnostics.add(
                        DiagnosticItem(
                            id = UUID.randomUUID().toString(),
                            filePath = file.filePath,
                            lineNumber = idx + 1,
                            severity = DiagnosticSeverity.WARNING,
                            message = "Pending task item: ${line.trim()}",
                            source = "LSP / TaskScanner"
                        )
                    )
                }
            }

            if (openBraces != 0) {
                diagnostics.add(
                    DiagnosticItem(
                        id = UUID.randomUUID().toString(),
                        filePath = file.filePath,
                        lineNumber = lines.size,
                        severity = DiagnosticSeverity.ERROR,
                        message = "Unbalanced curly braces ({}) detected in file.",
                        source = "TypeScript Compiler (TS1005)"
                    )
                )
            }
        }

        return diagnostics
    }
}
