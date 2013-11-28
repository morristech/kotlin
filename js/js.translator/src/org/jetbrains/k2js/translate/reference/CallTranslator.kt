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
import com.google.dart.compiler.backend.js.ast.JsName
import org.jetbrains.k2js.translate.context.Namer


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

    fun callType(callType: CallType): MyCallBuilder {
        this.callType = callType
        return this
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
        return CallInfo.build(context(), resolvedCall, receiver, arguments, callType)
    }

    fun intrinsic(): JsExpression? {
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

    fun translate(consumer: CallEvaluator): JsExpression {
        return intrinsic() ?: consumer.compute(inf())!!
    }


}

enum abstract class CallEvaluator {

    abstract fun compute(inf: CallInfo): JsExpression?

    SIMPLE_CALL : CallEvaluator() {
        override fun compute(inf: CallInfo): JsExpression? {
            return JsInvocation(inf.getFunctionRef(), inf.arguments)
        }
    }

    PROPERTY_GET : CallEvaluator() {
        override fun compute(inf: CallInfo): JsExpression? {
            if (inf.isExtention) {
                val propertyGetName = Namer.getNameForAccessor(inf.functionName.getIdent()!!, true, false)
                return JsInvocation(inf.getFunctionRef(propertyGetName), inf.arguments)
            }
            return inf.getFunctionRef()
        }
    }
}


class CallInfo(val context: TranslationContext,
               val resolvedCall: ResolvedCall<out CallableDescriptor>,
               val originReceiver: JsExpression?,
               val originArguments: List<JsExpression>,
               val callType: CallType,
               val isVariableAsFunctionCall: Boolean,
               val isExtention: Boolean,
               val callableDescriptor: CallableDescriptor,
               val arguments: List<JsExpression>,
               val qualifier: JsExpression?,
               val functionName: JsName,
               val receiver: JsExpression?,
               val thisObject: JsExpression?) {

    class object {
        fun build(context: TranslationContext,
                  resolvedCall: ResolvedCall<out CallableDescriptor>,
                  originQualifier: JsExpression?,
                  originArguments: List<JsExpression>,
                  callType: CallType = CallType.NORMAL): CallInfo {

            val isVariableAsFunctionCall = resolvedCall is VariableAsFunctionResolvedCall
            val isExtention = resolvedCall.getReceiverArgument() != ReceiverValue.NO_RECEIVER
            val callableDescriptor = resolvedCall.getResultingDescriptor().getOriginal()

            var receiverKind = resolvedCall.getExplicitReceiverKind()
            if (isExtention && receiverKind == THIS_OBJECT) { // TODO: temporary hack
                receiverKind = RECEIVER_ARGUMENT
            }
            if (receiverKind == NO_EXPLICIT_RECEIVER && originQualifier != null) { // TODO: temporary hack
                receiverKind = THIS_OBJECT
            }

            if (isVariableAsFunctionCall)     // TODO
                throw UnsupportedOperationException("Now variableAsFunctionCall not supported")

            val functionName = context.getNameForDescriptor(callableDescriptor)
            val receiver = if (isExtention) {
                if(receiverKind.isReceiver() && originQualifier != null) { // TODO: hack
                    originQualifier
                } else {
                    context.getQualifierForDescriptor(callableDescriptor)
                }
            } else {
                null
            }

            var thisObject = if (resolvedCall.getThisObject() == ReceiverValue.NO_RECEIVER) {
                null
            } else {
                if (receiverKind.isThisObject() && originQualifier != null) {
                    originQualifier
                } else {
                    context.getThisObject(getDeclarationDescriptorForReceiver(resolvedCall.getThisObject()))
                }
            }

            var qualifier = if(thisObject != null) {
                thisObject
            } else {
                context.getQualifierForDescriptor(callableDescriptor) // TODO: check
            }

            val arguments: List<JsExpression> = if (isExtention) {
                TranslationUtils.generateInvocationArguments(receiver!!, originArguments)
            } else {
                originArguments
            }

            return CallInfo(context, resolvedCall, originQualifier, originArguments, callType, isVariableAsFunctionCall, isExtention,
                            callableDescriptor, arguments, qualifier, functionName, receiver, thisObject)
        }
    }

    {

    }

    fun CallType.construct(construct0: (JsExpression?) -> JsExpression): JsExpression {
        val callConstructor: CallConstructor = object : CallConstructor {
            override fun construct(receiver: JsExpression?): JsExpression {
                return construct0(receiver)
            }
        }
        return this.constructCall(originReceiver, callConstructor, context)
    }

    fun wrapUseCallType(functionRef: JsExpression): JsExpression {
        if (originReceiver != null) {
            return callType.construct {functionRef}
        } else {
            return functionRef
        }
    }

    fun getFunctionRef(): JsExpression {
        val functionRef = if (qualifier == null) {
            JsNameRef(functionName)
        } else {
            JsNameRef(functionName, qualifier)
        }
        return wrapUseCallType(functionRef)
    }

    fun getFunctionRef(functionName: String): JsExpression {
        val functionRef = if (qualifier == null) {
            JsNameRef(functionName)
        } else {
            JsNameRef(functionName, qualifier)
        }
        return wrapUseCallType(functionRef)
    }

}