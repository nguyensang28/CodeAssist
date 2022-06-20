package com.tyron.builder.cache.internal;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;
import com.tyron.builder.internal.UncheckedException;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Synchronizes access to some resource, by making sure that 2 threads do not try to produce it at the same time.
 * The resource to be accessed is represented by a key, and the factory is whatever needs to be done to produce it.
 * This is <b>not</b> a cache. The factory should take care of caching wherever it makes sense.
 *
 * <p>
 * The concurrency level and memory usage depend on the implementation, see {@link #adaptive()}, {@link #striped()} and {@link #serial()} for details.
 * </p>
 *
 * @param <T> the type of key to lock on
 */
public abstract class ProducerGuard<T> {

    /**
     * Creates a {@link ProducerGuard} which guarantees that different keys will block on different locks,
     * ensuring maximum concurrency. This comes at the cost of allocating locks for each key, leading to
     * relatively high memory pressure. If the above guarantee is not necessary, consider using a {@link #striped()}
     * guard instead.
     */
    public static <T> ProducerGuard<T> adaptive() {
        return new AdaptiveProducerGuard<T>();
    }

    /**
     * Creates a {@link ProducerGuard} which evenly spreads calls over a fixed number of locks.
     * This means that in some cases two different keys can block on the same lock. The benefit of
     * this strategy is that it uses only a fixed amount of memory. If your code depends on
     * different keys always getting different locks, use a {@link #adaptive()} guard instead.
     */
    public static <T> ProducerGuard<T> striped() {
        return new StripedProducerGuard<T>();
    }

    /**
     * Creates a {@link ProducerGuard} which limits concurrency to a single factory at a time,
     * ignoring the key. This is mainly useful for testing.
     */
    public static <T> ProducerGuard<T> serial() {
        return new SerialProducerGuard<T>();
    }

    private ProducerGuard() {

    }

    /**
     * Runs the given factory, guarded by the given key.
     *
     * @param key the key to lock on
     * @param supplier the supplier to run under the lock
     * @param <V> the type returned by the factory
     * @return the value returned by the factory
     */
    public abstract <V> V guardByKey(T key, Supplier<V> supplier);

    private static class AdaptiveProducerGuard<T> extends ProducerGuard<T> {
        private final Set<T> producing = Sets.newHashSet();

        @Override
        public <V> V guardByKey(T key, Supplier<V> supplier) {
            synchronized (producing) {
                while (!producing.add(key)) {
                    try {
                        producing.wait();
                    } catch (InterruptedException e) {
                        throw UncheckedException.throwAsUncheckedException(e);
                    }
                }
            }
            try {
                return supplier.get();
            } finally {
                synchronized (producing) {
                    producing.remove(key);
                    producing.notifyAll();
                }
            }
        }
    }

    private static class StripedProducerGuard<T> extends ProducerGuard<T> {
        private final Striped<Lock> locks = Striped.lock(Runtime.getRuntime().availableProcessors() * 4);

        @Override
        public <V> V guardByKey(T key, Supplier<V> supplier) {
            Lock lock = locks.get(key);
            try {
                lock.lock();
                return supplier.get();
            } finally {
                lock.unlock();
            }
        }
    }

    private static class SerialProducerGuard<T> extends ProducerGuard<T> {
        private final Lock lock = new ReentrantLock();

        @Override
        public <V> V guardByKey(T key, Supplier<V> supplier) {
            try {
                lock.lock();
                return supplier.get();
            } finally {
                lock.unlock();
            }
        }
    }
}
