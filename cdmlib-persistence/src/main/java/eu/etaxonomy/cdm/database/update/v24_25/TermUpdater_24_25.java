/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v24_25;

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
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_24_25 extends TermUpdaterBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_24_25.class);

	public static final String startTermVersion = "2.4.2.2.201006011715";
	private static final String endTermVersion = "2.5.0.0.201009211255";

// *************************** FACTORY **************************************/

	public static TermUpdater_24_25 NewInstance(){
		return new TermUpdater_24_25(startTermVersion, endTermVersion);
	}

// *************************** CONSTRUCTOR ***********************************/

	protected TermUpdater_24_25(String startTermVersion, String endTermVersion) {
		super(startTermVersion, endTermVersion);
	}

//

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<ITermUpdaterStep> getUpdaterList() {
		List<ITermUpdaterStep> list = new ArrayList<ITermUpdaterStep>();

		// comb. illeg.
		UUID uuidTerm = UUID.fromString("d901d455-4e01-45cb-b653-01a840b97eed");
		String description = "Combination Illegitimate";
		String label = "Combination Illegitimate";
		String abbrev = "comb. illeg.";
		String dtype = NomenclaturalStatusType.class.getSimpleName();
		UUID uuidVocabulary = UUID.fromString("bb28cdca-2f8a-4f11-9c21-517e9ae87f1f");
		UUID uuidAfterTerm = UUID.fromString("f858e619-7b7f-4225-913b-880a2143ec83");
		list.add( SingleTermUpdater.NewInstance("Add comb. illeg. status", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidLatin, true, uuidAfterTerm));

		//Habitat
		uuidTerm = UUID.fromString("fb16929f-bc9c-456f-9d40-dec987b36438");
		description = "Habitat";
		label = "Habitat";
		abbrev = "Habitat";
		dtype = Feature.class.getSimpleName();
		uuidVocabulary = uuidFeatureVocabulary;
		uuidAfterTerm = null;
		list.add( SingleTermUpdater.NewInstance("Add habitat feature", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidEnglish, false, null));

		//Habitat & Ecology
		uuidTerm = UUID.fromString("9fdc4663-4d56-47d0-90b5-c0bf251bafbb");
		description = "Habitat & Ecology";
		label = "Habitat & Ecology";
		abbrev = "Hab. & Ecol.";
		dtype = Feature.class.getSimpleName();
		uuidVocabulary = uuidFeatureVocabulary;
		uuidAfterTerm = null;
		list.add( SingleTermUpdater.NewInstance("Add habitat & ecology feature", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidEnglish, false, null));

		//Chromosome Numbers
		uuidTerm = UUID.fromString("6f677e98-d8d5-4bc5-80bf-affdb7e3945a");
		description = "Chromosome Numbers";
		label = "Chromosome Numbers";
		abbrev = "Chromosome Numbers";
		dtype = Feature.class.getSimpleName();
		uuidVocabulary = uuidFeatureVocabulary;
		uuidAfterTerm = null;
		list.add( SingleTermUpdater.NewInstance("Add chromosome number feature", uuidTerm, description, label, abbrev, dtype, uuidVocabulary, Language.uuidEnglish, false, null));

		return list;
	}

	@Override
	public ITermUpdater getNextUpdater() {
		return TermUpdater_25_30.NewInstance();
	}

	@Override
	public ITermUpdater getPreviousUpdater() {
		return null;
	}

}
