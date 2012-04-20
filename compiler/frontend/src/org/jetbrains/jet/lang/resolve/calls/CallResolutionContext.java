/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.resolve.calls;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.CallableDescriptor;
import org.jetbrains.jet.lang.psi.Call;
import org.jetbrains.jet.lang.resolve.BindingTrace;

/**
* @author svtk
*/
public final class CallResolutionContext<D extends CallableDescriptor> extends ResolutionContext {
    /*package*/ final ResolvedCallImpl<D> candidateCall;
    /*package*/ final TracingStrategy tracing;

    public CallResolutionContext(@NotNull ResolvedCallImpl<D> candidateCall, @NotNull ResolutionTask<D> task, @NotNull BindingTrace trace, @NotNull TracingStrategy tracing, @NotNull Call call) {
        super(trace, task.scope, call, task.expectedType, task.dataFlowInfo);
        this.candidateCall = candidateCall;
        this.tracing = tracing;
    }

    public CallResolutionContext(@NotNull ResolvedCallImpl<D> candidateCall, @NotNull ResolutionTask<D> task, @NotNull BindingTrace trace, @NotNull TracingStrategy tracing) {
        this(candidateCall, task, trace, tracing, task.call);
    }
}