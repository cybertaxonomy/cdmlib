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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ITermUpdater;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermUpdaterBase;

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

		
//		String description;
//		String label;
//		String abbrev;
//		String dtype;
//		boolean isOrdered;
//		UUID uuidVocabulary;
//		UUID uuidAfterTerm;
//		UUID uuidLang;
//		String stepName;

//		// reported in error
//		UUID uuidTerm = UUID.fromString("38604788-cf05-4607-b155-86db456f7680");
//		description = "reported in error";
//		label = "reported in error";
//		abbrev = "f";
//		dtype = AbsenceTerm.class.getSimpleName();
//		isOrdered = true;
//		uuidVocabulary = UUID.fromString("5cd438c8-a8a1-4958-842e-169e83e2ceee");
//		uuidAfterTerm = UUID.fromString("59709861-f7d9-41f9-bb21-92559cedd598");
//		uuidLang = Language.uuidEnglish;
//		stepName = "Add 'reported in error' to absence terms";
//		list.add( SingleTermUpdater.NewInstance(stepName, uuidTerm, description, label, abbrev, dtype, uuidVocabulary, uuidLang, isOrdered, uuidAfterTerm));
//
//		String updateQuery = "UPDATE DefinedTermBase SET defaultColor = 'cccccc' WHERE uuid = '38604788-cf05-4607-b155-86db456f7680'";
//		stepName = "Add 'colour schema' to reported in error";
//		SimpleSchemaUpdaterStep colourUpdater = SimpleSchemaUpdaterStep.NewInstance(stepName, updateQuery);
//		list.add(colourUpdater);
		
		return list;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_313_314.NewInstance();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return TermUpdater_311_312.NewInstance();
	}

}
