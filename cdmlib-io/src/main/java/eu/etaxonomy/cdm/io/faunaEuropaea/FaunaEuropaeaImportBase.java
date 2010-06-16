/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author a.babadshanjan
 * @created 11.05.2009
 * @version 1.0
 */
public abstract class FaunaEuropaeaImportBase extends CdmImportBase<FaunaEuropaeaImportConfigurator, FaunaEuropaeaImportState> 
implements ICdmImport<FaunaEuropaeaImportConfigurator,FaunaEuropaeaImportState> {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaImportBase.class);
	
//	/* Max number of taxa to retrieve (for test purposes) */
//	protected static final int maxTaxa = 1000;
//	/* Max number of taxa to be saved with one service call */
//	protected int limit = 20000; // TODO: Make configurable
//	/* Interval for progress info message when retrieving taxa */
//	protected static final int modCount = 10000;
//	/* Highest taxon index in the FauEu database */
//	protected int highestTaxonIndex = 0;
	
	protected boolean resultSetHasColumn(ResultSet rs, String columnName){
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			for (int i = 0; i < metaData.getColumnCount(); i++){
				if (metaData.getColumnName(i + 1).equalsIgnoreCase(columnName)){
					return true;
				}
			}
			return false;
		} catch (SQLException e) {
            logger.warn("Exception in resultSetHasColumn");
            return false;
		}
	}
	
	protected boolean checkSqlServerColumnExists(Source source, String tableName, String columnName){
		String strQuery = "SELECT  Count(t.id) as n " +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " + 
				" (t.name = '" + tableName + "') AND " + 
				" (c.name = '" + columnName + "')";
		ResultSet rs = source.getResultSet(strQuery) ;		
		int n;
		try {
			rs.next();
			n = rs.getInt("n");
			return n>0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Returns a map that holds all values of a ResultSet. This is needed if a value needs to
	 * be accessed twice
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected Map<String, Object> getValueMap(ResultSet rs) throws SQLException{
		try{
			Map<String, Object> valueMap = new HashMap<String, Object>();
			int colCount = rs.getMetaData().getColumnCount();
			for (int c = 0; c < colCount ; c++){
				Object value = rs.getObject(c+1);
				String label = rs.getMetaData().getColumnLabel(c+1).toLowerCase();
				if (value != null && ! CdmUtils.Nz(value.toString()).trim().equals("")){
					valueMap.put(label, value);
				}
			}
			return valueMap;
		}catch(SQLException e){
			throw e;
		}
	}
	

	/**
	 * @param state
	 * @param sourceRef
	 */
	protected TaxonomicTree getTaxonomicTreeFor(FaunaEuropaeaImportState state, ReferenceBase<?> sourceRef) {
		
		TaxonomicTree tree;
		UUID treeUuid = state.getTreeUuid(sourceRef);
		if (treeUuid == null){
			if(logger.isInfoEnabled()) { logger.info(".. creating new taxonomic tree"); }
			
			TransactionStatus txStatus = startTransaction();
			tree = makeTreeMemSave(state, sourceRef);
			commitTransaction(txStatus);
			
		} else {
			tree = getTaxonTreeService().getTaxonomicTreeByUuid(treeUuid);
		}
		return tree;
	}

}
