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

public class MemberComments(elements: List<Element>) : WhiteSpaceSeparatedElementList(elements, WhiteSpace.NoSpace) {
    class object {
        public val Empty: MemberComments = MemberComments(ArrayList())
    }
}

public abstract class Member(val comments: MemberComments, val modifiers: Set<Modifier>) : Element {
    fun accessModifier(): Modifier? {
        return modifiers.find { m -> m == Modifier.PUBLIC || m == Modifier.PROTECTED || m == Modifier.PRIVATE }
    }

    public fun isAbstract(): Boolean = modifiers.contains(Modifier.ABSTRACT)
    public fun isStatic(): Boolean = modifiers.contains(Modifier.STATIC)
    public fun commentsToKotlin(): String = comments.toKotlin()
}

//member itself and all the elements before it in the code (comments, whitespaces)
class MemberHolder(val member: Member, val elements: List<Element>)

public class MemberList(elements: List<Element>) : WhiteSpaceSeparatedElementList(elements, WhiteSpace.NewLine) {
    val members: List<Member>
        get() = elements.filter { it is Member }.map { it as Member }
}

public class ClassMembers(
        val primaryConstructor: Constructor?,
        val secondaryConstructors: MemberList,
        val allMembers: MemberList,
        val staticMembers: MemberList,
        val nonStaticMembers: MemberList
) {
}

fun parseClassMembers(elements: List<Element>): ClassMembers {
    val groups = splitInGroups(elements)
    val constructors = groups.filter { it.member is Constructor }
    val primaryConstuctor = constructors.map { it.member }.find { (it as Constructor).isPrimary }
    val secondaryConstructors = constructors.filter { !(it.member as Constructor).isPrimary }
    val nonConstructors = groups.filter { it.member !is Constructor }
    val staticMembers = nonConstructors.filter { it.member.isStatic() }
    val nonStaticMembers = nonConstructors.filter { !it.member.isStatic() }
    return ClassMembers(primaryConstuctor as Constructor?,
                        secondaryConstructors.toMemberList(),
                        nonConstructors.toMemberList(),
                        staticMembers.toMemberList(),
                        nonStaticMembers.toMemberList())
}

private fun List<MemberHolder>.toMemberList() = MemberList(map { it.elements }.concat())

private fun splitInGroups(elements: List<Element>): List<MemberHolder> {
    val result = ArrayList<Pair<Member, MutableList<Element>>>()
    var current = ArrayList<Element>()
    for (element in elements) {
        current.add(element)
        if (element is Member) {
            result.add(Pair(element, current))
            current = ArrayList<Element>()
        }
    }
    if (result.isNotEmpty()) {
        result.last!!.second.addAll(current)
    }
    return result. map { MemberHolder(it.first, it.second) }
}

private fun <T> List<Collection<T>>.concat(): List<T> = this.fold(ArrayList<T>()) {
    (list, element) ->
    list.addAll(element)
    list
}