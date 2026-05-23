/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.util.UUID;

import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * Factory methods for {@link EntityFilter}s related to {@link DefinedTermBase defined terms}.
 *
 * @author muellera
 * @since 23.05.2026
 */
public class DefinedTermFilters extends CdmFiltersBase {

    public static final String TEXT = "text";
    public static final String ID_IN_VOC = "idInVocabulary";
    public static final String ABBREV_LABEL = "abbreviatedLabel";
    public static final String REPRESENTATIONS = "representations";
    public static final String ISO_3166_A2 = "iso3166_A2";


    public static <T extends DefinedTermBase<T>> EntityFilter<T> representationTextFilter(
            String text, @SuppressWarnings("unused") Class<T> clazzForCasting) {
        return (root, cb) -> cb.equal(root.join("representations")
                .get(TEXT), text);
    }

    public static <T extends DefinedTermBase<T>> EntityFilter<T> representationAbbreviationFilter(
            String text, @SuppressWarnings("unused") Class<T> clazzForCasting) {
        return (root, cb) -> cb.equal(root.join(REPRESENTATIONS)
                .get(ABBREV_LABEL), text);
    }

    public static <T extends DefinedTermBase<T>> EntityFilter<T> idInVocabularyFilter(
            String idInVoc, UUID vocabularyUuid, @SuppressWarnings("unused") Class<T> clazzForCasting) {
        return (root, cb) ->
                cb.and(
                    cb.equal(root.join(REPRESENTATIONS)
                            .get(CdmBaseFilters.UUID), vocabularyUuid),
                    cb.equal(root.get(ID_IN_VOC), idInVoc)
                );
    }

    public static <T extends DefinedTermBase<T>> EntityFilter<T> namedAreaLevelAndTypeFilter(
            String idInVoc, UUID vocabularyUuid, @SuppressWarnings("unused") Class<T> clazzForCasting) {
        return (root, cb) ->
                cb.and(
                    cb.equal(root.join(REPRESENTATIONS)
                            .get(CdmBaseFilters.UUID), vocabularyUuid),
                    cb.equal(root.get(ID_IN_VOC), idInVoc)
                );
    }

    public static EntityFilter<NamedArea> namedAreaLevelAndTypeFilter(NamedAreaLevel level,
            NamedAreaType type) {
        return (root, cb) ->
            cb.and(
                predicateEqualIfNotNull(cb, root, "type", type),
                predicateEqualIfNotNull(cb, root, "level", level)
            );
    }

    public static EntityFilter<Country> countryByIsoFilter(String isoCode) {
        return (root, cb) ->
                cb.or(
                    cb.equal(root.get(ISO_3166_A2), isoCode),
                    cb.equal(root.get(ID_IN_VOC), isoCode)
                );
    }
}