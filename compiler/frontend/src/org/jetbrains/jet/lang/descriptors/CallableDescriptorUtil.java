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

package org.jetbrains.jet.lang.descriptors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind;

import static org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind.*;

public class CallableDescriptorUtil {
    public static ExplicitReceiverKind getExpectedReceiverKind(@NotNull CallableDescriptor descriptor) {
        ReceiverParameterDescriptor receiverParameter = descriptor.getReceiverParameter();
        ReceiverParameterDescriptor expectedThisObject = descriptor.getExpectedThisObject();
        if (receiverParameter != null) {
            if (expectedThisObject != null) return BOTH_RECEIVERS;
            return RECEIVER_ARGUMENT;
        }
        if (expectedThisObject != null) return THIS_OBJECT;
        return NO_EXPLICIT_RECEIVER;
    }
}
