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
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v25_30.TermUpdater_25_30;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.Rank;

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

	
		// [unranked]
		UUID uuidTerm = UUID.fromString("a965befb-70a9-4747-a18f-624456c65223");
		String description = "Unranked Rank: The name on purpose has no rank";
		String label = "Unranked";
		String abbrev = "[unranked]";
		String dtype = Rank.class.getSimpleName();
		boolean isOrdered = true;
		UUID uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
		UUID uuidAfterTerm = UUID.fromString("5c4d6755-2cf6-44ca-9220-cccf8881700b");
		UUID uuidLang = Language.uuidEnglish;
		list.add( SingleTermUpdater.NewInstance("Add 'unranked' rank to ranks", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

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
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_25_30.NewInstance();
	}

}
