/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.api.tasks.compile


import org.gradle.api.tasks.compile.fixtures.ProblematicClassGenerator
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
/**
 * Sister class of {@link JavaCompileProblemsIntegrationTest} that tests the problems API with multiple compilation tasks.
 */
class JavaMultiCompileProblemsIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        enableProblemsApiCheck()
    }

    def "problems are reported from multiple tasks when continue flag is used"() {
        given:
        settingsFile """
            rootProject.name = 'multi-compile-problems'
            include 'project1', 'project2'
        """

        def project1Dir = file("project1").createDir()
        project1Dir.file("build.gradle") << """
            apply plugin: 'java'
        """
        def project1ProblematicTestGenerator = new ProblematicClassGenerator(project1Dir)
        project1ProblematicTestGenerator.addError()
        project1ProblematicTestGenerator.save()

        def project2Dir = file("project2").createDir()
        project2Dir.file("build.gradle") << """
            apply plugin: 'java'
        """
        def project2ProblematicTestGeneratorFoo = new ProblematicClassGenerator(project2Dir, "Foo")
        project2ProblematicTestGeneratorFoo.addError()
        project2ProblematicTestGeneratorFoo.save()
        def project2ProblematicTestGeneratorBar = new ProblematicClassGenerator(project2Dir, "Bar")
        project2ProblematicTestGeneratorBar.addWarning()
        project2ProblematicTestGeneratorBar.save()

        when:
        fails("compileJava", "--continue")

        then:
        // Total 2 problems, 1 per project
        collectedProblems.size() == 2
    }

}
