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

import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.internal.GeneralData;
import org.gradle.api.problems.internal.Problem;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProblemRenderer {

    private final PrintWriter output;
    private int problemCount = 0;

    public ProblemRenderer(Writer writer) {
        output = new PrintWriter(writer);
    }

    public void render(List<Problem> problems) {
        Map<ProblemId, List<Problem>> renderingGroups = new HashMap<>();
        for (Problem problem : problems) {
            List<Problem> groupedProblems = renderingGroups.computeIfAbsent(
                problem.getDefinition().getId(),
                id -> new ArrayList<>()
            );
            groupedProblems.add(problem);
        }

        renderingGroups.forEach((id, groupedProblems) -> renderSingleProblemGroup(id, groupedProblems));
        problemCount++;
    }

    public void render(Problem problem) {
        this.render(Collections.singletonList(problem));
    }

    private void renderSingleProblemGroup(ProblemId id, List<Problem> groupedProblems) {
        output.printf(
            "%s (%s)%n", id.getDisplayName(), id
        );
        groupedProblems.forEach(this::renderSingleProblem);
    }

    private void renderSingleProblem(Problem problem) {
        Map<String, String> additionalData = Optional.ofNullable(problem.getAdditionalData())
            .map(GeneralData.class::cast)
            .map(GeneralData::getAsMap)
            .orElse(Collections.emptyMap());

        if (additionalData.containsKey("formatted")) {
            printMultiline(additionalData.get("formatted"), 1);
        } else {
            if (problem.getContextualLabel() != null) {
                printMultiline(problem.getContextualLabel(), 1);
            } else {
                printMultiline(problem.getDefinition().getId().getDisplayName(), 1);
            }
            if (problem.getDetails() != null) {
                printMultiline(problem.getDetails(), 2);
            }
        }
    }

    private void printMultiline(String message, int level) {
        for (String line : message.split("\n")) {
            for (int i = 0; i < level; i++) {
                output.print("  ");
            }
            output.printf("%s%n", line);
        }
    }

    public int getProblemCount() {
        return problemCount;
    }
}
