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

import eu.etaxonomy.cdm.api.application.CdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.io.common.events.IoProgressEvent;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 */
public abstract class CdmIoBase<STATE extends IoStateBase, RESULT extends IoResultBase>
	    extends CdmRepository
        implements ICdmIO<STATE>, IIoObservable {

    private static final long serialVersionUID = -2216451655392574659L;
    private static final Logger logger = Logger.getLogger(CdmIoBase.class);

    private final Set<IIoObserver> observers = new HashSet<IIoObserver>();
    protected String ioName = null;


    protected CdmIoBase() {
        super();
        this.ioName = this.getClass().getSimpleName();
    }

//******************** Observers *********************************************************

    @Override
    public boolean addObserver(IIoObserver observer){
        return observers.add(observer);
    }

    @Override
    public Set<IIoObserver> getObservers() {
        return observers;
    }

    @Override
    public void addObservers(Set<IIoObserver> newObservers) {
        for (IIoObserver observer : newObservers){
            this.observers.add(observer);
        }
    }

    @Override
    public int countObservers(){
        return observers.size();
    }

    @Override
    public boolean removeObserver(IIoObserver observer){
        return observers.remove(observer);
    }

    @Override
    public void removeObservers(){
        observers.removeAll(observers);
    }

    @Override
    public void fire(IIoEvent event){
        for (IIoObserver observer: observers){
            observer.handleEvent(event);
        }
    }

//******************** End Observers *********************************************************



    public RESULT invoke(STATE state) {
        if (isIgnore(state)){
            logger.info("No invoke for " + ioName + " (ignored)");
            return getNoDataResult(state);
        }else{
            updateProgress(state, "Invoking " + ioName);
            state.setResult(getDefaultResult(state));
            doInvoke(state);
            RESULT result = (RESULT)state.getResult();
            return result;
        }
    }

    protected abstract RESULT getNoDataResult(STATE state);
    protected abstract RESULT getDefaultResult(STATE state);


//    //TODO move up to CdmIoBase once ImportResult is used here
//    @Override
//    public ImportResult invoke(STATE state) {
//        if (isIgnore( state)){
//            logger.info("No invoke for " + ioName + " (ignored)");
//            return true;
//        }else{
//            updateProgress(state, "Invoking " + ioName);
//            doInvoke(state);
//            return state.isSuccess();
//        }
//    }


    public int countSteps(){
        return 1;
    }



    @Autowired
    private SessionFactory sessionFactory;

    /**
     * flush the current session
     */
    //TODO move into super class CdmApplicationDefaultConfiguration#flush() ?
    public void flush() {
        sessionFactory.getCurrentSession().flush();
    }

    @Override
    //TODO seems to be exact duplicate of CdmApplicationDefaultConfiguration#startTransaction(), remove duplicate
    public TransactionStatus startTransaction() {
        return startTransaction(false);
    }

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
            // org.springframework.orm.hibernate5.HibernateTransactionManager
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
    //==> no
    public void rollbackTransaction(TransactionStatus txStatus){
        PlatformTransactionManager txManager = super.getTransactionManager();
        txManager.rollback(txStatus);
        return;
    }

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

    /**
     * invoke method to be implemented by implementing classes
     * @param state
     * @return
     */
    protected abstract void doInvoke(STATE state);

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
    }

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
        String className = stackTrace[stackDepth].getClassName();
		Class<?> declaringClass;
		try {
			declaringClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			declaringClass = this.getClass();
		}

        IoProblemEvent event = IoProblemEvent.NewInstance(declaringClass, message, dataLocation,
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

}
