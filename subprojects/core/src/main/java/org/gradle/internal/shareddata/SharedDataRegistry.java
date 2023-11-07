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

package org.gradle.internal.shareddata;

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.provider.Provider;
import org.gradle.api.shareddata.ProjectSharedData;

import javax.annotation.Nullable;

public interface SharedDataRegistry {
    <T> void registerSharedDataProducer(ProjectInternal providerProject, Class<T> dataType, @Nullable String dataIdentifier, Provider<T> dataProvider);
    <T> ProviderInternal<T> obtainData(ProjectInternal consumerProject, Class<T> dataType, @Nullable String dataIdentifier, ProjectSharedData.SingleSourceIdentifier dataSourceIdentifier);
}
