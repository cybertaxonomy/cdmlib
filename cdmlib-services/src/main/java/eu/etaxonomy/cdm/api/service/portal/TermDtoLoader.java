/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.service.geo.GeoServiceArea;
import eu.etaxonomy.cdm.api.service.geo.GeoServiceAreaAnnotatedMapping;
import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author muellera
 * @since 29.02.2024
 */
public class TermDtoLoader {

    private static final Logger logger = LogManager.getLogger();

    public static TermDtoLoader INSTANCE(){
        return new TermDtoLoader();
    }

    //TODO maybe not always needed but only in DinstributionInfo context
    public NamedAreaDto fromEntity(NamedArea entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        //TODO i18n
//        UUID parentUuid = entity.getPartOf() != null ? entity.getPartOf().getUuid() : null;
        NamedAreaDto dto = new NamedAreaDto(entity.getUuid(), entity.getId(), getPreferredLabel(entity));
        load(dto, entity);
        return dto;
    }


    public <T extends DefinedTermBase<T>> TermDto fromEntity(T entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        if (entity instanceof NamedArea) {  //calling fromEntity(NamedArea)directly does not always work as the term is sometimes casted to a lower class before
            return fromEntity((NamedArea)entity);
        }else {
            //TODO i18n
            TermDto dto = new TermDto(entity.getUuid(), entity.getId(), getPreferredLabel(entity));
            if (entity instanceof PresenceAbsenceTerm) {
                dto.setDefaultColor(((PresenceAbsenceTerm)entity).getDefaultColor());
            }
            load(dto, entity);
            return dto;
        }
    }

    private void load(NamedAreaDto dto, NamedArea term) {
        load((TermDto)dto, term);
        dto.setLevelUuid(term.getLevel() == null ? null : term.getLevel().getUuid());
        term.getMarkers().stream().filter(m->m.getValue() && m.getMarkerType() != null)
            .map(m->m.getMarkerType().getUuid())
            .forEach(uuid->dto.addMarker(uuid));
        if (term.getVocabulary()!=null) { //should always be true
            dto.setVocabularyUuid(term.getVocabulary().getUuid());
        }
        //TODO only on demand
        dto.setGeoServiceMapping(getGeoServiceMapping(term));
    }


    private <T extends DefinedTermBase<T>> void load(TermDto dto, T term) {
        dto.setSymbol1(term.getSymbol());
        dto.setSymbol2(term.getSymbol2());
        dto.setIdInVoc(term.getIdInVocabulary());
        dto.setOrderIndex(term.getOrderIndex());
        dto.setAbbrevLabel(term.getPreferredAbbreviation(LocaleContext.getLanguages(),
                false, false));
        return;
    }

    private String getPreferredLabel(DefinedTermBase<?> term) {
        return term.getPreferredLabel(LocaleContext.getLanguages());
    }

    private String getGeoServiceMapping(NamedArea term) {
        try {
            GeoServiceArea geoServiceArea = new GeoServiceAreaAnnotatedMapping().valueOf(term);
            if (geoServiceArea != null) {
                return geoServiceArea.toXml();
            }
        } catch (XMLStreamException e) {
            logger.warn("Exception during geoServiceMapping retrieval");
        }
        return null;
    }
}