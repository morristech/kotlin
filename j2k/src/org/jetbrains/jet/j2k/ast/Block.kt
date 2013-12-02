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

import java.util.ArrayList

public fun Block(statements: List<Statement>, notEmpty: Boolean = false): Block {
    val elements = ArrayList<Element>()
    elements.add(WhiteSpace("\n"))
    elements.addAll(statements)
    elements.add(WhiteSpace("\n"))
    return Block(StatementList(elements), notEmpty)
}

public class Block(val statementList: StatementList, val notEmpty: Boolean = false) : Statement() {

    public val statements: List<Statement> = statementList.statements

    public override fun isEmpty(): Boolean {
        return !notEmpty && statementList.statements.all { it.isEmpty() }
    }

    public override fun toKotlin(): String {
        if (!isEmpty()) {
            return "{${statementList.toKotlin()}}"
        }

        return ""
    }

    class object {
        public val EMPTY_BLOCK: Block = Block(StatementList(ArrayList()))
    }
}
