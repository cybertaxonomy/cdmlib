// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v31_33;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.v30_31.TermUpdater_314_315;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_31_33 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_31_33.class);
	
	public static final String startTermVersion = "3.0.1.4.201105100000";
	private static final String endTermVersion = "3.0.1.5.201109280000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_31_33 NewInstance(){
		return new TermUpdater_31_33(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_31_33(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}
	
// 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

		// proles, #2793
		UUID uuidTerm = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
		String description = "Rank 'Proles'. Note: This rank is not compliant with the current nomenclatural codes";
		String label = "Proles";
		String abbrev = "prol.";
		String dtype = Rank.class.getSimpleName();
		boolean isOrdered = true;
		UUID uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
		UUID uuidAfterTerm = UUID.fromString("bff22f84-553a-4429-a4e7-c4b3796c3a18");
		UUID uuidLang = Language.uuidEnglish;
		RankClass rankClass = RankClass.Infraspecific;
		String stepName = "Add 'proles' rank to ranks";
		TermType termType = TermType.Rank;
		list.add( SingleTermUpdater.NewInstance(stepName, termType, uuidTerm, abbrev, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm).setRankClass(rankClass));

		// race, #2793
		uuidTerm = UUID.fromString("196dee39-cfd8-4460-8bf0-88b83da27f62");
		description = "Rank 'Race'. Note: This rank is not compliant with the current nomenclatural codes";
		label = "Race";
		abbrev = "race";
		dtype = Rank.class.getSimpleName();
		isOrdered = true;
		uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
		uuidAfterTerm = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
		uuidLang = Language.uuidEnglish;
		rankClass = RankClass.Infraspecific;
		stepName = "Add 'race' rank to ranks";
		termType = TermType.Rank;
		list.add( SingleTermUpdater.NewInstance(stepName, termType, uuidTerm, abbrev, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm).setRankClass(rankClass));

		// sublusus, #2793
		uuidTerm = UUID.fromString("1fafa596-a8e7-4e62-a378-3cc8cb3627ca");
		description = "Rank 'Sublusus'. Note: This rank is not compliant with the current nomenclatural codes";
		label = "Sublusus";
		abbrev = "sublusus";
		dtype = Rank.class.getSimpleName();
		isOrdered = true;
		uuidVocabulary = UUID.fromString("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b");
		uuidAfterTerm = UUID.fromString("196dee39-cfd8-4460-8bf0-88b83da27f625");
		uuidLang = Language.uuidEnglish;
		rankClass = RankClass.Infraspecific;
		stepName = "Add 'sublusus' rank to ranks";
		termType = TermType.Rank;
		list.add( SingleTermUpdater.NewInstance(stepName, termType, uuidTerm, abbrev, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm).setRankClass(rankClass));

		
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
		return TermUpdater_314_315.NewInstance();
	}

}
