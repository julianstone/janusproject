/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.kernel.executor;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link FutureTask} that is {@link Runnable}. Successful
 * execution of the <tt>run</tt> method causes completion of the
 * <tt>Future</tt> and allows access to its results.
 * 
 * @param <V>
 * @see FutureTask
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusFutureTask<V> extends FutureTask<V> {

	private final AtomicBoolean treated = new AtomicBoolean(false);
	private WeakReference<Thread> thread = null;

	/**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Runnable}, and arrange that {@code get} will return the
     * given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion. If
     * you don't need a particular result, consider using
     * constructions of the form:
     * {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */
	JanusFutureTask(Runnable runnable, V result) {
		super(runnable, result);
	}
	
    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Callable}.
     *
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     */
	JanusFutureTask(Callable<V> callable) {
		super(callable);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void done() {
		if (!this.treated.getAndSet(true)) {
			// Test the throw of an exception
			try {
				// This function should not timeout because the task should be terminated.
				get(10, TimeUnit.SECONDS);
			}
			catch(Throwable e) {
				ExecutorUtil.log(getThread(), e);
			}
		}
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException {
		try {
			return super.get();
		}
		catch(ExecutionException e) {
			Throwable ex = e;
			while (ex instanceof ExecutionException) {
				ex = e.getCause();
			}
			if (ex instanceof ChuckNorrisException) {
				return null;
			}
			throw e;
		}
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		try {
			return super.get(timeout, unit);
		}
		catch(ExecutionException e) {
			Throwable ex = e;
			while (ex instanceof ExecutionException) {
				ex = e.getCause();
			}
			if (ex instanceof ChuckNorrisException) {
				return null;
			}
			throw e;
		}
	}

	/** Set the running thread.
	 * 
	 * @param thread
	 */
	void setThread(Thread thread) {
		this.thread = new WeakReference<>(thread);
	}

	/** Replies the thread that is running the task associated to this future.
	 * 
	 * @return the thread, never <code>null</code>.
	 */
	public Thread getThread() {
		return this.thread.get();
	}
	
	/** Replies the task associated to this future is running on the calling thread.
	 * 
	 * @return <code>true</code> if the current thread is running the associated
	 * task, <code>false</code> otherwie.
	 */
	public boolean isCurrentThread() {
		return Thread.currentThread()==this.thread.get();
	}

}