// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Moves terms from one vocabulary to another.
 * TODO does not yet check all DefinedTermBase_XXX tables except for representations.
 * TODO Does also not handle AUD tables
 * TODO Does not handle orderindex
 * 
 * @author a.mueller
 * @date 06.09.2013
 *
 */
public class TermMover extends SchemaUpdaterStepBase<TermMover> implements ITermUpdaterStep{
	private static final Logger logger = Logger.getLogger(TermMover.class);
	
	public static final TermMover NewInstance(String stepName, UUID newVocabulary, String uuidTerm){
		List<String> terms = new ArrayList<String>();
		terms.add(uuidTerm);
		return new TermMover(stepName, newVocabulary, terms);	
	}
	

	public static final TermMover NewInstance(String stepName, UUID newVocabulary, List<String> terms){
		return new TermMover(stepName, newVocabulary, terms);	
	}
	
	
	private String uuidNewVocabulary ;
	private List<String> termUuids = new ArrayList<String>();
	

	private TermMover(String stepName, UUID newVocabulary, List<String> terms) {
		super(stepName);
		this.uuidNewVocabulary = newVocabulary.toString();
		this.termUuids = terms; 
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException{
 		//get new vocabulary id
		String sql = " SELECT id FROM TermVocabulary WHERE uuid = '%s'";
		Integer id = (Integer)datasource.getSingleValue(String.format(sql, this.uuidNewVocabulary));
		if (id == null || id == 0){
			String messageString = "New vocabulary ("+uuidNewVocabulary+") does not exist. Can't move terms";
			monitor.warning(messageString);
			logger.warn(messageString);
			return null;
		}
		
		//check if in use
		for (String uuid : this.termUuids){
			sql = " UPDATE DefinedTermBase SET vocabulary_id = %d WHERE uuid = '%s' ";
			sql = String.format(sql, id, uuid);
			datasource.executeUpdate(sql);
		}
		
		return 0;
	}



	public TermMover addTermUuid(UUID uuid){
		this.termUuids.add(uuid.toString());
		return this;
	}
	


}
