/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print.docbuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator.FactSortMode;
import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator.FeatureSortMode;
import eu.etaxonomy.cdm.io.print.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Immutable snapshot of the data and rendering options required to build a
 * Print/Publication document.
 *
 * The request contains no reference to PrintPubExportState or the complete
 * PrintPubExportConfigurator.
 */
public record PrintPubDocumentRequest(
        String documentTitle,
        List<PrintPubTaxonSummaryDTO> taxa,
        List<Reference> bibliography,
        boolean includeSynonyms,
        boolean includeSynonymConceptReferences,
        boolean oneLinePerHomotypicGroup,
        boolean includeScientificNameIndex,
        boolean includeCommonNameIndex,
        boolean includeIdentifierAppendix,
        boolean includeEmptyIds,
        boolean includeWfoId,
        boolean includeIpniId,
        boolean includeProtologueUris) {

    public PrintPubDocumentRequest {

        documentTitle =
                documentTitle == null || documentTitle.isBlank()
                        ? "Taxonomic Export"
                        : documentTitle.trim();

        taxa =
                taxa == null
                        ? List.of()
                        : List.copyOf(taxa);

        bibliography =
                bibliography == null
                        ? List.of()
                        : List.copyOf(bibliography);
    }

    /**
     * Indicates whether the document contains bibliography entries.
     */
    public boolean hasBibliography() {
        return !bibliography.isEmpty();
    }

    /**
     * Indicates whether there are taxa available for rendering.
     */
    public boolean hasTaxa() {
        return !taxa.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String documentTitle = "Taxonomic Export";

        private List<PrintPubTaxonSummaryDTO> taxa =
                List.of();

        private Map<UUID, Integer> featureOrderIndex =
                Map.of();

        private List<Reference> bibliography =
                List.of();

        private boolean includeSynonyms;
        private boolean includeSynonymConceptReferences;
        private boolean oneLinePerHomotypicGroup = true;

        private boolean includeScientificNameIndex;
        private boolean includeCommonNameIndex;

        private boolean includeAppendix;
        private boolean includeEmptyIds;
        private boolean includeWfoId;
        private boolean includeIpniId;
        private boolean includeProtologueUris;

        private FeatureSortMode featureSortMode =
                FeatureSortMode.ALPHABETICAL;

        private FactSortMode factSortMode =
                FactSortMode.PORTAL_LIKE;

        private Builder() {
        }

        public Builder documentTitle(String value) {
            this.documentTitle = value;
            return this;
        }

        public Builder taxa(
                List<PrintPubTaxonSummaryDTO> value) {

            this.taxa = value;
            return this;
        }

        public Builder featureOrderIndex(
                Map<UUID, Integer> value) {

            this.featureOrderIndex = value;
            return this;
        }

        public Builder bibliography(
                List<Reference> value) {

            this.bibliography = value;
            return this;
        }

        public Builder includeSynonyms(boolean value) {
            this.includeSynonyms = value;
            return this;
        }

        public Builder includeSynonymConceptReferences(
                boolean value) {

            this.includeSynonymConceptReferences = value;
            return this;
        }

        public Builder oneLinePerHomotypicGroup(
                boolean value) {

            this.oneLinePerHomotypicGroup = value;
            return this;
        }

        public Builder includeScientificNameIndex(
                boolean value) {

            this.includeScientificNameIndex = value;
            return this;
        }

        public Builder includeCommonNameIndex(
                boolean value) {

            this.includeCommonNameIndex = value;
            return this;
        }

        public Builder includeAppendix(boolean value) {
            this.includeAppendix = value;
            return this;
        }

        public Builder includeEmptyIds(boolean value) {
            this.includeEmptyIds = value;
            return this;
        }

        public Builder includeWfoId(boolean value) {
            this.includeWfoId = value;
            return this;
        }

        public Builder includeIpniId(boolean value) {
            this.includeIpniId = value;
            return this;
        }

        public Builder includeProtologueUris(
                boolean value) {

            this.includeProtologueUris = value;
            return this;
        }

        public Builder featureSortMode(
                FeatureSortMode value) {

            this.featureSortMode = value;
            return this;
        }

        public Builder factSortMode(
                FactSortMode value) {

            this.factSortMode = value;
            return this;
        }

        public PrintPubDocumentRequest build() {

            return new PrintPubDocumentRequest(
                    documentTitle,
                    taxa,
                    bibliography,
                    includeSynonyms,
                    includeSynonymConceptReferences,
                    oneLinePerHomotypicGroup,
                    includeScientificNameIndex,
                    includeCommonNameIndex,
                    includeAppendix,
                    includeEmptyIds,
                    includeWfoId,
                    includeIpniId,
                    includeProtologueUris);
        }
    }
}