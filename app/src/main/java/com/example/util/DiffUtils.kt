package com.example.util

import com.example.data.model.DiffItem
import com.example.data.model.DiffLine
import com.example.data.model.DiffLineType

object DiffUtils {

    fun calculateDiff(filePath: String, oldContent: String, newContent: String): DiffItem {
        val oldLines = if (oldContent.isEmpty()) emptyList() else oldContent.lines()
        val newLines = if (newContent.isEmpty()) emptyList() else newContent.lines()

        val diffLines = mutableListOf<DiffLine>()
        var additions = 0
        var deletions = 0

        diffLines.add(DiffLine(DiffLineType.HEADER, "--- a/$filePath", null, null))
        diffLines.add(DiffLine(DiffLineType.HEADER, "+++ b/$filePath", null, null))

        // Simple LCS / Myers style diff algorithm approximation for unified display
        var i = 0
        var j = 0
        var oldLineNum = 1
        var newLineNum = 1

        while (i < oldLines.size || j < newLines.size) {
            if (i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j]) {
                diffLines.add(
                    DiffLine(
                        type = DiffLineType.SAME,
                        text = " " + oldLines[i],
                        oldLineNumber = oldLineNum++,
                        newLineNumber = newLineNum++
                    )
                )
                i++
                j++
            } else {
                // Check if old line exists further down in newLines
                val indexInNew = newLines.subList(j, newLines.size).indexOf(if (i < oldLines.size) oldLines[i] else "")
                val indexInOld = oldLines.subList(i, oldLines.size).indexOf(if (j < newLines.size) newLines[j] else "")

                if (i < oldLines.size && (indexInNew == -1 || (indexInOld != -1 && indexInOld <= indexInNew))) {
                    diffLines.add(
                        DiffLine(
                            type = DiffLineType.DELETE,
                            text = "-" + oldLines[i],
                            oldLineNumber = oldLineNum++,
                            newLineNumber = null
                        )
                    )
                    deletions++
                    i++
                } else if (j < newLines.size) {
                    diffLines.add(
                        DiffLine(
                            type = DiffLineType.ADD,
                            text = "+" + newLines[j],
                            oldLineNumber = null,
                            newLineNumber = newLineNum++
                        )
                    )
                    additions++
                    j++
                } else {
                    i++
                    j++
                }
            }
        }

        return DiffItem(
            filePath = filePath,
            oldContent = oldContent,
            newContent = newContent,
            additions = additions,
            deletions = deletions,
            diffLines = diffLines
        )
    }

    fun formatUnifiedDiff(diffItem: DiffItem): String {
        return diffItem.diffLines.joinToString("\n") { it.text }
    }
}
