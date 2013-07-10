/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.indexFungorum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 27.02.2012
 */
@Component
public class IndexFungorumDistributionImport  extends IndexFungorumImportBase {
	private static final Logger logger = Logger.getLogger(IndexFungorumDistributionImport.class);
	
	private static final String pluralString = "distributions";
	private static final String dbTableName = "[tblPESIfungi]";

	public IndexFungorumDistributionImport(){
		super(pluralString, dbTableName, null);
	}


	
	
	@Override
	protected String getIdQuery() {
		String result = " SELECT PreferredNameIFnumber FROM " + getTableName() +
				" ORDER BY PreferredNameIFnumber ";
		return result;
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(IndexFungorumImportConfigurator config) {
		String strRecordQuery = 
				" SELECT distribution.* " +
				" FROM tblPESIfungi AS distribution  " +
			" WHERE ( distribution.PreferredNameIFnumber  IN (" + ID_LIST_TOKEN + ") )" +
			"";
		return strRecordQuery;
	}

	
	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, IndexFungorumImportState state) {
		boolean success = true;
//		Reference<?> sourceReference = state.getRelatedObject(NAMESPACE_REFERENCE, SOURCE_REFERENCE, Reference.class);
		ResultSet rs = partitioner.getResultSet();
		
		try {
			//column names that do not hold distribution information
			Set<String> excludedColumns = new HashSet<String>();
			excludedColumns.add("PreferredName");
			excludedColumns.add("PreferredNameIFnumber");
			excludedColumns.add("PreferredNameFDCnumber");
			
			PresenceTerm status = PresenceTerm.PRESENT();
			MarkerType noLastActionMarkerType = getNoLastActionMarkerType(state);
			while (rs.next()){

				//get taxon description
				Integer id = rs.getInt("PreferredNameIFnumber");
				Taxon taxon = state.getRelatedObject(NAMESPACE_SPECIES, String.valueOf(id), Taxon.class);
				Reference<?> ref = null;
				TaxonDescription description = getTaxonDescription(taxon, ref, false, true);
				
				//handle single distributions
				int count = rs.getMetaData().getColumnCount();
				for (int i=1; i <= count; i++ ){
					String colName = rs.getMetaData().getColumnName(i);
					//exclude non distribution columns
					if (! excludedColumns.contains(colName)){
						String distributionValue = rs.getString(i);
						if (StringUtils.isNotBlank(distributionValue)){
							//create distribution for existing occurrences
							if (! distributionValue.equals("X")){
								logger.warn("Unexpected distribution value '" + distributionValue + "' for area " + colName);
							}
							NamedArea area = state.getTransformer().getNamedAreaByKey(colName);
							Distribution distribution = Distribution.NewInstance(area, status);
							description.addElement(distribution);
							//no last action
							distribution.addMarker(Marker.NewInstance(noLastActionMarkerType, true));
						}
						
					}
				}
				
				//save
				getTaxonService().saveOrUpdate(taxon);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setSuccess(false);
			success = false;
		}
		return success;
	}

	
	private Taxon getParentTaxon(IndexFungorumImportState state, ResultSet rs) throws SQLException {
		Integer genusId = rs.getInt("PreferredNameFDCnumber");
		
		Taxon taxon = state.getRelatedObject(NAMESPACE_GENERA, String.valueOf(genusId), Taxon.class);
		if (taxon == null){
			logger.warn("Taxon not found for " + genusId);
		}
		return taxon;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "PreferredNameIFnumber" );
			}
			
			//taxon map
			nameSpace = NAMESPACE_SPECIES;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
			//sourceReference
			Reference<?> sourceReference = getReferenceService().find(PesiTransformer.uuidSourceRefIndexFungorum);
			Map<String, Reference> referenceMap = new HashMap<String, Reference>();
			referenceMap.put(SOURCE_REFERENCE, sourceReference);
			result.put(NAMESPACE_REFERENCE, referenceMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IndexFungorumImportState state){
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IndexFungorumImportState state){
		return ! state.getConfig().isDoOccurrence();
	}





}
