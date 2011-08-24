package eu.etaxonomy.cdm.api.conversation;

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

public class ConversationHolderMock extends ConversationHolder {


	/**
	 * This method has to be called when starting a new unit-of-work. All required resources are
	 * bound so that SessionFactory.getCurrentSession() returns the right session for this conversation
	 */
	public void bind() {
		
	}
//	
//	public SessionHolder getSessionHolder(){
//		return null;
//	}
//	
//	/**
//	 * @return
//	 */
//	private DataSource getDataSource() {
//		return null;
//	}
//
//	/**
//	 * @return true if this longSession is bound to the session factory.
//	 */
//	public boolean isBound(){
//		return false;
//	}
//	
	/**
	 * Creates an instance of TransactionStatus and binds it to this conversation manager.
	 * At the moment we allow only on transaction per conversation holder.
	 * 
	 * @return the transaction status bound to this conversation holder
	 */
	public TransactionStatus startTransaction(){
		return null;
	}
//	
//	/** 
//	 * @return if there is a running transaction
//	 */
//	public boolean isTransactionActive(){
//		return false;
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.hibernate.Session#evict(java.lang.Object object)
//	 */
//	public void evict(Object object){
//
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.hibernate.Session#refresh(java.lang.Object object)
//	 */
//	public void refresh(Object object){
//		
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.hibernate.Session#clear()
//	 */
//	public void clear(){
//		
//	}
//	
	/**
	 * Commit the running transaction.
	 */
	public void commit(){
		
	}
	
	/**
	 * Commit the running transaction but optionally start a
	 * new one right away.
	 * 
	 * @param restartTransaction whether to start a new transaction
	 */
	public TransactionStatus commit(boolean restartTransaction){
		return null;
	}

	/**
	 * @return the session associated with this conversation manager 
	 */
	private Session getSession() {
		return null;
	}
//	
//	/** 
//	 * @return the session factory that is bound to this conversation manager
//	 */
//	private SessionFactory getSessionFactory() {
//		return null;
//	}
//
//	public void delete(Object object){
//
//	}
//	
//	/**
//	 * Facades Session.lock()
//	 */
//	public void lock(Object persistentObject, LockMode lockMode) {
//		
//	}
//	
//	public void lock(String entityName, Object persistentObject, LockMode lockMode){
//		getSession().lock(entityName, persistentObject, lockMode);
//	}
//
//	/**
//	 * @return the definition
//	 */
//	public TransactionDefinition getDefinition() {
//		return null;
//	}
//
//	/**
//	 * @param definition the definition to set
//	 */
//	public void setDefinition(TransactionDefinition definition) {
//
//	}
//	
//	/**
//	 * Register to get updated after any interaction with the datastore
//	 */
//	public void registerForDataStoreChanges(IConversationEnabled observer) {
//
//	}
//	
//	/**
//	 * Register to get updated after any interaction with the datastore
//	 */
//	public void unregisterForDataStoreChanges(IConversationEnabled observer) {
//		
//	}
//	
	/**
	 * Free resources bound to this conversationHolder
	 */
	public void close(){

	}
//	
//	public boolean isClosed(){
//		return false;
//	}

}
