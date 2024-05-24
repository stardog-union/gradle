/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.problems.internal.rendering;

import org.gradle.api.problems.internal.DefaultProblemProgressDetails;
import org.gradle.api.problems.internal.Problem;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.buildtree.BuildActionRunner;
import org.gradle.internal.buildtree.BuildTreeLifecycleController;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.invocation.BuildAction;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationListener;
import org.gradle.internal.operations.OperationFinishEvent;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.internal.operations.OperationProgressEvent;
import org.gradle.internal.operations.OperationStartEvent;

import java.util.ArrayList;
import java.util.List;

public class ProblemRenderingBuildActionExecutor implements BuildActionRunner, BuildOperationListener {

    private final BuildActionRunner delegate;
    private final ListenerManager listenerManager;
    private final List<Problem> problems = new ArrayList<>();

    public ProblemRenderingBuildActionExecutor(ListenerManager listenerManager, BuildActionRunner delegate) {
        this.delegate = delegate;
        this.listenerManager = listenerManager;
    }

    @Override
    public BuildActionRunner.Result run(BuildAction action, BuildTreeLifecycleController buildController) {
        BuildActionRunner.Result result;
        try {
            this.listenerManager.addListener(this);
            result = delegate.run(action, buildController);
            System.out.println("ProblemRenderingBuildActionExecutor");
        } catch (Throwable t) {
            throw UncheckedException.throwAsUncheckedException(t);
        } finally {
            problems.clear();
            this.listenerManager.removeListener(this);
        }
        return result;
    }

    @Override
    public void started(BuildOperationDescriptor buildOperation, OperationStartEvent startEvent) {
        // No-op, we are only interested in the progress events
    }

    @Override
    public void progress(OperationIdentifier operationIdentifier, OperationProgressEvent progressEvent) {
        if (progressEvent.getDetails() instanceof DefaultProblemProgressDetails) {
            DefaultProblemProgressDetails details = (DefaultProblemProgressDetails) progressEvent.getDetails();
            problems.add(details.getProblem());
        }
    }

    @Override
    public void finished(BuildOperationDescriptor buildOperation, OperationFinishEvent finishEvent) {
        // No-op, we are only interested in the progress events
    }
}
