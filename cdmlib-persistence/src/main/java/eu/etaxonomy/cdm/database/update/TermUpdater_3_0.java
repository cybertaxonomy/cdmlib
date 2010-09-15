// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_3_0 extends TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_3_0.class);
	
	public static final String myTermVersion = "2.4.2.2.201006011715";
	
// *************************** FACTORY **************************************/
	
	public static TermUpdater_3_0 NewInstance(){
		return new TermUpdater_3_0(myTermVersion);
	}
	
// *************************** CONSTRUCTOR ***********************************/	

	protected TermUpdater_3_0(String mySchemaVersion) {
		super(mySchemaVersion);
	}
	
// 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	protected List<SingleTermUpdater> getUpdaterList() {
		List<SingleTermUpdater> list = new ArrayList<SingleTermUpdater>();
		
		// comb. illeg.
		UUID uuidTerm = UUID.fromString("d901d455-4e01-45cb-b653-01a840b97eed");
		String description = "Combination Illegitimate";
		String label = "Combination Illegitimate";
		String abbrev = "comb. illeg.";
		String dtype = NomenclaturalStatusType.class.getSimpleName();
		UUID uuidVocabulary = UUID.fromString("bb28cdca-2f8a-4f11-9c21-517e9ae87f1f");
		Integer orderIndex = null;
		list.add( SingleTermUpdater.NewInstance("Add comb. illeg. status", uuidTerm, description, label, abbrev, dtype, orderIndex, uuidVocabulary));
		
		//Habitat
		uuidTerm = UUID.fromString("fb16929f-bc9c-456f-9d40-dec987b36438");
		description = "Habitat";
		label = "Habitat";
		abbrev = "Habitat";
		dtype = Feature.class.getSimpleName();
		uuidVocabulary = uuidFeatureVocabulary;
		orderIndex = null;
		list.add( SingleTermUpdater.NewInstance("Add habitat feature", uuidTerm, description, label, abbrev, dtype, orderIndex, uuidVocabulary));

		//Habitat & Ecology
		uuidTerm = UUID.fromString("9fdc4663-4d56-47d0-90b5-c0bf251bafbb");
		description = "Habitat & Ecology";
		label = "Habitat & Ecology";
		abbrev = "Hab. & Ecol.";
		dtype = Feature.class.getSimpleName();
		uuidVocabulary = uuidFeatureVocabulary;
		orderIndex = null;
		list.add( SingleTermUpdater.NewInstance("Add habitat & ecology feature", uuidTerm, description, label, abbrev, dtype, orderIndex, uuidVocabulary));

		//Chromosome Numbers
		uuidTerm = UUID.fromString("6f677e98-d8d5-4bc5-80bf-affdb7e3945a");
		description = "Chromosome Numbers";
		label = "Chromosome Numbers";
		abbrev = "Chromosome Numbers";
		dtype = Feature.class.getSimpleName();
		uuidVocabulary = uuidFeatureVocabulary;
		orderIndex = null;
		list.add( SingleTermUpdater.NewInstance("Add chromosome number feature", uuidTerm, description, label, abbrev, dtype, orderIndex, uuidVocabulary));
		
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
		return null;
	}

}
