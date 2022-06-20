package com.tyron.builder.execution.plan;

import com.tyron.builder.api.Describable;
import com.tyron.builder.api.Task;
import com.tyron.builder.api.specs.Spec;
import com.tyron.builder.internal.resources.ResourceLockState;
import com.tyron.builder.internal.work.WorkerLeaseRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a graph of dependent work items, returned in execution order.
 */
public interface ExecutionPlan extends Describable {
    ExecutionPlan EMPTY = new ExecutionPlan() {
        @Override
        public void useFilter(Predicate<? super Task> filter) {
            throw new IllegalStateException();
        }

        @Override
        public void setContinueOnFailure(boolean continueOnFailure) {
            throw new IllegalStateException();
        }

        @Nullable
        @Override
        public Node selectNext(WorkerLeaseRegistry.WorkerLease workerLease, ResourceLockState resourceLockState) {
            return null;
        }

        @Override
        public void finishedExecuting(Node node) {
            throw new IllegalStateException();
        }

        @Override
        public void abortAllAndFail(Throwable t) {
        }

        @Override
        public void cancelExecution() {
        }

        @Override
        public TaskNode getNode(Task task) {
            throw new IllegalStateException();
        }

        @Override
        public void addNodes(Collection<? extends Node> nodes) {
            throw new IllegalStateException();
        }

        @Override
        public void addEntryTasks(Collection<? extends Task> tasks) {
            throw new IllegalStateException();
        }

        @Override
        public void determineExecutionPlan() {
        }

        @Override
        public Set<Task> getTasks() {
            return Collections.emptySet();
        }

        @Override
        public Set<Task> getRequestedTasks() {
            return Collections.emptySet();
        }

        @Override
        public List<Node> getScheduledNodes() {
            return Collections.emptyList();
        }

        @Override
        public List<Node> getScheduledNodesPlusDependencies() {
            return Collections.emptyList();
        }

        @Override
        public Set<Task> getFilteredTasks() {
            return Collections.emptySet();
        }

        @Override
        public void collectFailures(Collection<? super Throwable> failures) {
        }

        @Override
        public boolean allNodesComplete() {
            return true;
        }

        @Override
        public boolean hasNodesRemaining() {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public String getDisplayName() {
            return "empty";
        }
    };

    void useFilter(Predicate<? super Task> filter);

    void setContinueOnFailure(boolean continueOnFailure);

    /**
     * Selects a work item to run, returns null if there is no work remaining _or_ if no queued work is ready to run.
     */
    @Nullable
    Node selectNext(WorkerLeaseRegistry.WorkerLease workerLease, ResourceLockState resourceLockState);

    void finishedExecuting(Node node);

    void abortAllAndFail(Throwable t);

    void cancelExecution();

    /**
     * Returns the node for the supplied task that is part of this execution plan.
     *
     * @throws IllegalStateException When no node for the supplied task is part of this execution plan.
     */
    TaskNode getNode(Task task);

    void addNodes(Collection<? extends Node> nodes);

    void addEntryTasks(Collection<? extends Task> tasks);

    void determineExecutionPlan();

    /**
     * @return The set of all available tasks. This includes tasks that have not yet been executed, as well as tasks that have been processed.
     */
    Set<Task> getTasks();

    Set<Task> getRequestedTasks();

    List<Node> getScheduledNodes();

    List<Node> getScheduledNodesPlusDependencies();

    /**
     * @return The set of all filtered tasks that don't get executed.
     */
    Set<Task> getFilteredTasks();

    /**
     * Collects the current set of task failures into the given collection.
     */
    void collectFailures(Collection<? super Throwable> failures);

    boolean allNodesComplete();

    boolean hasNodesRemaining();

    /**
     * Returns the number of work items in the plan.
     */
    int size();
}
