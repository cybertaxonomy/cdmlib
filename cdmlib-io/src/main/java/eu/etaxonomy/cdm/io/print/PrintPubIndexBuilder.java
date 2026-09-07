/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.print.dto.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubTaxonSummaryDTO;

@Component
public class PrintPubIndexBuilder {

    public List<String> buildScientificNameIndex(List<PrintPubTaxonSummaryDTO> taxa) {

        if (taxa == null || taxa.isEmpty()) {
            return List.of();
        }

        return taxa.stream().filter(Objects::nonNull)
                .flatMap(taxon -> Stream.concat(Stream.ofNullable(taxon.scientificName), synonymScientificNames(taxon)))
                .filter(StringUtils::isNotBlank).map(String::trim).distinct().sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> buildCommonNameIndex(List<PrintPubTaxonSummaryDTO> taxa) {

        if (taxa == null || taxa.isEmpty()) {
            return List.of();
        }

        return taxa.stream().filter(Objects::nonNull).filter(taxon -> taxon.commonNames != null)
                .flatMap(taxon -> taxon.commonNames.stream()).filter(StringUtils::isNotBlank).map(String::trim)
                .distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private Stream<String> synonymScientificNames(PrintPubTaxonSummaryDTO taxon) {

        Stream<String> homotypicNames = synonymScientificNames(taxon.homotypicSynonymGroup);

        Stream<String> heterotypicNames = taxon.synonymGroups == null ? Stream.empty()
                : taxon.synonymGroups.stream().filter(Objects::nonNull).flatMap(this::synonymScientificNames);

        return Stream.concat(homotypicNames, heterotypicNames);
    }

    private Stream<String> synonymScientificNames(PrintPubSynonymGroupDTO group) {

        if (group == null || group.synonyms == null) {
            return Stream.empty();
        }

        return group.synonyms.stream().filter(Objects::nonNull).map(synonym -> synonym.scientificName);
    }
}
