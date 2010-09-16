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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.application.CdmApplicationDefaultConfiguration;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmIoBase<STATE extends IoStateBase> extends CdmApplicationDefaultConfiguration implements ICdmIO<STATE> {
	private static Logger logger = Logger.getLogger(CdmIoBase.class);

	protected String ioName = null;

	protected abstract boolean doInvoke(STATE state);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmExport#invoke(eu.etaxonomy.cdm.io.common.ExportStateBase)
	 */
	public boolean invoke(STATE state) {
		if (isIgnore( state)){
			logger.warn("No invoke for " + ioName + " (ignored)");
			return true;
		}else{
			updateProgress(state, "Invoking " + ioName);
			return doInvoke(state);
		}
	}
	
	@Autowired
	SessionFactory sessionFactory;
	
	public void flush() {		
		sessionFactory.getCurrentSession().flush();
	}
	
	public TransactionStatus startTransaction() {
		return startTransaction(false);
	}
	
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
			// org.springframework.orm.hibernate3.HibernateTransactionManager
			// provides more transaction/session-related debug information.
		}
		
		TransactionStatus txStatus = super.getTransactionManager().getTransaction(txDef);
		return txStatus;
	}

	public void commitTransaction(TransactionStatus txStatus){
		PlatformTransactionManager txManager = super.getTransactionManager();
		txManager.commit(txStatus);
		return;
	}
	
	/**
	 * 
	 */
	public CdmIoBase() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ICdmIO#check(eu.etaxonomy.cdm.io.common.IIoConfigurator)
	 */
	public boolean check(STATE state) {
		if (isIgnore(state)){
			logger.warn("No check for " + ioName + " (ignored)");
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
		if(state.getConfig().getProgressMonitor() != null){
			IProgressMonitor monitor = state.getConfig().getProgressMonitor();
			monitor.worked(worked);
			monitor.subTask(message);
		}
	}
}
