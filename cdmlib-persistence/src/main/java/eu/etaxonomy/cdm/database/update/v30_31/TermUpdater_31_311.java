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
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_31_311 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_31_311.class);
	
	public static final String startTermVersion = "3.0.1.0.201012150000";
	private static final String endTermVersion = "3.0.1.1.201101310000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_31_311 NewInstance(){
		return new TermUpdater_31_311(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_31_311(String startTermVersion, String endTermVersion) {
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

		String sql = "UPDATE @@Representation@@ SET text = 'Unranked Infrageneric Rank: The infrageneric name on purpose has no rank', label = 'Unranked (infrageneric)', abbreviatedlabel = '[infragen.]'" +
		" WHERE abbreviatedlabel = 't.infgen.'";
		stepName = "Update unranked infrageneric representation";
		SimpleSchemaUpdaterStep infraGenStep = SimpleSchemaUpdaterStep.NewInstance(stepName, sql, 99);
		list.add(infraGenStep);

		sql = "UPDATE @@DefinedTermBase@@ SET titleCache = 'Unranked (infrageneric)' " +
			" WHERE titleCache  = 'Infrageneric Taxon'";
		stepName = "Update unranked infrageneric title cache";
		SimpleSchemaUpdaterStep infraGenTitleStep = SimpleSchemaUpdaterStep.NewInstance(stepName, sql, 99);
		list.add(infraGenTitleStep);

		
		
		sql = "UPDATE @@Representation@@ SET text = 'Unranked Infraspecific Rank: The infraspecific name on purpose has no rank', label = 'Unranked (infraspecific)', abbreviatedlabel = '[infraspec.]' " +
		" WHERE abbreviatedlabel = 't.infr.'";
		stepName = "Update unranked infraspecific representation";
		SimpleSchemaUpdaterStep infraSpecStep = SimpleSchemaUpdaterStep.NewInstance(stepName, sql, 99);
		list.add(infraSpecStep);

		sql = "UPDATE @@DefinedTermBase@@ SET titleCache = 'Unranked (infraspecific)' " +
			" WHERE titleCache  = 'Infraspecific Taxon'";
		stepName = "Update unranked infraspecific title cache";
		SimpleSchemaUpdaterStep infraSpecTitleStep = SimpleSchemaUpdaterStep.NewInstance(stepName, sql, 99);
		list.add(infraSpecTitleStep);


		
		return list;
	}

	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_311_312.NewInstance();
	}

	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_30_31.NewInstance();
	}

}
