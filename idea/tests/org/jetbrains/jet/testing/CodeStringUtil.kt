/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.testing

import java.io.StringReader
import java.io.BufferedReader

fun String.trimIndent(): String {
    return BufferedReader(StringReader(this)).useLines { (linesIterator : Iterator<String>): String ->
        val lines = linesIterator.toList()

        val firstNonEmpty = lines.iterator().find { !it.trim().isEmpty() }
        if (firstNonEmpty == null) {
            return@useLines this
        }

        val trimmedPrefix = firstNonEmpty.takeWhile { ch -> ch.isWhitespace() }
        if (trimmedPrefix.isEmpty()) {
            return@useLines this
        }

        lines.map { line ->
            when {
                line.isEmpty() -> line
                line.trim().isEmpty() -> ""
                else -> {
                    if (!line.startsWith(trimmedPrefix)) {
                        throw IllegalArgumentException("""Invalid line "$line", ${trimmedPrefix.size} whitespace character are expected""")
                    }

                    line.substring(trimmedPrefix.length)
                }
            }
        }.makeString(separator = "\n")
    }
}
