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

package org.jetbrains.jet.resolve.calls;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.ConfigurationKind;
import org.jetbrains.jet.JetLiteFixture;
import org.jetbrains.jet.JetTestUtils;
import org.jetbrains.jet.analyzer.AnalyzeExhaust;
import org.jetbrains.jet.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.jet.lang.descriptors.CallableDescriptor;
import org.jetbrains.jet.lang.diagnostics.DiagnosticUtils;
import org.jetbrains.jet.lang.psi.JetElement;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall;
import org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind;
import org.jetbrains.jet.lang.resolve.java.AnalyzerFacadeForJVM;
import org.jetbrains.jet.lang.resolve.scopes.receivers.AbstractReceiverValue;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractResolvedCallsTest extends JetLiteFixture {
    private static final String EXPLICIT_RECEIVER_KIND_DIRECTIVE = "EXPLICIT_RECEIVER_KIND";


    @Override
    protected JetCoreEnvironment createEnvironment() {
        return createEnvironmentWithMockJdk(ConfigurationKind.JDK_ONLY);
    }

    public void doTest(String filePath) throws Exception {
        File file = new File(filePath);
        String text = JetTestUtils.doLoadFile(file);

        Map<String, String> directives = JetTestUtils.parseDirectives(text);
        String callName = directives.get("CALL");
        String thisObject = directives.get("THIS_OBJECT");
        String receiverArgument = directives.get("RECEIVER_ARGUMENT");

        ExplicitReceiverKind explicitReceiverKind = getExplicitReceiverKind(directives);

        JetFile psiFile = JetTestUtils.loadJetFile(getProject(), file);
        AnalyzeExhaust analyzeExhaust = AnalyzerFacadeForJVM
                .analyzeOneFileWithJavaIntegration(psiFile, Collections.<AnalyzerScriptParameter>emptyList());

        BindingContext bindingContext = analyzeExhaust.getBindingContext();

        ImmutableMap<JetElement, ResolvedCall<? extends CallableDescriptor>> resolvedCallsEntries =
                bindingContext.getSliceContents(BindingContext.RESOLVED_CALL);
        boolean callFound = false;
        for (Map.Entry<JetElement, ResolvedCall<? extends CallableDescriptor>> entry : resolvedCallsEntries.entrySet()) {
            JetElement element = entry.getKey();
            if (callName.equals(element.getText())) {
                callFound = true;

                ResolvedCall<? extends CallableDescriptor> resolvedCall = entry.getValue();
                checkResolvedCall(resolvedCall, element, thisObject, receiverArgument, explicitReceiverKind);
            }
        }
        assertTrue("Resolved call for " + callName + " was not found.", callFound);
    }

    private static void checkResolvedCall(
            @NotNull ResolvedCall<? extends CallableDescriptor> resolvedCall,
            @NotNull JetElement element,
            @NotNull String thisObject,
            @NotNull String receiverArgument,
            @NotNull ExplicitReceiverKind explicitReceiverKind
    ) {
        DiagnosticUtils.LineAndColumn lineAndColumn =
                DiagnosticUtils.getLineAndColumnInPsiFile(element.getContainingFile(), element.getTextRange());

        assertEquals("This object mismatch: ", thisObject, getReceiverText(resolvedCall.getThisObject()));
        assertEquals("Receiver argument mismatch: ", receiverArgument, getReceiverText(resolvedCall.getReceiverArgument()));

        assertEquals(
                "Explicit receiver kind for resolved call for '" + element.getText() + "'" + lineAndColumn + " in not as expected",
                explicitReceiverKind, resolvedCall.getExplicitReceiverKind());
    }

    private static String getReceiverText(ReceiverValue receiverValue) {
        if (receiverValue instanceof ExpressionReceiver) {
            return ((ExpressionReceiver) receiverValue).getExpression().getText();
        }
        if (receiverValue instanceof AbstractReceiverValue) {
            return receiverValue.getType().toString();
        }
        return receiverValue.toString();
    }

    private static ExplicitReceiverKind getExplicitReceiverKind(Map<String, String> directives) {
        String explicitReceiverKind = directives.get(EXPLICIT_RECEIVER_KIND_DIRECTIVE);
        assert explicitReceiverKind != null : EXPLICIT_RECEIVER_KIND_DIRECTIVE + " should be present.";
        try {
            return ExplicitReceiverKind.valueOf(explicitReceiverKind);
        }
        catch (Exception e) {
            StringBuilder message = new StringBuilder();
            message.append(EXPLICIT_RECEIVER_KIND_DIRECTIVE).append(" must be one of the following: ");
            for (ExplicitReceiverKind kind : ExplicitReceiverKind.values()) {
                message.append(kind).append(", ");
            }
            message.append("\nnot ").append(explicitReceiverKind).append(".");
            throw new AssertionError(message);
        }
    }
}
