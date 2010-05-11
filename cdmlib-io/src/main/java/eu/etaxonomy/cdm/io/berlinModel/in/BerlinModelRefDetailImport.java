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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelRefDetailImportValidator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * This class imports all preliminary refdetails as generic references. Non preliminary 
 * refdetails are imported as microcitation together with the record using the refdetail
 * and therefore left out here.
 *  
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelRefDetailImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelRefDetailImport.class);

	public static final String NOM_REFDETAIL_NAMESPACE = "NomRefDetail";
	public static final String BIBLIO_REFDETAIL_NAMESPACE = "BiblioRefDetail";
	ReferenceFactory refFactory;
	
	
	
	private int modCount = 1000;
	private static final String pluralString = "ref-details";
	private static final String dbTableName = "RefDetail";

	
	public BerlinModelRefDetailImport(){
		super();
	}
	
	
	//type to count the references nomReferences that have been created and saved
	private class RefCounter{
		RefCounter() {nomRefCount = 0; biblioRefCount = 0;};
		int nomRefCount;
		int biblioRefCount;
		public String toString(){return String.valueOf(nomRefCount) + "," +String.valueOf(biblioRefCount);};
	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT RefDetail.RefDetailId " +
        	" FROM RefDetail " +
        	" WHERE (RefDetail.PreliminaryFlag = 1)";
		return strQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT RefDetail.*, Reference.RefYear " +
            " FROM RefDetail " +
            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
            " WHERE (RefDetail.refDetailId IN (" + ID_LIST_TOKEN + ")) AND " + 
            	" (RefDetail.PreliminaryFlag = 1)";
		return strQuery;  
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;
		logger.info("start make " + getPluralString() + " ...");
		
		BerlinModelImportConfigurator config = state.getConfig(); 
		Map<Integer, ReferenceBase> biblioRefDetailsToSave = new HashMap<Integer, ReferenceBase>();
		Map<Integer, ReferenceBase> nomRefDetailsToSave =  new HashMap<Integer, ReferenceBase>();
		
		ResultSet rs = partitioner.getResultSet();
		
		try {
			int i = 0;
			RefCounter refCounter  = new RefCounter();
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RefDetails handled: " + (i-1) );}
				int refDetailId = rs.getInt("refDetailId"); 
				String refYear = rs.getString("RefYear"); 
				
				//nomRef
				String fullNomRefCache = rs.getString("fullNomRefCache"); 
				if ( CdmUtils.isNotEmpty(fullNomRefCache) ){
					ReferenceBase genericReference = refFactory.newGeneric();
					genericReference.setTitleCache(fullNomRefCache, true);
					nomRefDetailsToSave.put(refDetailId, genericReference);
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					//refId, created, notes
					doIdCreatedUpdatedNotes(state, genericReference, rs, refDetailId, NOM_REFDETAIL_NAMESPACE );						
					refCounter.nomRefCount++;
				}	
				
				//biblioRef
				String fullRefCache = rs.getString("fullRefCache"); 
				if ( CdmUtils.isNotEmpty(fullRefCache) && ! fullRefCache.equals(fullNomRefCache)){
					ReferenceBase genericReference = refFactory.newGeneric();
					genericReference.setTitleCache(fullRefCache, true);
					biblioRefDetailsToSave.put(refDetailId, genericReference);
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					//refId, created, notes
					doIdCreatedUpdatedNotes(state, genericReference, rs, refDetailId, BIBLIO_REFDETAIL_NAMESPACE );						
					refCounter.biblioRefCount++;
				}
			}
			//save and store in map
			logger.info("Save nomenclatural preliminary references (" + refCounter.nomRefCount + ")");
			partitioner.startDoSave();
			Collection<ReferenceBase> col = nomRefDetailsToSave.values();
			getReferenceService().save(col);
			logger.info("Save bibliographical preliminary references (" + refCounter.biblioRefCount +")");
			getReferenceService().save(biblioRefDetailsToSave.values());
			
			//TODO
			//SecondarySources
			//IdInSource

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		return success;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		//no related objects needed 

		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelRefDetailImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		DO_REFERENCES doReference = state.getConfig().getDoReferences();
		return (doReference == IImportConfigurator.DO_REFERENCES.NONE || doReference == IImportConfigurator.DO_REFERENCES.CONCEPT_REFERENCES);
	}

}
