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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 16.02.2010
 */
public class ResultSetPartitioner<STATE extends IPartitionedState> {
	private static final Logger logger = Logger.getLogger(ResultSetPartitioner.class);
	private final PartitionerProfiler profiler = new PartitionerProfiler();

//************************* STATIC ***************************************************/

	public static <T extends IPartitionedState> ResultSetPartitioner<T> NewInstance(Source source, String strIdQuery, String strRecordQuery, int partitionSize) throws SQLException{
		ResultSetPartitioner<T> resultSetPartitioner
		        = new ResultSetPartitioner<>(source, strIdQuery, strRecordQuery, partitionSize);
		return resultSetPartitioner;
	}

//*********************** VARIABLES *************************************************/

	/**
	 * The database
	 */
	private final Source source;

	/**
	 * The result set containing all records and at least the ids as a field. This result set
	 * will be used for partitioning
	 */
	private final ResultSet idResultSet;

	/**
	 * A template for a SQL Query returning all records and all values needed for a partition
	 * to be handled. The 'where' condition is filled by replacing the templates '@IdList' token
	 */
	private final String strRecordQueryTemplate;

	/**
	 * The resultset returned for the strRecordQueryTemplate
	 */
	private ResultSet partitionResultSet;

	/**
	 * A 2-key map holding all related objects needed during the handling of a partition (e.g. when
	 * creating a taxon partition the map holds all taxon names.
	 * The key is a combination of a namespace and the id in the original source
	 */
	private Map<Object, Map<String, ? extends CdmBase>> relatedObjects;

	/**
	 * Number of records handled in the partition
	 */
	private final int partitionSize;

	/**
	 * Lists of ids handled in this partition (must be an array of lists because sometimes
	 * we have non-single keys
	 */
	private List<String>[] currentIdLists;

	/**
	 * The sql type of the id columns.
	 * @see Types
	 */
	private int[] currentIdListType;

	private String lastPartitionHighestIDs;

	boolean nextAlreadyCalled = false;

	/**
	 * counter for the partitions
	 */
	private int currentPartition;


	/**
	 * counter for all records
	 */
	private int allRecords;

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
	 * Import the whole partition of an input stream by starting a transaction, getting the related objects
	 * stored in the destination, invoke the IOs partition handling and commit the transaction
	 * @param partitionedIO
	 */
	public void doPartition(IPartitionedIO<STATE> partitionedIO, STATE state) {
		int i = 0;
	    try{
			profiler.startTx();
			TransactionStatus txStatus = getTransaction(partitionSize, partitionedIO);

			i = 1;
			state.makeTransactionalSourceReference(partitionedIO.getReferenceService());

			profiler.startRs();
			ResultSet rs = makePartitionResultSet();
            i = 2;
			profiler.startRelObjects();
			this.relatedObjects = partitionedIO.getRelatedObjectsForPartition(rs, state);
            i = 3;
            state.setRelatedObjects(relatedObjects);
            i = 4;
			profiler.startRs2();
			partitionResultSet = makePartitionResultSet();
            i = 5;
			profiler.startDoPartition();
			partitionedIO.doPartition(this, state);
            i = 6;
			profiler.startDoCommit();
			partitionedIO.commitTransaction(txStatus);
			state.resetTransactionalSourceReference();
            i = 7;
			profiler.end();
			state.setRelatedObjects(null);
            i = 8;
			logger.info("Saved " + getCurrentNumberOfRows() + " " + partitionedIO.getPluralString() );
			profiler.print();
		}catch(Exception e){
			String message = "Exception (%s) occurred at position " + i + " while handling import partition for %s.";
			message = String.format(message, e.getMessage(), partitionedIO.getPluralString());
			e.printStackTrace();
			throw new RuntimeException(message, e);
		}
	}


	public void startDoSave(){
		profiler.startDoSave();
	}

