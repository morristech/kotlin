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

package org.jetbrains.k2js.translate.reference

import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall
import org.jetbrains.jet.lang.descriptors.CallableDescriptor
import com.google.dart.compiler.backend.js.ast.JsExpression
import java.util.Collections
import org.jetbrains.k2js.translate.context.TranslationContext
import org.jetbrains.k2js.translate.general.AbstractTranslator
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue
import com.google.dart.compiler.backend.js.ast.JsNameRef
import org.jetbrains.jet.lang.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind.*
import org.jetbrains.k2js.translate.utils.JsDescriptorUtils.*
import org.jetbrains.k2js.translate.reference.CallType.CallConstructor
import com.google.dart.compiler.backend.js.ast.JsInvocation
import org.jetbrains.k2js.translate.utils.TranslationUtils
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCallImpl
import org.jetbrains.jet.lang.resolve.calls.tasks.ResolutionCandidate
import org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.jet.lang.resolve.TemporaryBindingTrace
import org.jetbrains.jet.lang.resolve.BindingTraceContext
import org.jetbrains.jet.lang.resolve.calls.tasks.TracingStrategy
import org.jetbrains.jet.lang.resolve.calls.model.MutableDataFlowInfoForArguments
import org.jetbrains.jet.lang.descriptors.ReceiverParameterDescriptor


private fun safeGetValue(descriptor : ReceiverParameterDescriptor?) : ReceiverValue {
    return if (descriptor == null)
        ReceiverValue.NO_RECEIVER
    else
        descriptor.getValue()
}


private fun getResolverCall(descriptor : CallableDescriptor, receiverKind: ExplicitReceiverKind) : ResolvedCallImpl<CallableDescriptor> {
    val candidate = ResolutionCandidate.create(descriptor,
                                               safeGetValue(descriptor.getExpectedThisObject()),
                                               safeGetValue(descriptor.getReceiverParameter()),
                                               receiverKind,
                                               false)!!
    val trace = TemporaryBindingTrace.create(BindingTraceContext(), "trace to resolve call (in js)")!!
    return ResolvedCallImpl.create(candidate, trace, TracingStrategy.EMPTY, MutableDataFlowInfoForArguments.WITHOUT_ARGUMENTS_CHECK)
}



class MyCallBuilder(context: TranslationContext,
                    private val resolvedCall: ResolvedCall<out CallableDescriptor>,
                    private val receiver: JsExpression?) : AbstractTranslator(context) {
    var arguments: List<JsExpression> = Collections.emptyList()
    var callType: CallType = CallType.NORMAL

    class object {
        fun build(context: TranslationContext, descriptor: CallableDescriptor, receiver: JsExpression?, receiverKind: ExplicitReceiverKind): MyCallBuilder {
            return MyCallBuilder(context, getResolverCall(descriptor, receiverKind), receiver)
        }
    }

    fun args(vararg args: JsExpression): MyCallBuilder {
        assert(arguments == Collections.EMPTY_LIST)
        arguments = args.toList()
        return this
    }

    fun args(args: List<JsExpression>): MyCallBuilder {
        assert(arguments == Collections.EMPTY_LIST)
        arguments = args
        return this
    }

    private fun inf(): CallInfo {
        return CallInfo(context(), resolvedCall, receiver, arguments, callType)
    }

    fun intrinsic (): JsExpression? {
        val translator = CallTranslator(receiver,
                                        null,
                                        arguments,
                                        resolvedCall,
                                        resolvedCall.getResultingDescriptor().getOriginal(),
                                        callType,
                                        context())
        return translator.intrinsicInvocation()
    }
    fun simple(): JsExpression {
        return CallEvaluator.SIMPLE_CALL.compute(inf())!!
    }

    fun translate(): JsExpression {
        return intrinsic() ?: CallEvaluator.SIMPLE_CALL.compute(inf())!!
    }

    fun translate(consumer: CallEvaluator): JsExpression? {
        return consumer.compute(inf())
    }


}

enum abstract class CallEvaluator {

    abstract fun compute(inf: CallInfo): JsExpression?

    SIMPLE_CALL : CallEvaluator() {
        override fun compute(inf: CallInfo): JsExpression? {
            return JsInvocation(inf.getFunctionRef(), inf.arguments)
        }
    }
}

class CallInfo(val context: TranslationContext,
               val resolvedCall: ResolvedCall<out CallableDescriptor>,
               val receiver: JsExpression?,
               arguments: List<JsExpression>,
               val callType: CallType = CallType.NORMAL) {

    val isVariableAsFunctionCall = resolvedCall is VariableAsFunctionResolvedCall
    val isExtention = resolvedCall.getReceiverArgument() != ReceiverValue.NO_RECEIVER
    val callableDescriptor = resolvedCall.getResultingDescriptor().getOriginal()
    val arguments: List<JsExpression> = if (isExtention) TranslationUtils.generateInvocationArguments(receiver!!, arguments) else arguments

    fun CallType.construct(construct0: (JsExpression?) -> JsExpression): JsExpression {
        val callConstructor: CallConstructor = object : CallConstructor {
            override fun construct(receiver: JsExpression?): JsExpression {
                return construct0(receiver)
            }
        }
        return this.constructCall(receiver, callConstructor, context)
    }

    //  TODO simplify
    fun getFunctionRef(): JsExpression {
        when (resolvedCall.getExplicitReceiverKind()) {
            NO_EXPLICIT_RECEIVER -> {
                assert(receiver == null, "ResolvedCAll say, what call no receiver parameter")
                return context.getQualifiedReference(callableDescriptor)
            }
            THIS_OBJECT -> {
                val shortName = context.getNameForDescriptor(callableDescriptor)
                val thisObject = context.getThisObject(getDeclarationDescriptorForReceiver(resolvedCall.getThisObject()))
                if (isExtention) {
                    return callType.construct { receiver -> JsNameRef(shortName, thisObject) }
                }
                else if (receiver != null) {
                    return callType.construct { receiver -> JsNameRef(shortName, receiver) }
                }
                else {
                    return JsNameRef(shortName, thisObject)
                }
            }
            RECEIVER_ARGUMENT -> {
                assert(receiver != null, "ResolvedCAll say, what call has receiver parameter")
                return context.getQualifiedReference(callableDescriptor)
            }
            // TODO BOTH_RECEIVERS
            else -> {
                throw UnsupportedOperationException("not supported")
            }
        }
    }

    {
        if (isVariableAsFunctionCall)
            throw UnsupportedOperationException("Now variableAsFunctionCall not supported")
    }
}