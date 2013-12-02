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

package org.jetbrains.k2js.translate.reference;

import com.google.dart.compiler.backend.js.ast.JsExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.CallableDescriptor;
import org.jetbrains.jet.lang.psi.JetSimpleNameExpression;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall;
import org.jetbrains.k2js.translate.context.TemporaryVariable;
import org.jetbrains.k2js.translate.context.TranslationContext;
import org.jetbrains.k2js.translate.general.AbstractTranslator;

import java.util.Collections;
import java.util.List;

public class MyAccessTranslatorImpl extends AbstractTranslator implements CachedAccessTranslator {

    private final ResolvedCall<? extends CallableDescriptor> resolvedCall;

    protected MyAccessTranslatorImpl(@NotNull TranslationContext context, JetSimpleNameExpression simpleNameExpression) {
        super(context);
        resolvedCall = context.bindingContext().get(BindingContext.RESOLVED_CALL, simpleNameExpression);
    }

    @NotNull
    @Override
    public JsExpression translateAsGet() {
        return new MyCallBuilder(context(), resolvedCall, null).translate(CallEvaluatorImpl.PROPERTY_GET);
    }

    @NotNull
    @Override
    public JsExpression translateAsSet(@NotNull JsExpression setTo) {
        return new MyCallBuilder(context(), resolvedCall, null).args(setTo)
                .translate(CallEvaluatorImpl.PROPERTY_SET);
    }

    @NotNull
    @Override
    public CachedAccessTranslator getCached() {
        return this;
    }

    @NotNull
    @Override
    public List<TemporaryVariable> declaredTemporaries() {
        return Collections.emptyList();
    }
}
