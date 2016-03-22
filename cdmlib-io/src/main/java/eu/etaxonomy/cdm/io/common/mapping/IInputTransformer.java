// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;


/**
 * Interface for import and export transformer classes. Mainly to transform defined terms.
 * @author a.mueller
 * @created 15.03.2010
 * @version 1.0
 */
public interface IInputTransformer {

	//Feature
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException;

	//Language
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException;

	//Extension Type
	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getExtensionTypeUuid(String key) throws UndefinedTransformerMethodException;

	//MarkerType
	public MarkerType getMarkerTypeByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException;

	//NameTypeDesignationStatus
	public NameTypeDesignationStatus getNameTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getNameTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException;


	//SpecimenTypeDesignationStatus
	public SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getSpecimenTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException;


	//Presence Term
	public PresenceAbsenceTerm getPresenceTermByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException;

	//Named area
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException;

	//named area level
	public NamedAreaLevel getNamedAreaLevelByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getNamedAreaLevelUuid(String key) throws UndefinedTransformerMethodException;

	//reference system
	public UUID getReferenceSystemUuid(String key)  throws UndefinedTransformerMethodException;

	public ReferenceSystem getReferenceSystemByKey(String key) throws UndefinedTransformerMethodException;

	//rank
	//Feature
	public Rank getRankByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getRankUuid(String key) throws UndefinedTransformerMethodException;

	//state
	public State getStateByKey(String key) throws UndefinedTransformerMethodException;

	public UUID getStateUuid(String key) throws UndefinedTransformerMethodException;

    public NomenclaturalStatusType getNomenclaturalStatusByKey(String key) throws UndefinedTransformerMethodException;

}
