// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 16.02.2010
 * @version 1.0
 */
public class ResultSetPartitioner<STATE extends IPartitionedState> {
	private static final Logger logger = Logger.getLogger(ResultSetPartitioner.class);
	private PartitionerProfiler profiler = new PartitionerProfiler();

//************************* STATIC ***************************************************/
	
	public static ResultSetPartitioner NewInstance(Source source, String strIdQuery, String strRecordQuery, int partitionSize) throws SQLException{
		ResultSetPartitioner resultSetPartitioner = new ResultSetPartitioner(source, strIdQuery, strRecordQuery, partitionSize);
		return resultSetPartitioner;
	}
	
//	public static ResultSetPartitioner NewInstance(ResultSet resultSet, int partitionSize) throws SQLException{
//		ResultSetPartitioner resultSetPartitioner = new ResultSetPartitioner(resultSet, partitionSize);
//		return resultSetPartitioner;
//	}

//*********************** VARIABLES *************************************************/
	
	/**
	 * The database
	 */
	private Source source;
	
	/**
	 * The result set containing all records and at least the ids as a field. This result set
	 * will be used for partitioning
	 */
	private ResultSet idResultSet;
	
	/**
	 * A template for a SQL Query returning all records and all values needed for a partition
	 * to be handled. The 'where' condition is filled by replacing the templates '@IdList' token
	 */
	private String strRecordQueryTemplate;
	
	/**
	 * The resultset returned for the strRecordQueryTemplate 
	 */
	private ResultSet partitionResultSet;
	
	/**
	 * A 2-key map holding all related objects needed during the handling of a partition (e.g. when 
	 * creating a taxon partition the map holds all taxon names.
	 * The key is a combination of a namespace and the id in the original source
	 */
	private Map<Object, Map<String, CdmBase>> relatedObjects;
	
	/**
	 * number of records handled in the partition
	 */
	private int partitionSize;
	
	/**
	 * A list of ids handled in this partition 
	 */
	private List<Integer> currentIdList;
	
	/**
	 * counter for the partitions
	 */
	private int currentPartition;
	
	/**
	 * number of records in the current partition
	 */
	private int rowsInCurrentPartition;
	
	private TransactionStatus txStatus;
	
//*********************** CONSTRUCTOR *************************************************/

	private ResultSetPartitioner(Source source, String strIdQuery, String strRecordQuery, int partitionSize) throws SQLException{
		ResultSet idResultSet = source.getResultSet(strIdQuery);
//		if (! idResultSet.isBeforeFirst()){
//			idResultSet.beforeFirst();
//		}
		this.source = source;
		this.idResultSet = idResultSet;
		this.strRecordQueryTemplate = strRecordQuery;
		this.partitionSize = partitionSize;
	}
	
//************************ METHODS ****************************************************/
	

	/**
	 * @param partitionedIO
	 */
	public void doPartition(IPartitionedIO partitionedIO, STATE state) {
		try{
			profiler.startTx();
			TransactionStatus txStatus = getTransaction(partitionSize, partitionedIO);
			
			profiler.startRs();
			ResultSet rs = makePartitionResultSet();

			profiler.startRelObjects();
			this.relatedObjects = partitionedIO.getRelatedObjectsForPartition(rs);
			state.setRelatedObjects(relatedObjects);
			
			profiler.startRs2();
			partitionResultSet = makePartitionResultSet();
			
			profiler.startDoPartition(); 
			partitionedIO.doPartition(this, state);
			
			profiler.startDoCommit();
			partitionedIO.commitTransaction(txStatus);
			
			profiler.end();
			
			
			logger.info("Saved " + getCurrentNumberOfRows() + " " + partitionedIO.getPluralString() );
			profiler.print();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	
	public void startDoSave(){
		profiler.startDoSave();
	}
	
	/**
	 * Increases the partition counter and generates the new <code>currentIdList</code>
	 * @return
	 * @throws SQLException
	 */
	public boolean nextPartition() throws SQLException{
		boolean result = false;
		currentPartition++;
		currentIdList = new ArrayList<Integer>();
		int i = 0;
		for (i = 0; i < partitionSize; i++){
			if (idResultSet.next() == false){
				break; 
			}
			int nextId = idResultSet.getInt(1);
			currentIdList.add(nextId);
			result = true;
		}
		rowsInCurrentPartition = i;
		
		return result;
	}



	/**
	 * Returns the underlying resultSet holding all records needed to handle the partition.<BR>
	 * @return
	 */
	public ResultSet getResultSet(){
		return partitionResultSet;
	}

	
	
	/**
	 * Computes the value result set needed to handle a partition by using the <code>currentIdList</code>
	 * created during {@link #nextPartition}
	 * @return
	 */
	private ResultSet makePartitionResultSet(){
		String strIdList = "";
		for (Integer id: currentIdList){
			strIdList = CdmUtils.concat(",", strIdList, String.valueOf(id));
		}
		String strRecordQuery = strRecordQueryTemplate.replace(IPartitionedIO.ID_LIST_TOKEN, strIdList);
		ResultSet resultSet = source.getResultSet(strRecordQuery);
		return resultSet;
	}
	
	public Map<String, ? extends CdmBase> getObjectMap(Object key){
		Map<String, ? extends CdmBase> objectMap = this.relatedObjects.get(key);
		return objectMap;
	}
	
	/**
	 * 
	 */
	private int getCurrentNumberOfRows() {
		return ((currentPartition - 1) * partitionSize + rowsInCurrentPartition);
	}
	

	/**
	 * @param recordsPerTransaction
	 * @param partitionedIO 
	 * @param i
	 */
	protected TransactionStatus getTransaction(int recordsPerTransaction, IPartitionedIO partitionedIO) {
		//if (loopNeedsHandling (i, recordsPerTransaction) || txStatus == null) {
			txStatus = partitionedIO.startTransaction();
			if(logger.isInfoEnabled()) {
				logger.debug("currentPartitionNumber = " + currentPartition + " - Transaction started"); 
			}
		//}
		return txStatus;
	}
	
// ************************** Not Needed ?? **************************************************
	
//	protected void doLogPerLoop(int recordsPerLog, String pluralString){
//		int count = getAbsoluteRow() - 1;
//		if ((count % recordsPerLog ) == 0 && count!= 0 ){ 
//			logger.info(pluralString + " handled: " + (count));
//		}
//	}
//
//	

	

//	public boolean nextRow() throws SQLException{
//		if (currentRowInPartition >= partitionSize ){
//			return false;
//		}else{
//			currentRowInPartition++;
//			return resultSet.next();
//		}
//	}
//	
	
	
	
}
