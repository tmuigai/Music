package com.example.playback

import com.example.model.LyricsLine
import com.example.model.WordTimestamp

object LrcParser {
    private val LINE_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
    private val WORD_REGEX = Regex("""<(\d{2}):(\d{2})\.(\d{2,3})>([^<]*)""")

    fun parse(lrcContent: String?): List<LyricsLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = mutableListOf<LyricsLine>()

        lrcContent.lineSequence().forEach { line ->
            val match = LINE_REGEX.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val millisStr = match.groupValues[3]
                val ms = if (millisStr.length == 2) millisStr.toLong() * 10 else millisStr.toLong()
                val timestampMs = min * 60000L + sec * 1000L + ms
                val rawText = match.groupValues[4].trim()

                // Parse word timestamps if present
                val words = mutableListOf<WordTimestamp>()
                val wordMatches = WORD_REGEX.findAll(rawText).toList()

                if (wordMatches.isNotEmpty()) {
                    for (i in wordMatches.indices) {
                        val wm = wordMatches[i]
                        val wMin = wm.groupValues[1].toLongOrNull() ?: 0L
                        val wSec = wm.groupValues[2].toLongOrNull() ?: 0L
                        val wMillisStr = wm.groupValues[3]
                        val wMs = if (wMillisStr.length == 2) wMillisStr.toLong() * 10 else wMillisStr.toLong()
                        val wStart = wMin * 60000L + wSec * 1000L + wMs
                        val wordText = wm.groupValues[4].trim()

                        val wEnd = if (i < wordMatches.size - 1) {
                            val next = wordMatches[i + 1]
                            val nMin = next.groupValues[1].toLongOrNull() ?: 0L
                            val nSec = next.groupValues[2].toLongOrNull() ?: 0L
                            val nMsStr = next.groupValues[3]
                            val nMs = if (nMsStr.length == 2) nMsStr.toLong() * 10 else nMsStr.toLong()
                            nMin * 60000L + nSec * 1000L + nMs
                        } else {
                            wStart + 800L
                        }

                        words.add(WordTimestamp(word = wordText, startMs = wStart, endMs = wEnd))
                    }
                }

                val cleanText = if (wordMatches.isNotEmpty()) {
                    rawText.replace(Regex("""<[^>]+>"""), "")
                } else {
                    rawText
                }

                lines.add(
                    LyricsLine(
                        timestampMs = timestampMs,
                        text = cleanText,
                        wordTimestamps = if (words.isNotEmpty()) words else null,
                        isSynchronized = true
                    )
                )
            } else if (line.isNotBlank() && !line.startsWith("[")) {
                // Non-timestamped plain lyric line
                lines.add(LyricsLine(timestampMs = -1L, text = line.trim(), isSynchronized = false))
            }
        }

        return lines.sortedBy { it.timestampMs }
    }

    fun exportToLrc(lines: List<LyricsLine>): String {
        val sb = StringBuilder()
        lines.forEach { line ->
            if (line.timestampMs >= 0) {
                val min = line.timestampMs / 60000L
                val sec = (line.timestampMs % 60000L) / 1000L
                val ms = (line.timestampMs % 1000L) / 10L
                val timeStr = String.format("[%02d:%02d.%02d]", min, sec, ms)
                sb.append(timeStr).append(line.text).append("\n")
            } else {
                sb.append(line.text).append("\n")
            }
        }
        return sb.toString()
    }
}
