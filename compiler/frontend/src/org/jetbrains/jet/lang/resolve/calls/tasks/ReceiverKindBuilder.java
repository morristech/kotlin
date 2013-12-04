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

package org.jetbrains.jet.lang.resolve.calls.tasks;

import static org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind.*;

public class ReceiverKindBuilder {
    private static final ReceiverKindBuilder INVOKE_EXPLICIT = new ReceiverKindBuilder(true, true);
    private static final ReceiverKindBuilder INVOKE_IMPLICIT = new ReceiverKindBuilder(true, false);
    private static final ReceiverKindBuilder NOT_INVOKE_EXPLICIT = new ReceiverKindBuilder(false, true);
    private static final ReceiverKindBuilder NOT_INVOKE_IMPLICIT = new ReceiverKindBuilder(false, false);

    public static ReceiverKindBuilder buildKind(boolean isInvoke, boolean isExplicit) {
        if (isInvoke) {
            if (isExplicit) return INVOKE_EXPLICIT;
            return INVOKE_IMPLICIT;
        }
        if (isExplicit) return NOT_INVOKE_EXPLICIT;
        return NOT_INVOKE_IMPLICIT;

    }

    private final boolean isInvoke;
    private final boolean isExplicit;

    private ReceiverKindBuilder(boolean isInvoke, boolean isExplicit) {
        this.isInvoke = isInvoke;
        this.isExplicit = isExplicit;
    }

    private ExplicitReceiverKind getKind(ExplicitReceiverKind kind) {
        if (!isInvoke && !isExplicit) return NO_EXPLICIT_RECEIVER;
        if (isInvoke && isExplicit) return BOTH_RECEIVERS;
        return kind;
    }

    public ExplicitReceiverKind asThisObject() {
        return getKind(THIS_OBJECT);
    }

    public ExplicitReceiverKind asReceiverArgument() {
        return getKind(RECEIVER_ARGUMENT);
    }

}
