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

package org.jetbrains.jet.codegen.asm;

import org.jetbrains.jet.codegen.state.GenerationState;

import java.util.List;
import java.util.Map;

public class InliningInfo {

    public final Map<Integer, ClosureInfo> expresssionMap;

    public final List<InlinableAccess> inlinableAccesses;

    public final List<ConstructorInvocation> constructorInvocation;

    public final VarRemapper remapper;

    public final GenerationState state;

    public InliningInfo(
            Map<Integer, ClosureInfo> map,
            List<InlinableAccess> accesses,
            List<ConstructorInvocation> invocation,
            VarRemapper remapper,
            GenerationState state
    ) {
        expresssionMap = map;
        inlinableAccesses = accesses;
        constructorInvocation = invocation;
        this.remapper = remapper;
        this.state = state;
    }
}
