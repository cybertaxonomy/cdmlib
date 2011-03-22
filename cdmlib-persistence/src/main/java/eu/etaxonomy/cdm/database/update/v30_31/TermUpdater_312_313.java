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
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_312_313 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_312_313.class);
	
	public static final String startTermVersion = "3.0.1.2.201102090000";
	private static final String endTermVersion = "3.0.1.3.201103210000";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_312_313 NewInstance(){
		return new TermUpdater_312_313(startTermVersion, endTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_312_313(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}
	
// 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

		//THIS CLASS IS FOR FUTURE USE. NOT NEEDED YET.
		
		
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
//		UUID uuidTerm = UUID.fromString("0deddc65-2505-4c77-91a7-17d0de24afcc");
//		description = "hermaphrodite";
//		label = "hermaphrodite";
//		abbrev = "h";
//		dtype = Sex.class.getSimpleName();
//		isOrdered = true;
//		uuidVocabulary = UUID.fromString("9718b7dd-8bc0-4cad-be57-3c54d4d432fe");
//		uuidAfterTerm = UUID.fromString("b4cfe0cb-b35c-4f97-9b6b-2b3c096ea2c0");
//		uuidLang = Language.uuidEnglish;
//		stepName = "Add 'hermaphrodite' to sex";
//		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));

	
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
		return TermUpdater_311_312.NewInstance();
	}

}
