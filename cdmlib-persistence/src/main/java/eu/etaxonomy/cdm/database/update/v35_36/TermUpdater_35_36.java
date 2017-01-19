/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v35_36;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v34_35.TermUpdater_34_35;
import eu.etaxonomy.cdm.database.update.v36_40.TermUpdater_36_40;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_35_36 extends TermUpdaterBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_35_36.class);

	public static final String endTermVersion = "3.6.0.0.201527040000";
	private static final String startTermVersion = "3.5.0.0.201531030000";

// *************************** FACTORY **************************************/

	public static TermUpdater_35_36 NewInstance(){
		return new TermUpdater_35_36(startTermVersion, endTermVersion);
	}

// *************************** CONSTRUCTOR ***********************************/

	protected TermUpdater_35_36(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}


	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

//		// proles, #2793
//		UUID uuidTerm = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
//		String description = "Rank ''Proles''. Note: This rank is not compliant with the current nomenclatural codes";
//		String label = "Proles";
//		String abbrev = "prol.";
//		String dtype = Rank.class.getSimpleName();
//		boolean isOrdered = true;
//		UUID uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
//		UUID uuidAfterTerm = UUID.fromString("bff22f84-553a-4429-a4e7-c4b3796c3a18");
//		UUID uuidLang = Language.uuidEnglish;
//		RankClass rankClass = RankClass.Infraspecific;
//		String stepName = "Add 'proles' rank to ranks";
//		TermType termType = TermType.Rank;
//		list.add( SingleTermUpdater.NewInstance(stepName, termType, uuidTerm, abbrev, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm).setRankClass(rankClass));

		//there are some more new vocabularies, but we trust that the term initializer will
		//initialize and persist them correctly

		return list;
	}

	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_36_40.NewInstance();
	}

	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_34_35.NewInstance();
	}

}
