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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelRefDetailImportValidator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * This class imports all preliminary refdetails as generic references. Non preliminary 
 * refdetails are imported as microcitation together with the record using the refdetail
 * and therefore left out here.
 *  
 * @author a.mueller
 * @created 20.03.2008
 */
@Component
public class BerlinModelRefDetailImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelRefDetailImport.class);

//	public static final String NOM_REFDETAIL_NAMESPACE = "NomRefDetail";
//	public static final String BIBLIO_REFDETAIL_NAMESPACE = "BiblioRefDetail";
	public static final String REFDETAIL_NAMESPACE = "RefDetail";
		
	private int modCount = 1000;
	private static final String pluralString = "ref-details";
	private static final String dbTableName = "RefDetail";

	
	public BerlinModelRefDetailImport(){
		super(dbTableName, pluralString);
	}
		
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String strQuery = " SELECT RefDetail.RefDetailId " +
        	" FROM RefDetail " +
        	" WHERE (RefDetail.PreliminaryFlag = 1)";
			if (StringUtils.isNotBlank(state.getConfig().getRefDetailFilter())){
				strQuery += " AND " + state.getConfig().getRefDetailFilter();
			}
		
		return strQuery;
	}

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

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;
		logger.info("start make " + getPluralString() + " ...");
		
		BerlinModelImportConfigurator config = state.getConfig(); 
		Map<Integer, Reference> refDetailsToSave = new HashMap<Integer, Reference>();
		
		ResultSet rs = partitioner.getResultSet();
		int refCount = 0; 
		
		try {
			int i = 0;
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RefDetails handled: " + (i-1) );}
				int refDetailId = rs.getInt("refDetailId"); 
				String refYear = rs.getString("RefYear"); 
				
				//nomRef
				String fullNomRefCache = rs.getString("fullNomRefCache"); 
				String fullRefCache = rs.getString("fullRefCache"); 
				
				if ( StringUtils.isNotBlank(fullNomRefCache) || StringUtils.isNotBlank(fullRefCache)  ){
					Reference<?> genericReference = ReferenceFactory.newGeneric();
					
					if (StringUtils.isNotBlank(fullNomRefCache)){
						genericReference.setAbbrevTitleCache(fullNomRefCache, true);
					}
					if ( StringUtils.isNotBlank(fullRefCache) ){
						genericReference.setTitleCache(fullRefCache, true);
					}
					
					refDetailsToSave.put(refDetailId, genericReference);
					//year
					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
					//refId, created, notes
					doIdCreatedUpdatedNotes(state, genericReference, rs, refDetailId, REFDETAIL_NAMESPACE );						
					refCount++;
				}	
//				xx;
//				//biblioRef
//				String fullRefCache = rs.getString("fullRefCache"); 
//				if ( StringUtils.isNotBlank(fullRefCache) && ! fullRefCache.equals(fullNomRefCache)){
//					Reference<?> genericReference = ReferenceFactory.newGeneric();
//					genericReference.setTitleCache(fullRefCache, true);
//					biblioRefDetailsToSave.put(refDetailId, genericReference);
//					//year
//					genericReference.setDatePublished(ImportHelper.getDatePublished(refYear)); 
//					//refId, created, notes
//					doIdCreatedUpdatedNotes(state, genericReference, rs, refDetailId, BIBLIO_REFDETAIL_NAMESPACE );						
//					refCounter.biblioRefCount++;
//				}
			}
			//save and store in map
			logger.info("Save preliminary (RefDetail) references  (" + refCount + ")");
			partitioner.startDoSave();
			Collection<Reference> col = refDetailsToSave.values();
			getReferenceService().save(col);
			
			//TODO
			//SecondarySources
			//IdInSource

		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
		return success;
	}

	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		//no related objects needed 

		return result;
	}
	
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelRefDetailImportValidator();
		return validator.validate(state);
	}

	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		DO_REFERENCES doReference = state.getConfig().getDoReferences();
		return (doReference == IImportConfigurator.DO_REFERENCES.NONE || doReference == IImportConfigurator.DO_REFERENCES.CONCEPT_REFERENCES);
	}

}
