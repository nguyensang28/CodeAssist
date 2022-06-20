package com.tyron.builder.execution;

import com.tyron.builder.api.Task;

import java.util.Collection;

public interface TaskSelectionResult {
    void collectTasks(Collection<? super Task> tasks);
}
