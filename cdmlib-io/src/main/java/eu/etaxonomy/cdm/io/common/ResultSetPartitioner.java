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
public class ResultSetPartitioner<STATE extends ImportStateBase> {
	private static final Logger logger = Logger.getLogger(ResultSetPartitioner.class);
	private PartitionerProfiler duration = new PartitionerProfiler();

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
	
	private Source source;
	
	private ResultSet idResultSet;
	
	private String strRecordQueryTemplate;
	
	private ResultSet partitionResultSet;
	
	private Map<Object, Map<String, ? extends CdmBase>> relatedObjects;
	
	private int partitionSize;
	
	private List<Integer> currentIdList;
	
	private int currentPartition;
	
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
			duration.startTx();
			TransactionStatus txStatus = getTransaction(partitionSize, partitionedIO);
			
			duration.startRs();
			ResultSet rs = getPartitionResultSet();

			duration.startRelObjects();
			this.relatedObjects = partitionedIO.getRelatedObjectsForPartition(rs);
			
			duration.startRs2();
			partitionResultSet = getPartitionResultSet();
			
			duration.startDoPartition(); 
			partitionedIO.doPartition(this, state);
			
			duration.startDoCommit();
			partitionedIO.commitTransaction(txStatus);
			
			duration.end();
			
			
			logger.info("Saved " + getCurrentNumberOfRows() + " " + partitionedIO.getPluralString() );
			duration.print();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	
	public void startDoSave(){
		duration.startDoSave();
	}
	
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
	 * Returns the underlying resultSet.<BR>
	 * @return
	 */
	public ResultSet getResultSet(){
		return partitionResultSet;
	}

	
	
	private ResultSet getPartitionResultSet(){
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