	/**
	 * Increases the partition counter and generates the <code>currentIdLists</code> for this partition
	 * @return
	 * @throws SQLException
	 */
    public boolean nextPartition() throws SQLException{
		boolean result = false;
		ResultSetMetaData metaData = idResultSet.getMetaData();
		int nOfIdColumns = metaData.getColumnCount();
		currentPartition++;

		currentIdLists = new List[nOfIdColumns];
		currentIdListType = new int[nOfIdColumns];

		for (int col = 0; col< currentIdLists.length; col++){
			currentIdLists[col] = new ArrayList<>();
			currentIdListType[col] = metaData.getColumnType(col + 1);
		}

		int i = 0;
		//for each record
		for (i = 0; i < partitionSize || !firstIdIsNew(); i++){
			if( !nextAlreadyCalled){
			    if (!idResultSet.next()){
			        break;
			    }
			}else if (idResultSet.isAfterLast()){
			    break;
			}
			nextAlreadyCalled = false;
		    allRecords++;
			//for each column
			for (int colIndex = 0; colIndex < nOfIdColumns; colIndex++){

			    String strNextId = String.valueOf(idResultSet.getObject(colIndex + 1));
				List<String> currentIdList = currentIdLists[colIndex];
				currentIdList.add(strNextId);
				if(colIndex == 0){
				    lastPartitionHighestIDs = strNextId;
				}
			}
			result = true; //true if at least one record was read
		}
		rowsInCurrentPartition = i;

		return result;
	}

    /**
     * Checks if the current partition may have duplicates that were handled already.
     * This may happen if the result set has >1 columns and if the first column does
     * not change it value with the first record but with a later record and the
     * following columns jump back with there values.
     * E.g. first result set contains (x1=1,x2=3) and with the second result set
     * we ask for x1 in (1,2) x2 in (1-5, 10-15) where the 1-5 comes from x1=2.
     * This should not happen and therefore we increase the partition a bit such that
     * the first column always changes its value and therefore it is guranteed that
     * such duplicates will never exist.
     * The reason for this problem is, that we do not create tuples in the WHERE clause
     * of getIdRecord but we handle the range for each column separately. This is not correct
     * but handling of tuples is more difficult in SQL.
     * As multiple columns do not appear so often this workaround seems acceptable.
     * @return
     * @throws SQLException
     */
    private boolean firstIdIsNew() throws SQLException {
        String last = lastPartitionHighestIDs;
        if (!idResultSet.next()){
            return true;
        }
        nextAlreadyCalled = true;
        String current = String.valueOf(idResultSet.getObject(1));
        return !current.equals(last);
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
	 * @return ResultSet
	 */
	private ResultSet makePartitionResultSet(){
		int nColumns = currentIdLists.length;
		String[] strIdLists = new String[nColumns];

		String strRecordQuery = strRecordQueryTemplate;
		for (int col = 0; col < nColumns; col++){
			for (String id: currentIdLists[col]){
				id = addApostropheIfNeeded(id, currentIdListType[col]);
				strIdLists[col] = CdmUtils.concat(",", strIdLists[col], id);
			}
			strRecordQuery = strRecordQuery.replaceFirst(IPartitionedIO.ID_LIST_TOKEN, strIdLists[col]);
		}

		ResultSet resultSet = ResultSetProxy.NewInstance(source.getResultSet(strRecordQuery));

		return resultSet;
	}

	private String addApostropheIfNeeded(String id, int sqlType) {
		String result = id;
		if (isStringType(sqlType)){
			result = "'" + id + "'";
		}
		return result;
	}

	private boolean isStringType(int sqlType) {
		if(sqlType == Types.INTEGER){
			return false;  //standard case
		}else if (sqlType == Types.CHAR || sqlType == Types.CLOB || sqlType == Types.NVARCHAR ||
				sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR || sqlType == Types.NCHAR ||
				sqlType == Types.LONGNVARCHAR || sqlType == Types.NCLOB){
			return true;
		}else{
			return false;
		}
	}

	public Map<String, ? extends CdmBase> getObjectMap(Object key){
		Map<String, ? extends CdmBase> objectMap = this.relatedObjects.get(key);
		return objectMap;
	}

	/**
	 *
	 */
	private int getCurrentNumberOfRows() {
	    return allRecords;
//		return ((currentPartition - 1) * partitionSize + rowsInCurrentPartition);
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
