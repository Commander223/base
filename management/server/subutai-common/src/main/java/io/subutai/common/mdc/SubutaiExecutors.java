package io.subutai.common.mdc;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.MDC;


/**
 * MDC Context Aware Custom Subutai Executors
 */
public class SubutaiExecutors
{

    private SubutaiExecutors()
    {
    }


    public static ExecutorService newFixedThreadPool( int nThreads )
    {
        return new SubutaiThreadPoolExecutor( nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>() );
    }


    public static ExecutorService newCachedThreadPool()
    {
        return new SubutaiThreadPoolExecutor( 0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>() );
    }


    public static ScheduledExecutorService newSingleThreadScheduledExecutor()
    {
        return new DelegatedScheduledExecutorService( new SubutaiScheduledThreadPoolExecutor( 1 ) );
    }


    public static ExecutorService newSingleThreadExecutor()
    {
        return new FinalizableDelegatedExecutorService(
                new SubutaiThreadPoolExecutor( 1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>() ) );
    }


    //helpers
    static class FinalizableDelegatedExecutorService extends DelegatedExecutorService
    {
        FinalizableDelegatedExecutorService( ExecutorService executor )
        {
            super( executor );
        }


        @Override
        protected void finalize() throws Throwable
        {
            try
            {
                super.shutdown();
            }
            finally
            {
                super.finalize();
            }
        }
    }


    static class DelegatedScheduledExecutorService extends DelegatedExecutorService implements ScheduledExecutorService
    {
        private final ScheduledExecutorService e;


        DelegatedScheduledExecutorService( ScheduledExecutorService executor )
        {
            super( executor );
            e = executor;
        }


        @Override
        public ScheduledFuture<?> schedule( Runnable command, long delay, TimeUnit unit )
        {
            return e.schedule( command, delay, unit );
        }


        @Override
        public <V> ScheduledFuture<V> schedule( Callable<V> callable, long delay, TimeUnit unit )
        {
            return e.schedule( callable, delay, unit );
        }


        @Override
        public ScheduledFuture<?> scheduleAtFixedRate( Runnable command, long initialDelay, long period, TimeUnit unit )
        {
            return e.scheduleAtFixedRate( command, initialDelay, period, unit );
        }


        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay( Runnable command, long initialDelay, long delay,
                                                          TimeUnit unit )
        {
            return e.scheduleWithFixedDelay( command, initialDelay, delay, unit );
        }
    }


    static class DelegatedExecutorService extends AbstractExecutorService
    {
        private final ExecutorService e;


        DelegatedExecutorService( ExecutorService executor )
        {
            e = executor;
        }


        @Override
        public void execute( Runnable command )
        {
            e.execute( command );
        }


        @Override
        public void shutdown()
        {
            e.shutdown();
        }


        @Override
        public List<Runnable> shutdownNow()
        {
            return e.shutdownNow();
        }


        @Override
        public boolean isShutdown()
        {
            return e.isShutdown();
        }


        @Override
        public boolean isTerminated()
        {
            return e.isTerminated();
        }


        @Override
        public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
        {
            return e.awaitTermination( timeout, unit );
        }


        @Override
        public Future<?> submit( Runnable task )
        {
            return e.submit( task );
        }


        @Override
        public <T> Future<T> submit( Callable<T> task )
        {
            return e.submit( task );
        }


        @Override
        public <T> Future<T> submit( Runnable task, T result )
        {
            return e.submit( task, result );
        }


        @Override
        public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException
        {
            return e.invokeAll( tasks );
        }


        @Override
        public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
                throws InterruptedException
        {
            return e.invokeAll( tasks, timeout, unit );
        }


        @Override
        public <T> T invokeAny( Collection<? extends Callable<T>> tasks )
                throws InterruptedException, ExecutionException
        {
            return e.invokeAny( tasks );
        }


        @Override
        public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
                throws InterruptedException, ExecutionException, TimeoutException
        {
            return e.invokeAny( tasks, timeout, unit );
        }
    }


    private static class SubutaiThreadPoolExecutor extends ThreadPoolExecutor
    {
        private Map<String, String> parentContext;
        private Map<String, String> currentContext;


        public SubutaiThreadPoolExecutor( final int corePoolSize, final int maximumPoolSize, final long keepAliveTime,
                                          final TimeUnit unit, final BlockingQueue<Runnable> workQueue )
        {
            super( corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue );
        }


        @Override
        public Future<?> submit( final Runnable task )
        {
            parentContext = MDC.getCopyOfContextMap();
            return super.submit( task );
        }


        @Override
        public <T> Future<T> submit( final Runnable task, final T result )
        {
            parentContext = MDC.getCopyOfContextMap();
            return super.submit( task, result );
        }


        @Override
        public <T> Future<T> submit( final Callable<T> task )
        {
            parentContext = MDC.getCopyOfContextMap();
            return super.submit( task );
        }


        @Override
        public void execute( final Runnable command )
        {
            parentContext = MDC.getCopyOfContextMap();
            super.execute( command );
        }


        @Override
        protected void beforeExecute( final Thread t, final Runnable r )
        {
            if ( parentContext != null )
            {
                currentContext = MDC.getCopyOfContextMap();
                MDC.setContextMap( parentContext );
            }
            super.beforeExecute( t, r );
        }


        @Override
        protected void afterExecute( final Runnable r, final Throwable t )
        {
            if ( parentContext != null )
            {
                MDC.setContextMap( currentContext );
            }
            super.afterExecute( r, t );
        }
    }


    private static class SubutaiScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor
    {
        private Map<String, String> parentContext;
        private Map<String, String> currentContext;


        public SubutaiScheduledThreadPoolExecutor( final int corePoolSize )
        {
            super( corePoolSize );
        }


        @Override
        public Future<?> submit( final Runnable task )
        {
            parentContext = MDC.getCopyOfContextMap();
            return super.submit( task );
        }


        @Override
        public <T> Future<T> submit( final Runnable task, final T result )
        {
            parentContext = MDC.getCopyOfContextMap();
            return super.submit( task, result );
        }


        @Override
        public <T> Future<T> submit( final Callable<T> task )
        {
            parentContext = MDC.getCopyOfContextMap();
            return super.submit( task );
        }


        @Override
        public void execute( final Runnable command )
        {
            parentContext = MDC.getCopyOfContextMap();
            super.execute( command );
        }


        @Override
        protected void beforeExecute( final Thread t, final Runnable r )
        {
            if ( parentContext != null )
            {
                currentContext = MDC.getCopyOfContextMap();
                MDC.setContextMap( parentContext );
            }
            super.beforeExecute( t, r );
        }


        @Override
        protected void afterExecute( final Runnable r, final Throwable t )
        {
            if ( parentContext != null )
            {
                MDC.setContextMap( currentContext );
            }
            super.afterExecute( r, t );
        }
    }
}
