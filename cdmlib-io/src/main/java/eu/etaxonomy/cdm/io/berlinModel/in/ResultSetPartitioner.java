// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 16.02.2010
 * @version 1.0
 */
public class ResultSetPartitioner {
	private static final Logger logger = Logger.getLogger(ResultSetPartitioner.class);

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
	
	private int currentRowInPartition;
	
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
	public void doPartition(IPartitionedIO partitionedIO, BerlinModelImportState state) {
		TransactionStatus txStatus = getTransaction(partitionSize, partitionedIO);
		ResultSet rs = getPartitionResultSet();
		this.relatedObjects = partitionedIO.getRelatedObjectsForPartition(rs);
		partitionResultSet = getPartitionResultSet();
		partitionedIO.doPartition(this, state);
		partitionedIO.commitTransaction(txStatus);
	}
	
	
	public boolean nextPartition() throws SQLException{
		currentPartition++;
		if (idResultSet.isAfterLast()){
			return false;
		}
		currentIdList = new ArrayList<Integer>();
		for (int i = 0; i < partitionSize; i++){
			if (idResultSet.next() == false){
				break; 
			}
			int nextId = idResultSet.getInt(1);
			currentIdList.add(nextId);
		}
		currentRowInPartition = 0;
		
		return true;
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
		String strRecordQuery = strRecordQueryTemplate.replace("@IdList", strIdList);
		ResultSet resultSet = source.getResultSet(strRecordQuery);
		return resultSet;
	}
	
	public Map<String, ? extends CdmBase> getObjectMap(Object key){
		Map<String, ? extends CdmBase> objectMap = this.relatedObjects.get(key);
		return objectMap;
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
				logger.info("currentPartitionNumber = " + currentPartition + " - Transaction started"); 
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
//	/**
//	 * 
//	 */
//	private int getAbsoluteRow() {
//		return partitionSize * (currentPartition - 1) + currentRowInPartition;
//	}
	

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
