package eu.etaxonomy.cdm.persistence.validation;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ValidationThreadPoolExecutor extends ThreadPoolExecutor {

	public ValidationThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}


	public ValidationThreadPoolExecutor(int arg0, int arg1, long arg2, TimeUnit arg3, BlockingQueue<Runnable> arg4, ThreadFactory arg5)
	{
		super(arg0, arg1, arg2, arg3, arg4, arg5);
	}


	public ValidationThreadPoolExecutor(int arg0, int arg1, long arg2, TimeUnit arg3, BlockingQueue<Runnable> arg4, RejectedExecutionHandler arg5)
	{
		super(arg0, arg1, arg2, arg3, arg4, arg5);
	}


	public ValidationThreadPoolExecutor(int arg0, int arg1, long arg2, TimeUnit arg3, BlockingQueue<Runnable> arg4, ThreadFactory arg5,
			RejectedExecutionHandler arg6)
	{
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}


	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
	}

}
