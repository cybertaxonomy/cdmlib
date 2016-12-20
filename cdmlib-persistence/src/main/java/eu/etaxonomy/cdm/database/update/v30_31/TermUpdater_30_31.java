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
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v25_30.TermUpdater_25_30;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_30_31 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_30_31.class);
	
	public static final String startTermVersion = "3.0.0.0.201011170000";
	private static final String endTermVersion = "3.0.1.0.201012150000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_30_31 NewInstance(){
		return new TermUpdater_30_31(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_30_31(String startTermVersion, String endTermVersion) {
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

//		// [unranked infragneric]
//		UUID uuidTerm = UUID.fromString("994646a1-c2e7-461d-a1bc-2afd6ea51b40");
//		description = "Unranked Infragneric Rank: The infrageneric name on purpose has no rank";
//		label = "Unranked Infrageneric";
//		abbrev = "[unranked]";
//		dtype = Rank.class.getSimpleName();
//		isOrdered = true;
//		uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
//		uuidAfterTerm = UUID.fromString("41bcc6ac-37d3-4fd4-bb80-3cc5b04298b9");
//		uuidLang = Language.uuidEnglish;
//		stepName = "Add 'unranked infrageneric' rank to ranks";
//		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));
//
//		
//		// [unranked infraspecific]
//		uuidTerm = UUID.fromString("a965befb-70a9-4747-a18f-624456c65223");
//		description = "Unranked Infraspecific Rank: The infraspecific name on purpose has no rank";
//		label = "Unranked Infraspecific";
//		abbrev = "[unranked]";
//		dtype = Rank.class.getSimpleName();
//		isOrdered = true;
//		uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
//		uuidAfterTerm = UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");
//		uuidLang = Language.uuidEnglish;
//		stepName = "Add 'unranked' rank to ranks";
//		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		
		// endemic
		UUID uuidTerm = UUID.fromString("efe95ade-8a6c-4a0e-800e-437c8b50c45e");
		description = "endemic";
		label = "endemic";
		abbrev = "endemic";
		dtype = MarkerType.class.getSimpleName();
		isOrdered = false;
		uuidVocabulary = UUID.fromString("19dffff7-e142-429c-a420-5d28e4ebe305");
		uuidAfterTerm = null;//UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'endemic' rank to ranks";
		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		// status feature
		uuidTerm = UUID.fromString("86d40635-2a63-4ad6-be75-9faa4a6a57fb");
		description = "Status";
		label = "Status";
		abbrev = "Status";
		dtype = Feature.class.getSimpleName();
		isOrdered = false;
		uuidVocabulary = UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8");
		uuidAfterTerm = null;//UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'status' feature to features";
		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		// systematics feature
		uuidTerm = UUID.fromString("bd9aca17-cd0e-4418-a3a1-1a4b80dbc162");
		description = "Systematics";
		label = "Systematics";
		abbrev = "Systematics";
		dtype = Feature.class.getSimpleName();
		isOrdered = false;
		//TODO is this a name feature or a taxon feature
		uuidVocabulary = UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8");
		uuidAfterTerm = null;//UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'systematics' feature to features";
		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		
		//language labels
		LanguageLabelUpdater langLabelUpdater = LanguageLabelUpdater.NewInstance();
		list.add(langLabelUpdater);
		
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_31_311.NewInstance();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_25_30.NewInstance();
	}

}
