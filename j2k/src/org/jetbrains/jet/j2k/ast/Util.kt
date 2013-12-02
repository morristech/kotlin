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

package org.jetbrains.jet.j2k.ast

public fun List<Node>.toKotlin(separator: String, prefix: String = "", suffix: String = ""): String {
    val result = StringBuilder()
    if (size() > 0) {
        result.append(prefix)
        var first = true
        for (x in this) {
            if (!first) result.append(separator)
            first = false
            result.append(x.toKotlin())
        }
        result.append(suffix)
    }
    return result.toString()
}

public fun Collection<Modifier>.toKotlin(separator: String = " "): String {
    val result = StringBuilder()
    for (x in this) {
        result.append(x.name)
        result.append(separator)
    }
    return result.toString()
}

public fun String.withSuffix(suffix: String): String = if (isEmpty()) "" else this + suffix
public fun String.withPrefix(prefix: String): String = if (isEmpty()) "" else prefix + this
public fun Expression.withPrefix(prefix: String): String = if (isEmpty()) "" else prefix + toKotlin()

public open class WhiteSpaceSeparatedElementList(val nodes: List<Node>, val minimalWhiteSpace: WhiteSpace) {
    fun toKotlin(): String {
        val result = StringBuilder()
        if (nodes.isNotEmpty()) {
            for ((current, next) in nodes.subsequentPairs()) {
                if (current is WhiteSpace && current < minimalWhiteSpace) {
                    result.append(minimalWhiteSpace.toKotlin())
                } else {
                    result.append(current.toKotlin())
                }
                if (next != null && current !is WhiteSpace && next !is WhiteSpace) {
                    result.append(minimalWhiteSpace.toKotlin())
                }
            }
        }
        return result.toString()
    }
}

public class StatementList(nodes: List<Element>) : WhiteSpaceSeparatedElementList(nodes, WhiteSpace.NewLine) {
    val statements: List<Statement>
        get() = nodes.filter { it is Statement }.map { it as Statement }
}

fun <T : Any> List<T>.subsequentPairs(): List<Pair<T, T?>> {
    var i = 0
    return iterate {
        when (++i) {
            size() + 1 -> null
            size() -> Pair(get(i - 1), null)
            else -> Pair(get(i - 1), get(i))
        }
    }.toList()
}