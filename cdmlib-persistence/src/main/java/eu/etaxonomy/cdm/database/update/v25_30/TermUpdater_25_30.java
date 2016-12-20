/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v25_30;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SingleTermUpdater;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;
import eu.etaxonomy.cdm.database.update.VocabularyCreator;
import eu.etaxonomy.cdm.database.update.v24_25.TermUpdater_24_25;
import eu.etaxonomy.cdm.database.update.v30_31.TermUpdater_30_31;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_25_30 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_25_30.class);
	
	public static final String startTermVersion = "2.5.0.0.201009211255";
	private static final String endTermVersion = "3.0.0.0.201011170000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_25_30 NewInstance(){
		return new TermUpdater_25_30(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_25_30(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}
	
// 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

		// cf.
		UUID uuidTerm = DefinedTerm.uuidConfer;
		String description = "Confer";
		String label = "confer";
		String abbrev = "cf.";
		String dtype = DefinedTerm.class.getSimpleName();
		UUID uuidVocabulary = UUID.fromString("fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77");
		UUID uuidAfterTerm = null ; //UUID.fromString("");
		list.add( SingleTermUpdater.NewInstance("Add 'confer (cf.)' determination modifier", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidLatin, true, uuidAfterTerm));

		
		// aff.
		uuidTerm = DefinedTerm.uuidAffinis;
		description = "Affinis";
		label = "affinis";
		abbrev = "aff.";
		dtype = DefinedTerm.class.getSimpleName();
		uuidVocabulary = UUID.fromString("fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77");
		uuidAfterTerm = DefinedTerm.uuidConfer;
		list.add( SingleTermUpdater.NewInstance("Add 'affinis (aff.)' determination modifier", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidLatin, true, uuidAfterTerm));

		
		//undefined languages vocabulary
		UUID uuidUndefLanguagesVoc = UUID.fromString("7fd1e6d0-2e76-4dfa-bad9-2673dd042c28");
		description = "Undefined Language";
		label = "Undefined Language";
		abbrev = "undef. lang.";
		boolean isOrdered = false;
		Class termClass = Language.class;
		VocabularyCreator updater = VocabularyCreator.NewVocabularyInstance(uuidUndefLanguagesVoc, description, label, abbrev, isOrdered, termClass, TermType.Language);
		list.add(updater);
		
		// unknown language
		uuidTerm = Language.uuidUnknownLanguage;
		description = "Unknown Language";
		label = "unknown";
		abbrev = "unk";
		dtype = Language.class.getSimpleName();
		uuidVocabulary = uuidUndefLanguagesVoc;
		uuidAfterTerm = null;
		UUID uuidLanguageOfRepresentation = Language.uuidEnglish;
		isOrdered = false;
		list.add( SingleTermUpdater.NewInstance("Add 'unknown Lanugage' to language vocabulary", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLanguageOfRepresentation, isOrdered, uuidAfterTerm));

		// original language
		uuidTerm = Language.uuidOriginalLanguage;
		description = "Original Language";
		label = "original";
		abbrev = "org";
		dtype = Language.class.getSimpleName();
		uuidVocabulary = uuidUndefLanguagesVoc;
		uuidAfterTerm = Language.uuidUnknownLanguage; //needed ?
		uuidLanguageOfRepresentation = Language.uuidEnglish;
		isOrdered = false;
		list.add( SingleTermUpdater.NewInstance("Add 'original language' to language vocabulary", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLanguageOfRepresentation, isOrdered, uuidAfterTerm));

				
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_30_31.NewInstance();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_24_25.NewInstance();
	}

}
