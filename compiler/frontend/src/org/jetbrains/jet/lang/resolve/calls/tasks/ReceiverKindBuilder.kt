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

package org.jetbrains.jet.lang.resolve.calls.tasks.receiverKind;

import org.jetbrains.jet.lang.resolve.calls.tasks.ReceiverKind
import org.jetbrains.jet.lang.resolve.calls.tasks.ReceiverKind.*;
import org.jetbrains.jet.lang.resolve.calls.tasks.receiverKind.ReceiverKindBuilderImpl.*;

public trait ReceiverKindBuilder {
    public fun asThisObject(): ReceiverKind
    public fun asReceiverArgument(): ReceiverKind
}

private enum class ReceiverKindBuilderImpl private(val isInvoke: Boolean, val isExplicit: Boolean) : ReceiverKindBuilder {
    INVOKE_EXPLICIT : ReceiverKindBuilderImpl(true, true)
    INVOKE_IMPLICIT : ReceiverKindBuilderImpl(true, false)
    NOT_INVOKE_EXPLICIT : ReceiverKindBuilderImpl(false, true)
    NOT_INVOKE_IMPLICIT : ReceiverKindBuilderImpl(false, false)

    private fun getKind(kind: ReceiverKind): ReceiverKind {
        if (!isInvoke && !isExplicit) return NO_EXPLICIT_RECEIVER
        if (isInvoke && isExplicit) return BOTH_RECEIVERS
        return kind
    }
    override fun asThisObject() = getKind(THIS_OBJECT)
    override fun asReceiverArgument() = getKind(RECEIVER_ARGUMENT)
}

public fun buildKind(isInvoke: Boolean, isExplicit: Boolean): ReceiverKindBuilder =
        when (Pair(isInvoke, isExplicit)) {
            Pair(true, true) -> INVOKE_EXPLICIT
            Pair(true, false) -> INVOKE_IMPLICIT
            Pair(false, true) -> NOT_INVOKE_EXPLICIT
            else -> NOT_INVOKE_IMPLICIT
        }
