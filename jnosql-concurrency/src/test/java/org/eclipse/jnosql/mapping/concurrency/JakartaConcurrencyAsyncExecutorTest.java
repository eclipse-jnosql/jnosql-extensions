/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.concurrency;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("JakartaConcurrencyAsyncExecutor with a stubbed managed executor")
class JakartaConcurrencyAsyncExecutorTest {

    private static final int TIMEOUT_SECONDS = 10;

    private ExecutorService workers;
    private ManagedExecutorService managed;
    private List<String> lookedUp;

    @BeforeEach
    void setUp() {
        workers = Executors.newCachedThreadPool();
        managed = mock(ManagedExecutorService.class);
        lookedUp = new ArrayList<>();
        when(managed.supplyAsync(any())).thenAnswer(invocation ->
                CompletableFuture.supplyAsync(invocation.<java.util.function.Supplier<Object>>getArgument(0), workers));
    }

    @AfterEach
    void tearDown() {
        workers.shutdownNow();
    }

    private JakartaConcurrencyAsyncExecutor executor(Optional<String> configured) {
        return new JakartaConcurrencyAsyncExecutor(() -> configured, name -> {
            lookedUp.add(name);
            return managed;
        });
    }

    private static Throwable failureOf(java.util.concurrent.CompletionStage<?> stage) {
        return org.assertj.core.api.Assertions.catchThrowable(
                () -> stage.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should delegate the operation to the managed executor and complete with its result")
    void shouldDelegateToManagedExecutor() throws Exception {
        var result = executor(Optional.empty()).execute(() -> "value");

        assertThat(result.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo("value");
        verify(managed).supplyAsync(any());
    }

    @Test
    @DisplayName("Should use the default managed executor name when nothing is configured")
    void shouldUseDefaultJndiName() throws Exception {
        executor(Optional.empty()).execute(() -> 1).toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(lookedUp).containsExactly("java:comp/DefaultManagedExecutorService");
    }

    @Test
    @DisplayName("Should use the configured JNDI name")
    void shouldUseConfiguredJndiName() throws Exception {
        executor(Optional.of(" java:app/concurrent/JNoSQLExecutor "))
                .execute(() -> 1).toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(lookedUp).containsExactly("java:app/concurrent/JNoSQLExecutor");
    }

    @Test
    @DisplayName("Should not run the operation on the calling thread")
    void shouldNotRunOnCallingThread() throws Exception {
        var caller = Thread.currentThread();

        var thread = executor(Optional.empty()).execute(Thread::currentThread)
                .toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(thread).isNotSameAs(caller);
    }

    @Test
    @DisplayName("Should run each operation exactly once")
    void shouldRunOperationOnce() throws Exception {
        var runs = new AtomicInteger();

        executor(Optional.empty()).execute(runs::incrementAndGet)
                .toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(runs).hasValue(1);
        verify(managed, times(1)).supplyAsync(any());
    }

    @Test
    @DisplayName("Should complete exceptionally with the original exception when the operation fails")
    void shouldCompleteExceptionallyWithOriginalException() {
        var failure = new IllegalStateException("boom");

        var error = failureOf(executor(Optional.empty()).execute(() -> {
            throw failure;
        }));

        assertThat(error).isInstanceOf(ExecutionException.class).hasCause(failure);
    }

    @Test
    @DisplayName("Should complete exceptionally when the managed executor rejects the task")
    void shouldCompleteExceptionallyWhenRejected() {
        var rejection = new RejectedExecutionException("full");
        doThrow(rejection).when(managed).supplyAsync(any());

        var error = failureOf(executor(Optional.empty()).execute(() -> "never"));

        assertThat(error).isInstanceOf(ExecutionException.class).hasCause(rejection);
    }

    @Test
    @DisplayName("Should reject a null operation")
    void shouldRejectNullOperation() {
        assertThatThrownBy(() -> executor(Optional.empty()).execute(null))
                .isInstanceOf(NullPointerException.class);
        verifyNoMoreInteractions(managed);
    }

    @Test
    @DisplayName("Should execute several operations concurrently")
    void shouldExecuteConcurrently() throws Exception {
        var bothStarted = new CountDownLatch(2);
        var subject = executor(Optional.empty());

        var first = subject.execute(() -> awaitPeer(bothStarted));
        var second = subject.execute(() -> awaitPeer(bothStarted));

        assertThat(first.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
        assertThat(second.toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @DisplayName("Should return the managed executor's own future so its cancellation semantics are preserved")
    void shouldReturnManagedFuture() {
        var pending = new CompletableFuture<Object>();
        doReturn(pending).when(managed).supplyAsync(any());

        var stage = executor(Optional.empty()).execute(() -> "value");
        stage.toCompletableFuture().cancel(true);

        assertThat(stage).isSameAs(pending);
        assertThat(pending).isCancelled();
    }

    @Test
    @DisplayName("Should reject a blank JNDI name through the returned stage without looking it up")
    void shouldRejectBlankJndiName() {
        var error = failureOf(executor(Optional.of("  ")).execute(() -> "value"));

        assertThat(error).hasCauseInstanceOf(IllegalArgumentException.class)
                .cause().hasMessageContaining("jnosql.repository.async.concurrency.jndi-name");
        assertThat(lookedUp).isEmpty();
    }

    @Test
    @DisplayName("Should report a failed JNDI lookup through the returned stage keeping the cause")
    void shouldReportLookupFailure() {
        var naming = new NamingException("not bound");
        var subject = new JakartaConcurrencyAsyncExecutor(Optional::empty, name -> {
            throw naming;
        });

        var error = failureOf(subject.execute(() -> "value"));

        assertThat(error).hasCauseInstanceOf(IllegalStateException.class);
        assertThat(error.getCause()).hasCause(naming)
                .hasMessageContaining("java:comp/DefaultManagedExecutorService");
    }

    @Test
    @DisplayName("Should reject a resource that is not a managed executor service")
    void shouldRejectIncompatibleResource() {
        var subject = new JakartaConcurrencyAsyncExecutor(Optional::empty, name -> "not an executor");

        var error = failureOf(subject.execute(() -> "value"));

        assertThat(error.getCause()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ManagedExecutorService").hasMessageContaining("java.lang.String");
    }

    @Test
    @DisplayName("Should reject a missing resource")
    void shouldRejectNullResource() {
        var subject = new JakartaConcurrencyAsyncExecutor(Optional::empty, name -> null);

        assertThat(failureOf(subject.execute(() -> "value")).getCause())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("null");
    }

    @Test
    @DisplayName("Should retry the lookup after a failed one")
    void shouldRetryAfterFailedLookup() throws Exception {
        var attempts = new AtomicInteger();
        var subject = new JakartaConcurrencyAsyncExecutor(Optional::empty, name -> {
            if (attempts.incrementAndGet() == 1) {
                throw new NamingException("not yet bound");
            }
            return managed;
        });

        assertThat(failureOf(subject.execute(() -> "first"))).isNotNull();
        var second = subject.execute(() -> "second").toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(second).isEqualTo("second");
        assertThat(attempts).hasValue(2);
    }

    @Test
    @DisplayName("Should resolve the executor on every call without caching it")
    void shouldLookUpOnEveryCall() throws Exception {
        var subject = executor(Optional.empty());

        subject.execute(() -> 1).toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        subject.execute(() -> 2).toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(lookedUp).hasSize(2);
    }

    @Test
    @DisplayName("Should never shut down or close the managed executor")
    void shouldNotManageExecutorLifecycle() throws Exception {
        var subject = executor(Optional.empty());

        subject.execute(() -> 1).toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        failureOf(subject.execute(() -> {
            throw new IllegalStateException("boom");
        }));

        verify(managed, never()).shutdown();
        verify(managed, never()).shutdownNow();
        verify(managed, never()).close();
        verify(managed, times(2)).supplyAsync(any());
        verifyNoMoreInteractions(managed);
    }

    @Test
    @DisplayName("Should not resolve the same executor for different naming contexts")
    void shouldNotShareExecutorsAcrossContexts() throws Exception {
        var otherManaged = mock(ManagedExecutorService.class);
        when(otherManaged.supplyAsync(any())).thenReturn(CompletableFuture.completedFuture("other"));
        var first = new JakartaConcurrencyAsyncExecutor(Optional::empty, name -> managed);
        var second = new JakartaConcurrencyAsyncExecutor(Optional::empty, name -> otherManaged);

        assertThat(first.execute(() -> "mine").toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .isEqualTo("mine");
        assertThat(second.execute(() -> "mine").toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .isEqualTo("other");
        verify(managed).supplyAsync(any());
        verify(otherManaged).supplyAsync(any());
    }

    @Test
    @DisplayName("Should require the collaborators")
    void shouldRequireCollaborators() {
        assertThatCode(() -> new JakartaConcurrencyAsyncExecutor(null, name -> managed))
                .isInstanceOf(NullPointerException.class);
        assertThatCode(() -> new JakartaConcurrencyAsyncExecutor(Optional::empty, null))
                .isInstanceOf(NullPointerException.class);
    }

    private static boolean awaitPeer(CountDownLatch latch) {
        latch.countDown();
        try {
            return latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
