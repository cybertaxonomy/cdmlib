// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v30_31;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_313_314 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_313_314.class);
	
	public static final String startTermVersion = "3.0.1.3.201103210000";
	private static final String endTermVersion = "3.0.1.4.201105100000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_313_314 NewInstance(){
		return new TermUpdater_313_314(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_313_314(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}
	
// 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

		
		String description;
		String label;
		String abbrev;
		String dtype;
		boolean isOrdered;
		UUID uuidVocabulary;
		UUID uuidAfterTerm;
		UUID uuidLang;
		String stepName;

		// DO NOT COPY ; THIS IS INCOMPLETE
		//FIXME vocabulary 
		// native: formerly native
		UUID uuidTerm = UUID.fromString("5c397f7b-59ef-4c11-a33c-45691ceda91b");
		UUID oldUuid = UUID.fromString("8ad9e9df-49cd-4b6a-880b-51ec4de4ce32");
		
//		SingleTermRemover termRemover = SingleTermRemover.NewInstance(stepName, uuidTerm, checkUsedQueries);
		
		
		String updateQuery = "UPDATE @@DefinedTermBase@@ " + 
			"SET DTYPE='AbsenceTerm', uuid='"+uuidTerm+"', defaultColor = 'ccb462', orderIndex=6 "+
			"WHERE uuid = '"+oldUuid+"'";
		stepName = "Move 'native: formerly native' to absence terms";
		SimpleSchemaUpdaterStep formerlyUpdater = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, updateQuery, "DefinedTermBase", 99);
		list.add(formerlyUpdater);
	
		//FIXME vocabulary 
		// introduced: formerly introduced
		uuidTerm = UUID.fromString("b74dc30b-ee93-496d-8c00-4d00abae1ec7");
		oldUuid = UUID.fromString("2522c527-e488-45d4-87df-a5a5ef0fdbbd");
		
		updateQuery = "UPDATE @@DefinedTermBase@@ " + 
			"SET DTYPE='AbsenceTerm', uuid='"+uuidTerm+"', defaultColor = 'ccebcc', orderIndex=7 "+
			"WHERE uuid = '"+oldUuid+"'";
		stepName = "Move 'introduced: formerly introduced' to absence terms";
		formerlyUpdater = SimpleSchemaUpdaterStep.NewInstance(stepName, updateQuery, 99);
		list.add(formerlyUpdater);
	
		
		// gazzetteer
		uuidTerm = UUID.fromString("e35f1d1c-9347-4190-bd47-a3b00632fcf3");
		description = "Gazetteer";
		label = "Gazetteer";
		abbrev = "Gazetteer";
		dtype = ReferenceSystem.class.getSimpleName();
		isOrdered = false;
		uuidVocabulary = UUID.fromString("ec6376e5-0c9c-4f5c-848b-b288e6c17a86");
		uuidAfterTerm = null;
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'gazetteer' to reference systems";
		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		
		return list;
	}
	
	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_314_315.NewInstance();
	}

	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_312_313.NewInstance();
	}

}
