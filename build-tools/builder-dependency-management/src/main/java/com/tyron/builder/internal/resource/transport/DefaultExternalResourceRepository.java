/*
 * Copyright 2012 the original author or authors.
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

package com.tyron.builder.internal.resource.transport;


import com.tyron.builder.internal.resource.ExternalResource;
import com.tyron.builder.internal.resource.ExternalResourceName;
import com.tyron.builder.internal.resource.ExternalResourceRepository;
import com.tyron.builder.internal.resource.transfer.AccessorBackedExternalResource;
import com.tyron.builder.internal.resource.transfer.ExternalResourceAccessor;
import com.tyron.builder.internal.resource.transfer.ExternalResourceLister;
import com.tyron.builder.internal.resource.transfer.ExternalResourceUploader;

public class DefaultExternalResourceRepository implements ExternalResourceRepository {
    private final String name;
    private final ExternalResourceAccessor accessor;
    private final ExternalResourceUploader uploader;
    private final ExternalResourceLister lister;

    public DefaultExternalResourceRepository(
        String name,
        ExternalResourceAccessor accessor,
        ExternalResourceUploader uploader,
        ExternalResourceLister lister
    ) {
        this.name = name;
        this.accessor = accessor;
        this.uploader = uploader;
        this.lister = lister;
    }

    @Override
    public ExternalResourceRepository withProgressLogging() {
        return this;
    }

    @Override
    public ExternalResource resource(ExternalResourceName resource, boolean revalidate) {
        return new AccessorBackedExternalResource(resource, accessor, uploader, lister, revalidate);
    }

    @Override
    public ExternalResource resource(ExternalResourceName resource) {
        return resource(resource, false);
    }

    public String toString() {
        return name;
    }

}
