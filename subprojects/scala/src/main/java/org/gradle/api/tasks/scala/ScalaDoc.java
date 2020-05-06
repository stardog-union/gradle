/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.tasks.scala;

import org.gradle.api.Incubating;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.project.IsolatedAntBuilder;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.scala.internal.GenerateScaladoc;
import org.gradle.util.GUtil;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.List;

/**
 * Generates HTML API documentation for Scala source files.
 *
 */
@CacheableTask
public class ScalaDoc extends SourceTask {

    private File destinationDir;

    private FileCollection classpath;
    private FileCollection scalaClasspath;
    private ScalaDocOptions scalaDocOptions = new ScalaDocOptions();
    private String title;
    private final Property<String> maxMemory;

    public ScalaDoc() {
        this.maxMemory = getObjectFactory().property(String.class);
    }

    @Inject
    protected IsolatedAntBuilder getAntBuilder() {
        throw new UnsupportedOperationException();
    }

    @Inject
    public WorkerExecutor getWorkerExecutor() {
        throw new UnsupportedOperationException();
    }

    @Inject
    public ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the directory to generate the API documentation into.
     */
    @OutputDirectory
    public File getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    /**
     * {@inheritDoc}
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    @Override
    public FileTree getSource() {
        return super.getSource();
    }

    /**
     * <p>Returns the classpath to use to locate classes referenced by the documented source.</p>
     *
     * @return The classpath.
     */
    @Classpath
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    /**
     * Returns the classpath to use to load the ScalaDoc tool.
     */
    @Classpath
    public FileCollection getScalaClasspath() {
        return scalaClasspath;
    }

    public void setScalaClasspath(FileCollection scalaClasspath) {
        this.scalaClasspath = scalaClasspath;
    }

    /**
     * Returns the ScalaDoc generation options.
     */
    @Nested
    public ScalaDocOptions getScalaDocOptions() {
        return scalaDocOptions;
    }

    public void setScalaDocOptions(ScalaDocOptions scalaDocOptions) {
        this.scalaDocOptions = scalaDocOptions;
    }

    /**
     * Returns the documentation title.
     */
    @Nullable @Optional @Input
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    /**
     * Returns the amount of memory allocated to this task.
     * Ex. 512m, 1G
     * @since 6.5
     */
    @Incubating
    @Internal
    public Property<String> getMaxMemory() {
        return maxMemory;
    }

    @TaskAction
    protected void generate() {
        ScalaDocOptions options = getScalaDocOptions();
        if (!GUtil.isTrue(options.getDocTitle())) {
            options.setDocTitle(getTitle());
        }

        WorkQueue queue = getWorkerExecutor().processIsolation(worker -> {
            worker.getClasspath().from(getScalaClasspath());
            if(getMaxMemory().isPresent()) {
                worker.forkOptions(forkOptions -> forkOptions.setMaxHeapSize(getMaxMemory().get()));
            }
        });
        queue.submit(GenerateScaladoc.class, parameters -> {
            parameters.getClasspath().from(getClasspath());
            parameters.getOutputDirectory().set(getDestinationDir());
            parameters.getSources().from(getSource());

            if (options.isDeprecation()) {
                parameters.getOptions().add("-deprecation");
            }

            if (options.isUnchecked()) {
                parameters.getOptions().add("-unchecked");
            }

            String footer = options.getFooter();
            if (footer!=null) {
                parameters.getOptions().add("-doc-footer");
                parameters.getOptions().add(footer);
            }

            String docTitle = options.getDocTitle();
            if (docTitle!=null) {
                parameters.getOptions().add("-doc-title");
                parameters.getOptions().add(docTitle);
            }

            // None of these options work for Scala >=2.8
            // options.getBottom();;
            // options.getTop();
            // options.getHeader();
            // options.getWindowTitle();

            List<String> additionalParameters = options.getAdditionalParameters();
            if (additionalParameters!=null) {
                parameters.getOptions().addAll(additionalParameters);
            }
        });
    }
}
