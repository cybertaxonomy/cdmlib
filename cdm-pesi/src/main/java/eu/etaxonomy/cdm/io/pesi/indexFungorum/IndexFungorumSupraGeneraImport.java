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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * @author a.mueller
 * @created 27.02.2012
 */
@Component
public class IndexFungorumSupraGeneraImport  extends IndexFungorumImportBase {
	private static final Logger logger = Logger.getLogger(IndexFungorumSupraGeneraImport.class);
	
	private static final String pluralString = "Supragenera";
	private static final String dbTableName = "tblSupragenericNames";

	public IndexFungorumSupraGeneraImport(){
		super(pluralString, dbTableName, null);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(IndexFungorumImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM [tblSupragenericNames] " +
//			" WHERE ( dr.id IN (" + ID_LIST_TOKEN + ") )";
			"";
		return strRecordQuery;
	}

	
	@Override
	protected void doInvoke(IndexFungorumImportState state) {
		
		
		//handle source reference first
		Reference sourceReference = state.getConfig().getSourceReference();
		getReferenceService().save(sourceReference);
		
		//query
		String sql = getRecordQuery(state.getConfig());
		ResultSet rs = state.getConfig().getSource().getResultSet(sql);
		
		//transaction and related objects
		TransactionStatus tx = startTransaction();
		state.setRelatedObjects((Map)getRelatedObjectsForPartition(null));
		sourceReference = state.getRelatedObject(NAMESPACE_REFERENCE, SOURCE_REFERENCE, Reference.class);
		
		try {
			while (rs.next()){

				//TODO
				//DisplayName, NomRefCache

				Integer id = (Integer)rs.getObject("RECORD_NUMBER");
				
				String supragenericNames = rs.getString("SupragenericNames");
				String preferredName = rs.getString("PreferredName");
				Integer rankFk = rs.getInt("PESI_RankFk");
				
				//name
				Rank rank = state.getTransformer().getRankByKey(String.valueOf(rankFk));
				NonViralName<?> name = BotanicalName.NewInstance(rank);
				name.setGenusOrUninomial(supragenericNames);
				if (preferredName != null && !preferredName.equals(supragenericNames)){
					logger.warn("Suprageneric names and preferredName is not equal. This case is not yet handled by IF import. I take SupragenericNames for import. RECORD_NUMBER" + CdmUtils.Nz(id));
				}
				
				//taxon
				Taxon taxon = Taxon.NewInstance(name, sourceReference);
				//author + nom.ref.
				makeAuthorAndPublication(state, rs, name);
				//source
				makeSource(state, taxon, id, NAMESPACE_SUPRAGENERIC_NAMES );
				
				getTaxonService().saveOrUpdate(taxon);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			tx.setRollbackOnly();
			state.setSuccess(false);
		}
		commitTransaction(tx);
		return;
		
	}


	private Taxon makeTaxon(IndexFungorumImportState state, String uninomial, Rank rank) {
		NonViralName<?> name = BotanicalName.NewInstance(rank);
		name.setGenusOrUninomial(uninomial);
		return Taxon.NewInstance(name, state.getConfig().getSourceReference());
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		HashMap<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String,? extends CdmBase>>();  //not needed here
		
		//sourceReference
		Reference<?> sourceReference = getReferenceService().find(PesiTransformer.uuidSourceRefIndexFungorum);
		Map<String, Reference> referenceMap = new HashMap<String, Reference>();
		referenceMap.put(SOURCE_REFERENCE, sourceReference);
		result.put(NAMESPACE_REFERENCE, referenceMap);

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
		return ! state.getConfig().isDoTaxa();
	}





}
