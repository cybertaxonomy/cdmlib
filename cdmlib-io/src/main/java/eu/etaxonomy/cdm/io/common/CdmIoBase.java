/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.application.CdmApplicationDefaultConfiguration;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.io.common.events.IoProgressEvent;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmIoBase<STATE extends IoStateBase> extends CdmApplicationDefaultConfiguration
        implements ICdmIO<STATE>, IIoObservable {
    private static final Logger logger = Logger.getLogger(CdmIoBase.class);

    private final Set<IIoObserver> observers = new HashSet<IIoObserver>();
    protected String ioName = null;


    /**
     *
     */
    public CdmIoBase() {
        super();
        this.ioName = this.getClass().getSimpleName();
    }

//******************** Observers *********************************************************

    @Override
    public boolean addObserver(IIoObserver observer){
        return observers.add(observer);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IIoObservable#getObservers()
     */
    @Override
    public Set<IIoObserver> getObservers() {
        return observers;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IIoObservable#addObservers(java.util.Set)
     */
    @Override
    public void addObservers(Set<IIoObserver> newObservers) {
        for (IIoObserver observer : newObservers){
            this.observers.add(observer);
        }
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IIoObservable#countObservers()
     */
    @Override
    public int countObservers(){
        return observers.size();
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IIoObservable#removeObserver(eu.etaxonomy.cdm.io.common.events.IIoObserver)
     */
    @Override
    public boolean removeObserver(IIoObserver observer){
        return observers.remove(observer);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IIoObservable#removeObservers()
     */
    @Override
    public void removeObservers(){
        observers.removeAll(observers);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ICdmIO#fire(eu.etaxonomy.cdm.io.common.events.IIoEvent)
     */
    @Override
    public void fire(IIoEvent event){
        for (IIoObserver observer: observers){
            observer.handleEvent(event);
        }
    }

//******************** End Observers *********************************************************



    public int countSteps(){
        return 1;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ICdmExport#invoke(eu.etaxonomy.cdm.io.common.ExportStateBase)
     */
    @Override
    public boolean invoke(STATE state) {
        if (isIgnore( state)){
            logger.info("No invoke for " + ioName + " (ignored)");
            return true;
        }else{
            updateProgress(state, "Invoking " + ioName);
            doInvoke(state);
            return state.isSuccess();
        }
    }

    /**
     * invoke method to be implemented by implementing classes
     * @param state
     * @return
     */
    protected abstract void doInvoke(STATE state);


    @Autowired
    SessionFactory sessionFactory;

    /**
     * flush the current session
     */
    //TODO move into super class CdmApplicationDefaultConfiguration#flush() ?
    public void flush() {
        sessionFactory.getCurrentSession().flush();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.CdmApplicationDefaultConfiguration#startTransaction()
     */
    @Override
    //TODO seems to be exact duplicate of CdmApplicationDefaultConfiguration#startTransaction(), remove duplicate
    public TransactionStatus startTransaction() {
        return startTransaction(false);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.CdmApplicationDefaultConfiguration#startTransaction(java.lang.Boolean)
     */
    @Override
    //TODO seems to be exact duplicate of CdmApplicationDefaultConfiguration#startTransaction(java.lang.Boolean)
    public TransactionStatus startTransaction(Boolean readOnly) {

        DefaultTransactionDefinition defaultTxDef = new DefaultTransactionDefinition();
        defaultTxDef.setReadOnly(readOnly);
        TransactionDefinition txDef = defaultTxDef;

        // Log some transaction-related debug information.
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction name = " + txDef.getName());
            logger.debug("Transaction facets:");
            logger.debug("Propagation behavior = " + txDef.getPropagationBehavior());
            logger.debug("Isolation level = " + txDef.getIsolationLevel());
            logger.debug("Timeout = " + txDef.getTimeout());
            logger.debug("Read Only = " + txDef.isReadOnly());
            // org.springframework.orm.hibernate4.HibernateTransactionManager
            // provides more transaction/session-related debug information.
        }

        TransactionStatus txStatus = super.getTransactionManager().getTransaction(txDef);
        return txStatus;
    }

    @Override
    //TODO seems to be exact duplicate of CdmApplicationDefaultConfiguration#startTransaction(java.lang.Boolean), remove duplicate?
    public void commitTransaction(TransactionStatus txStatus){
        PlatformTransactionManager txManager = super.getTransactionManager();
        txManager.commit(txStatus);
        return;
    }

    //TODO move into super class CdmApplicationDefaultConfiguration#startTransaction(java.lang.Boolean)
    public void rollbackTransaction(TransactionStatus txStatus){
        PlatformTransactionManager txManager = super.getTransactionManager();
        txManager.rollback(txStatus);
        return;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ICdmIO#check(eu.etaxonomy.cdm.io.common.IIoConfigurator)
     */
    @Override
    public boolean check(STATE state) {
        if (isIgnore(state)){
            logger.info("No check for " + ioName + " (ignored)");
            return true;
        }else{
            return doCheck(state);
        }
    }

    protected abstract boolean doCheck(STATE state);


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ICdmIO#invoke(eu.etaxonomy.cdm.io.common.IIoConfigurator, java.util.Map)
     */
//	public boolean invoke(T config,
//			Map<String, MapWrapper<? extends CdmBase>> stores) {
//		if (isIgnore(config)){
//			logger.warn("No invoke for " + ioName + " (ignored)");
//			return true;
//		}else{
//			return doInvoke(config, stores);
//		}
//	}

//	protected abstract boolean doInvoke(T config,
//			Map<String, MapWrapper<? extends CdmBase>> stores);


    /**
     * Returns true if this (IO-)class should be ignored during the import/export process.
     * This information is usually stored in the configuration
     * @param config
     * @return
     */
    protected abstract boolean isIgnore(STATE state);

    protected <T extends CdmBase> T getInstance(Class<? extends T> clazz){
        T result = null;
        try {
            Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            result = constructor.newInstance();
        } catch (InstantiationException e) {
            logger.error("Class " + clazz.getSimpleName()+" could not be instantiated. Class = " );
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("Constructor of class "+clazz.getSimpleName()+" could not be accessed." );
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            logger.error("SecurityException for Constructor of class "+clazz.getSimpleName()+"." );
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error("Empty Constructor does not exist for class "+clazz.getSimpleName()+"." );
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            logger.error("Empty Constructor could not be invoked for class "+clazz.getSimpleName()+"." );
            e.printStackTrace();
        }
        return result;
    }


    protected String getSuccessString(boolean success){
        if (success){
            return "with success";
        }else{
            return "with errors";
        }
    }

    @Override
    public void updateProgress(STATE state, String message) {
        updateProgress(state, message, 1);
    };

    @Override
    public void updateProgress(STATE state, String message, int worked) {
        IProgressMonitor progressMonitor = state.getConfig().getProgressMonitor();
        if(progressMonitor != null){
            progressMonitor.worked(worked);
            progressMonitor.subTask(message);
        }
    }

    @Override
    public void warnProgress(STATE state, String message, Throwable e) {
        if(state.getConfig().getProgressMonitor() != null){
            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            if (e == null) {
                monitor.warning(message);
            }else{
                monitor.warning(message, e);
            }
        }
    }

    protected void fireProgressEvent(String message, String location) {
        IoProgressEvent event = new IoProgressEvent();
        event.setThrowingClass(this.getClass());
        event.setMessage(message);
        event.setLocation(location);
//		int linenumber = new Exception().getStackTrace()[0].getLineNumber();
        fire(event);
    }


    protected void fireWarningEvent(String message, String dataLocation, Integer severity) {
        fireWarningEvent(message, dataLocation, severity, 1);
    }

    protected void fireWarningEvent(String message, String dataLocation, Integer severity, int stackDepth) {
        stackDepth++;
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        int lineNumber = stackTrace[stackDepth].getLineNumber();
        String methodName = stackTrace[stackDepth].getMethodName();

        IoProblemEvent event = IoProblemEvent.NewInstance(this.getClass(), message, dataLocation,
                lineNumber, severity, methodName);

        //for performance improvement one may read:
        //http://stackoverflow.com/questions/421280/in-java-how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
//		Object o = new SecurityManager().getSecurityContext();


        fire(event);
    }

    protected boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }

    protected boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }


//	/**
//	   * Returns the first stack trace element of the first class not equal to "StackTraceUtils" or "LogUtils" and aClass. <br />
//	   * Stored in array of the callstack. <br />
//	   * Allows to get past a certain class.
//	   * @param aclass class to get pass in the stack trace. If null, only try to get past StackTraceUtils.
//	   * @return stackTraceElement (never null, because if aClass is not found, returns first class past StackTraceUtils)
//	   * @throws AssertionFailedException if resulting statckTrace is null (RuntimeException)
//	   */
//	  public static StackTraceElement getCallingStackTraceElement(final Class aclass) {
//	    final Throwable           t         = new Throwable();
//	    final StackTraceElement[] ste       = t.getStackTrace();
//	    int index = 1;
//	    final int limit = ste.length;
//	    StackTraceElement   st        = ste[index];
//	    String              className = st.getClassName();
//	    boolean aclassfound = false;
//	    if(aclass == null) {
//	        aclassfound = true;
//	    }
//	    StackTraceElement   resst = null;
//	    while(index < limit) {
//	        if(shouldExamine(className, aclass) == true) {
//	                if(resst == null) {
//	                        resst = st;
//	                }
//	                if(aclassfound == true) {
//	                        final StackTraceElement ast = onClassFound(aclass, className, st);
//	                        if(ast != null) {
//	                                resst = ast;
//	                                break;
//	                        }
//	                }
//	                else
//	                {
//	                        if(aclass != null && aclass.getName().equals(className) == true) {
//	                                aclassfound = true;
//	                        }
//	                }
//	        }
//	        index = index + 1;
//	        st        = ste[index];
//	        className = st.getClassName();
//	    }
//	    if(resst == null)  {
//	        throw new AssertionFailedException(StackTraceUtils.getClassMethodLine() + " null argument:" + "stack trace should null"); //$NON-NLS-1$
//	    }
//	    return resst;
//	  }
//
//	  static private boolean shouldExamine(String className, Class aclass) {
//	      final boolean res = StackTraceUtils.class.getName().equals(className) == false && (className.endsWith(LOG_UTILS
//	        	) == false || (aclass !=null && aclass.getName().endsWith(LOG_UTILS)));
//	      return res;
//	  }
//
//	  static private StackTraceElement onClassFound(Class aclass, String className, StackTraceElement st) {
//	      StackTraceElement   resst = null;
//	      if(aclass != null && aclass.getName().equals(className) == false)
//	      {
//	          resst = st;
//	      }
//	      if(aclass == null)
//	      {
//	          resst = st;
//	      }
//	      return resst;
//	  }


}
