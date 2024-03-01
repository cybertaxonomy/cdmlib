/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermTreeDto;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.portal.DistributionTreeDtoLoader;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;

/**
 * Class implementing the business logic for creating the map service string for
 * a given set of distributions. See {@link EditGeoService} as API for the given functionality.
 *
 * @see EditGeoService
 * @see
 *
 * @author a.mueller
 * @since 17.11.2008 (as {@link DistributionServiceUtilities} in cdmlib-ext)
 */
public class DistributionServiceUtilities {

    private static final Logger logger = LogManager.getLogger();

    private static HashMap<SpecimenOrObservationType, Color> defaultSpecimenOrObservationTypeColors = new HashMap<>();
    static {
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.FieldUnit, Color.ORANGE);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.DerivedUnit, Color.RED);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.LivingSpecimen, Color.GREEN);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Observation, Color.ORANGE);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.PreservedSpecimen, Color.GRAY);
        defaultSpecimenOrObservationTypeColors.put(SpecimenOrObservationType.Media, Color.BLUE);
    }

    private static HashMap<SpecimenOrObservationType, Color> getDefaultSpecimenOrObservationTypeColors() {
        return defaultSpecimenOrObservationTypeColors;
    }

    private static HashMap<PresenceAbsenceTerm, Color> defaultPresenceAbsenceTermBaseColors =  new HashMap<>();
    static {
        defaultPresenceAbsenceTermBaseColors = new HashMap<>();
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.PRESENT(), Color.decode("0x4daf4a"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATIVE(), Color.decode("0x4daf4a"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE(), Color.decode("0x377eb8"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.CULTIVATED(), Color.decode("0x984ea3"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.INTRODUCED(), Color.decode("0xff7f00"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.CASUAL(), Color.decode("0xffff33"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.INTRODUCED_CULTIVATED(), Color.decode("0xa65628"));
        defaultPresenceAbsenceTermBaseColors.put(PresenceAbsenceTerm.NATURALISED(), Color.decode("0xf781bf"));
    }

    private static List<UUID>  presenceAbsenceTermVocabularyUuids = null;

    /**
     * @param statusColorJson for example: {@code {"n":"#ff0000","p":"#ffff00"}}
     */
    public static Map<UUID,Color> buildStatusColorMap(String statusColorJson,
            ITermService termService, IVocabularyService vocabularyService)
            throws JsonProcessingException {

        Map<UUID,Color> presenceAbsenceTermColors = null;
        if(StringUtils.isNotEmpty(statusColorJson)){

            ObjectMapper mapper = new ObjectMapper();
            // TODO cache the color maps to speed this up?

            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);

            Map<String,String> statusColorMap = mapper.readValue(statusColorJson, mapType);
            presenceAbsenceTermColors = new HashMap<>();
            UUID paTermUuid = null;
            for(String statusId : statusColorMap.keySet()){
                try {
                    Color color = Color.decode(statusColorMap.get(statusId));
                    // the below loop is  a hack for #4522 (custom status colors not working in cyprus portal)
                    // remove it once the ticket is solved
                    for(UUID vocabUuid : presenceAbsenceTermVocabularyUuids(vocabularyService)) {
                        paTermUuid = termService.findUuidByIdInVocabulary(statusId, vocabUuid, PresenceAbsenceTerm.class);
                        if(paTermUuid != null) {
                            break;
                        }
                    }
                    if(paTermUuid != null){
                        presenceAbsenceTermColors.put(paTermUuid, color);
                    }
                } catch (NumberFormatException e){
                    logger.error("Cannot decode color", e);
                }
            }
        }
        return presenceAbsenceTermColors;
    }

    /**
     * this is a hack for #4522 (custom status colors not working in cyprus portal)
     * remove this method once the ticket is solved
     *
     * @param vocabularyService
     * @return
     */
    private static List<UUID> presenceAbsenceTermVocabularyUuids(IVocabularyService vocabularyService) {

        if(DistributionServiceUtilities.presenceAbsenceTermVocabularyUuids == null) {

            List<UUID> uuids = new ArrayList<>();
            // the default as first entry
            UUID presenceTermVocabUuid = PresenceAbsenceTerm.NATIVE().getVocabulary().getUuid();
            uuids.add(presenceTermVocabUuid);

            for(TermVocabulary<?> vocab : vocabularyService.findByTermType(TermType.PresenceAbsenceTerm, null)) {
                if(!uuids.contains(vocab.getUuid())) {
                    uuids.add(vocab.getUuid());
                }
            }
            DistributionServiceUtilities.presenceAbsenceTermVocabularyUuids = uuids;
        }
        return DistributionServiceUtilities.presenceAbsenceTermVocabularyUuids;
    }

    public static DistributionTreeDto buildOrderedTreeDto(Set<NamedAreaLevel> omitLevels,
            Collection<DistributionDto> distributions,
            SetMap<NamedAreaDto,NamedAreaDto> area2ParentAreaMap,
            TermTreeDto areaTree,
            Set<UUID> fallbackAreaMarkerTypes,
            Set<UUID> alternativeRootAreaMarkerType,
            boolean neverUseFallbackAreaAsParent,
            DistributionOrder distributionOrder,
            IDefinedTermDao termDao,
            boolean useSecondMethod) {

        //TODO loader needed?
        DistributionTreeDtoLoader loader = new DistributionTreeDtoLoader(termDao);
        DistributionTreeDto distributionTreeDto = loader.load();

        if (logger.isDebugEnabled()){logger.debug("order tree ...");}

        //order by areas
        if (!useSecondMethod) {
            loader.orderAsTree(distributionTreeDto, distributions, area2ParentAreaMap, omitLevels,
                    fallbackAreaMarkerTypes, neverUseFallbackAreaAsParent);
        }else {
            loader.orderAsTree2(distributionTreeDto, distributions, areaTree, omitLevels,
                    fallbackAreaMarkerTypes, neverUseFallbackAreaAsParent);
        }
        loader.handleAlternativeRootArea(distributionTreeDto, alternativeRootAreaMarkerType);
        loader.recursiveSortChildren(distributionTreeDto, distributionOrder); // TODO respect current locale for sorting

        if (logger.isDebugEnabled()){logger.debug("create tree - DONE");}
        return distributionTreeDto;
    }
}