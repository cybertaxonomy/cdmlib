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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_311_312 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_311_312.class);
	
	public static final String startTermVersion = "3.0.1.1.201101310000";
	private static final String endTermVersion = "3.0.1.2.201102090000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_311_312 NewInstance(){
		return new TermUpdater_311_312(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_311_312(String startTermVersion, String endTermVersion) {
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

		// sex.hermaphrodite
		UUID uuidTerm = UUID.fromString("0deddc65-2505-4c77-91a7-17d0de24afcc");
		description = "hermaphrodite";
		label = "hermaphrodite";
		abbrev = "h";
		dtype = "Sex";
		isOrdered = true;
		uuidVocabulary = UUID.fromString("9718b7dd-8bc0-4cad-be57-3c54d4d432fe");
		uuidAfterTerm = UUID.fromString("b4cfe0cb-b35c-4f97-9b6b-2b3c096ea2c0");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'hermaphrodite' to sex";
		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		// sex.unknown
		uuidTerm = UUID.fromString("4f5e4c51-a664-48ad-8238-2e9f49eaf8dd");
		description = "Sex unknown";
		label = "unknown";
		abbrev = "sex ?";
		dtype = "Sex";
		isOrdered = true;
		uuidVocabulary = UUID.fromString("9718b7dd-8bc0-4cad-be57-3c54d4d432fe");
		uuidAfterTerm = UUID.fromString("0deddc65-2505-4c77-91a7-17d0de24afcc");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'unknown' to sex";
		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

		// hybridRelationshipType.thirdParent
		uuidTerm = UUID.fromString("bfae2780-92ab-4f65-b534-e68826f59e7d");
		description = "Third parent";
		label = "Third parent";
		abbrev = null;
		dtype = HybridRelationshipType.class.getSimpleName();
		isOrdered = true;
		uuidVocabulary = UUID.fromString("fc4abe52-9c25-4cfa-a682-8615bf4bbf07");
		uuidAfterTerm = UUID.fromString("0485fc3d-4755-4f53-8832-b82774484c43");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'third parent' to hybrid relationship type";
		SingleTermUpdater hybridUpdater = SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm);
		hybridUpdater.setReverseRepresentation("Child", "Child", null);
		list.add(hybridUpdater);


		// hybridRelationshipType.fourthParent
		uuidTerm = UUID.fromString("9e92083b-cb9b-4c4d-bca5-c543bbefd3c7");
		description = "Fourth parent";
		label = "Fourth parent";
		abbrev = null;
		dtype = HybridRelationshipType.class.getSimpleName();
		isOrdered = true;
		uuidVocabulary = UUID.fromString("fc4abe52-9c25-4cfa-a682-8615bf4bbf07");
		uuidAfterTerm = UUID.fromString("bfae2780-92ab-4f65-b534-e68826f59e7d");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'fourth parent' to hybrid relationship type";
		hybridUpdater = SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm);
		hybridUpdater.setReverseRepresentation("Child", "Child", null);
		list.add(hybridUpdater);

		
		// hybridRelationshipType.majorParent
		uuidTerm = UUID.fromString("da759eea-e3cb-4d3c-ae75-084c2d08f4ed");
		description = "Major parent";
		label = "Major parent";
		abbrev = "major";
		dtype = HybridRelationshipType.class.getSimpleName();
		isOrdered = true;
		uuidVocabulary = UUID.fromString("fc4abe52-9c25-4cfa-a682-8615bf4bbf07");
		uuidAfterTerm = UUID.fromString("8b7324c5-cc6c-4109-b708-d49b187815c4");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'major parent' to hybrid relationship type";
		hybridUpdater = SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm);
		hybridUpdater.setReverseRepresentation("Child", "Child", null);
		list.add(hybridUpdater);
	
		// hybridRelationshipType.minorParent
		uuidTerm = UUID.fromString("e556b240-b03f-46b8-839b-ad89df633c5a");
		description = "Minor parent";
		label = "Minor parent";
		abbrev = "minor";
		dtype = HybridRelationshipType.class.getSimpleName();
		isOrdered = true;
		uuidVocabulary = UUID.fromString("fc4abe52-9c25-4cfa-a682-8615bf4bbf07");
		uuidAfterTerm = UUID.fromString("da759eea-e3cb-4d3c-ae75-084c2d08f4ed");
		uuidLang = Language.uuidEnglish;
		stepName = "Add 'minor parent' to hybrid relationship type";
		hybridUpdater = SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm);
		hybridUpdater.setReverseRepresentation("Child", "Child", null);
		list.add(hybridUpdater);
	
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_312_313.NewInstance();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_31_311.NewInstance();
	}

}
