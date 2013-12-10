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

public enum ReceiverKind {
    // Expected receiver kind can be found for callable descriptor by CallableDescriptorUtil.getExpectedReceiverKind.
    // It means the kind of the receiver the function (or property) expects.

    // For each resolved call explicit receiver kind is stored.
    // It often isn't the same as expected receiver kind for the corresponding function.

    // For example: function foo in 'class A { fun B.foo() }' has expected receiver kind BOTH_RECEIVERS.
    // It means both this object (of type A) and receiver argument (of type B) should be present in the corresponding resolved call.

    // However for the invocation: 'with(a) { with(b) { foo() }}' explicit receiver kind is NO_EXPLICIT_RECEIVER,
    // 'this object' refers to 'a', 'receiver argument' refers to 'b'.

    // See JetResolvedCallsTestGenerated for more examples.

    RECEIVER_ARGUMENT,
    THIS_OBJECT,
    NO_EXPLICIT_RECEIVER,

    // The callable descriptor expects both receivers if it's an extension member in a class.

    // The resolved call has both receivers explicitly in a very special case for 'invoke'.
    // In a call 'b.foo(1)' where class 'Foo' has an extension member 'fun B.invoke(Int)' function 'invoke' has two explicit receivers:
    // 'b' (as receiver argument) and 'foo' (as this object).
    BOTH_RECEIVERS;

    public boolean isReceiver() {
        return this == RECEIVER_ARGUMENT || this == BOTH_RECEIVERS;
    }

    public boolean isThisObject() {
        return this == THIS_OBJECT || this == BOTH_RECEIVERS;
    }
}
