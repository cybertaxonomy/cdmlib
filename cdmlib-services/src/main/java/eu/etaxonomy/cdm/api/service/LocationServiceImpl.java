/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.IOrderedTermVocabularyDao;

/**
 * Quick and dirty implementation of a location service as needed by the editor.
 *
 * NOTE: Current implementation does not support the IService methods like {@link #save(DefinedTermBase)}
 * as no base dao is loaded by autowiring.
 *
 * @author n.hoffman
 * @since 08.04.2009
 */
@Service
@Transactional(readOnly = true)
public class LocationServiceImpl extends ServiceBase<DefinedTermBase,IDefinedTermDao> implements ILocationService {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IOrderedTermVocabularyDao orderedVocabularyDao;

    @Override
    protected void setDao(IDefinedTermDao dao) {
        this.dao = dao;
    }


    /**
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ILocationService#getPresenceTermVocabulary()
     * FIXME Candidate for harmonization
     * is this method a duplicate of termService.getVocabulary(VocabularyEnum.PresenceTerm)
     */
    @Deprecated
    @Override
    public OrderedTermVocabulary<PresenceAbsenceTerm> getPresenceAbsenceTermVocabulary() {
        String uuidString = "adbbbe15-c4d3-47b7-80a8-c7d104e53a05";
        UUID uuid = UUID.fromString(uuidString);
        OrderedTermVocabulary<PresenceAbsenceTerm> presenceTermVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return presenceTermVocabulary;
    }

    @Override
    public List<NamedAreaVocabularyType> getNamedAreaVocabularyTypes() {
        List<NamedAreaVocabularyType> result = new ArrayList<NamedAreaVocabularyType>(3);
        result.add(NamedAreaVocabularyType.TDWG_AREA);
        result.add(NamedAreaVocabularyType.COUNTRY);
        result.add(NamedAreaVocabularyType.WATERBODY);
        result.add(NamedAreaVocabularyType.CONTINENT);
        return result;
    }

    @Override
    public OrderedTermVocabulary<NamedArea> getNamedAreaVocabulary(NamedAreaVocabularyType vocabularyType) {

        UUID namedAreaVocabularyUuid = null;

        if(vocabularyType == NamedAreaVocabularyType.TDWG_AREA){
            namedAreaVocabularyUuid = UUID.fromString("1fb40504-d1d7-44b0-9731-374fbe6cac77");
        }
        if(vocabularyType == NamedAreaVocabularyType.CONTINENT){
            namedAreaVocabularyUuid = UUID.fromString("e72cbcb6-58f8-4201-9774-15d0c6abc128");
        }
        if(vocabularyType == NamedAreaVocabularyType.COUNTRY){
            namedAreaVocabularyUuid = UUID.fromString("006b1870-7347-4624-990f-e5ed78484a1a");
        }
        if(vocabularyType == NamedAreaVocabularyType.WATERBODY){
            namedAreaVocabularyUuid = UUID.fromString("35a62b25-f541-4f12-a7c7-17d90dec3e03");
        }
        return (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(namedAreaVocabularyUuid);
    }

    @Override
    public OrderedTermVocabulary<NamedAreaLevel> getNamedAreaLevelVocabulary() {
        // TODO return namedAreaLevel filtered by NamedAreaVocabularyType
        String uuidString = "49034253-27c8-4219-97e8-f8d987d3d122";
        UUID uuid = UUID.fromString(uuidString);
        OrderedTermVocabulary<NamedAreaLevel> namedAreaLevelVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return namedAreaLevelVocabulary;
    }

    /**
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ILocationService#getNamedAreaTypeVocabulary()
     * FIXME Candidate for harmonization
     * is this method a duplicate of termService.getVocabulary(VocabularyEnum.NamedAreaType)
     */
    @Override
    public TermVocabulary<NamedAreaType> getNamedAreaTypeVocabulary() {
        String uuidString = "e51d52d6-965b-4f7d-900f-4ba9c6f5dd33";
        UUID uuid = UUID.fromString(uuidString);
        TermVocabulary<NamedAreaType> namedAreaTypeVocabulary =
            (OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
        return namedAreaTypeVocabulary;
    }

    @Override
    public List<NamedArea> getTopLevelNamedAreasByVocabularyType(NamedAreaVocabularyType vocabularyType){

        OrderedTermVocabulary<NamedArea> vocabulary = getNamedAreaVocabulary(vocabularyType);

        List<NamedArea> topLevelTerms = new ArrayList<>();

//		for(NamedArea area : vocabulary){
        Iterator<NamedArea> it = vocabulary.getTerms().iterator();
        while(it.hasNext()){

            NamedArea area =  HibernateProxyHelper.deproxy(it.next(), NamedArea.class);
            if(area.getPartOfWorkaround() == null){
                topLevelTerms.add(area);
            }
        }

        return topLevelTerms;
    }
}