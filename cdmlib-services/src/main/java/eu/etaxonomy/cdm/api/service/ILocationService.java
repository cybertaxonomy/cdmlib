/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;

/**
 * @author n.hoffman
 * @since 08.04.2009
 * @version 1.0
 */
public interface ILocationService extends IService<DefinedTermBase> {

	
	public static enum NamedAreaVocabularyType{
		TDWG_AREA, COUNTRY, WATERBODY, CONTINENT
	}
	
    /**
     * Returns a list of NamedArea Types "TDWG Areas", "ISO Country Codes"
     * 
     * @return
     */
    public List<NamedAreaVocabularyType> getNamedAreaVocabularyTypes();
    
    /**
     *  - these would be top-level areas for nested area vocabularies, e.g. North America and Europe, but NOT USA, Germany - alternatively:
     * 
     * @param vocabularyType
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    public OrderedTermVocabulary<NamedArea> getNamedAreaVocabulary(NamedAreaVocabularyType vocabularyType);
    
    /**
     * 
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    public TermVocabulary<NamedAreaType> getNamedAreaTypeVocabulary();
    
    /**
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    public OrderedTermVocabulary<NamedAreaLevel> getNamedAreaLevelVocabulary();
    
    /**
     * 
     * @return
     * @deprecated use TermService#getVocabulary(VocabularyType) instead
     */
    public OrderedTermVocabulary<PresenceAbsenceTerm> getPresenceAbsenceTermVocabulary();
   
    /**
     * 
     */
    public List<NamedArea> getTopLevelNamedAreasByVocabularyType(NamedAreaVocabularyType vocabularyType);
}
