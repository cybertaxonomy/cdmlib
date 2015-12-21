package eu.etaxonomy.cdm.api.conversation;

import javax.sql.DataSource;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public class ConversationHolderMock extends ConversationHolder {


	public ConversationHolderMock() {

	}
	/**
	 * This method has to be called when starting a new unit-of-work. All required resources are
	 * bound so that SessionFactory.getCurrentSession() returns the right session for this conversation
	 */
	@Override
    public void bind() {

	}

	@Override
    public SessionHolder getSessionHolder(){
		return null;
	}

	/**
	 * @return
	 */
	private DataSource getDataSource() {
		return null;
	}

	/**
	 * @return true if this longSession is bound to the session factory.
	 */
	@Override
    public boolean isBound(){
		return false;
	}

	/**
	 * Creates an instance of TransactionStatus and binds it to this conversation manager.
	 * At the moment we allow only on transaction per conversation holder.
	 *
	 * @return the transaction status bound to this conversation holder
	 */
	@Override
    public TransactionStatus startTransaction(){
		return null;
	}

	/**
	 * @return if there is a running transaction
	 */
	@Override
    public boolean isTransactionActive(){
		return false;
	}

	/* (non-Javadoc)
	 * @see org.hibernate.Session#evict(java.lang.Object object)
	 */
	@Override
    public void evict(Object object){

	}

	/* (non-Javadoc)
	 * @see org.hibernate.Session#refresh(java.lang.Object object)
	 */
	@Override
    public void refresh(Object object){

	}

	/* (non-Javadoc)
	 * @see org.hibernate.Session#clear()
	 */
	@Override
    public void clear(){

	}

	/**
	 * Commit the running transaction.
	 */
	@Override
    public void commit(){

	}

	/**
	 * Commit the running transaction but optionally start a
	 * new one right away.
	 *
	 * @param restartTransaction whether to start a new transaction
	 */
	@Override
    public TransactionStatus commit(boolean restartTransaction){
		return null;
	}

	/**
	 * @return the session associated with this conversation manager
	 */
	@Override
    public Session getSession() {
		return null;
	}

	/**
	 * @return the session factory that is bound to this conversation manager
	 */
	@Override
    public SessionFactory getSessionFactory() {
		return null;
	}

	@Override
    public void delete(Object object){

	}

	/**
	 * Facades Session.lock()
	 */
	@Override
    public void lock(Object persistentObject, LockMode lockMode) {

	}

	@Override
    public void lock(String entityName, Object persistentObject, LockMode lockMode){

	}

	/**
	 * @return the definition
	 */
	@Override
    public TransactionDefinition getDefinition() {
		return null;
	}

	/**
	 * @param definition the definition to set
	 */
	@Override
    public void setDefinition(TransactionDefinition definition) {

	}

	/**
	 * Register to get updated after any interaction with the datastore
	 */
	@Override
    public void registerForDataStoreChanges(IConversationEnabled observer) {

	}

	/**
	 * Register to get updated after any interaction with the datastore
	 */
	@Override
    public void unregisterForDataStoreChanges(IConversationEnabled observer) {

	}

	/**
	 * Free resources bound to this conversationHolder
	 */
	@Override
    public void close(){

	}

	@Override
    public boolean isClosed(){
		return true;
	}

}
