/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print.docbuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.io.print.PrintPubCitationRegistry;
import eu.etaxonomy.cdm.io.print.PrintPubExportState;
import eu.etaxonomy.cdm.io.print.compare.PrintPubFeatureKey;
import eu.etaxonomy.cdm.io.print.docmodel.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubSectionHeaderElement;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubTextRunElement;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubTextRunElement.Run;
import eu.etaxonomy.cdm.io.print.docmodel.PrintPubTextRunElement.RunType;
import eu.etaxonomy.cdm.io.print.dto.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.print.util.PrintPubNonNestedHtmlTokenConverter;
import eu.etaxonomy.cdm.io.print.util.PrintPubNonNestedHtmlTokenizer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Builds the Print/Publication document model.
 *
 * This builder does not write elements to the state's processor. Instead, it
 * returns a list containing the complete document model.
 */
@Component("printPubDocumentBuilder")
public class PrintPubDocumentBuilder {

    private static final String SYNONYM_MARKER = UTF8.EQUALS_SIGN + " ";
    private static final String HOMOTYPIC_MARKER = UTF8.IDENTICAL_TO + " ";
    private static final String INVALID_NAME_MARKER = UTF8.MINUS + " ";
    private static final String ACC_SEC_MARKER = " sec. ";
    private static final String SYN_SEC_MARKER = " syn sec. ";

    public List<IPrintPubDocumentElement> buildLayout(PrintPubDocumentRequest request) {

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.addAll(buildHeader(request));
        elements.addAll(buildContent(request));

        if (request.hasBibliography()) {
            elements.addAll(buildBibliography(request.bibliography()));
        }

        if (request.includeScientificNameIndex()) {
            elements.addAll(buildScientificNameIndex(request.taxa()));
        }

        if (request.includeCommonNameIndex()) {
            elements.addAll(buildCommonNameIndex(request.taxa()));
        }

        if (request.includeAppendix()) {
            elements.addAll(buildAppendix(request));
        }

        return List.copyOf(elements);
    }

    protected List<IPrintPubDocumentElement> buildContent(PrintPubDocumentRequest request) {

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        if (request.taxa().isEmpty()) {
            return elements;
        }

        elements.add(new PrintPubSectionHeaderElement("Taxonomic Hierarchy", 1));

        PrintPubCitationRegistry citations = new PrintPubCitationRegistry();

        for (PrintPubTaxonSummaryDTO dto : request.taxa()) {
            if (dto != null) {
                elements.addAll(renderTaxon(request, citations, dto));
            }
        }

        return elements;
    }

    protected List<IPrintPubDocumentElement> buildCommonNameIndex(List<PrintPubTaxonSummaryDTO> taxa) {

        if (taxa == null || taxa.isEmpty()) {
            return List.of();
        }

        List<String> commonNames = taxa.stream().filter(Objects::nonNull).filter(dto -> dto.commonNames != null)
                .flatMap(dto -> dto.commonNames.stream()).filter(StringUtils::isNotBlank).map(String::trim).distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();

        if (commonNames.isEmpty()) {
            return List.of();
        }

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.add(new PrintPubPageBreakElement());
        elements.add(new PrintPubSectionHeaderElement("Index to Common Names", 1));

        for (String commonName : commonNames) {
            elements.add(new PrintPubParagraphElement(commonName));
        }

        return elements;
    }

    protected List<IPrintPubDocumentElement> buildHeader(PrintPubDocumentRequest request) {

        return List.of(new PrintPubSectionHeaderElement(request.documentTitle(), 1),
                new PrintPubParagraphElement("Total Taxa: " + request.taxa().size()), new PrintPubPageBreakElement());
    }

    private List<IPrintPubDocumentElement> renderTaxon(PrintPubDocumentRequest request,
            PrintPubCitationRegistry citations, PrintPubTaxonSummaryDTO taxonDto) {

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.add(renderTaxonHeading(taxonDto));

        if (StringUtils.isNotBlank(taxonDto.typeSpecimenString)) {
            elements.add(new PrintPubParagraphElement(taxonDto.typeSpecimenString));
        }

        if (StringUtils.isNotBlank(taxonDto.typeStatementString)) {
            elements.add(new PrintPubParagraphElement(taxonDto.typeStatementString));
        }

        if (request.includeSynonyms()) {
            elements.addAll(renderSynonyms(request, citations, taxonDto));
        }

        elements.addAll(renderTaxonDetails(request, taxonDto));

        return elements;
    }

    private PrintPubTextRunElement renderTaxonHeading(PrintPubTaxonSummaryDTO taxonDto) {

        List<Run> runs = new ArrayList<>(runsFromTaggedNameForTitle(taxonDto.taggedNameList));

        if (StringUtils.isNotBlank(taxonDto.secReferenceCitation)) {
            runs.add(new Run(RunType.TEXT, ACC_SEC_MARKER + taxonDto.secReferenceCitation));
        }

        return new PrintPubTextRunElement(null, runs, PrintPubTextRunElement.PrintPubTextRole.TAXON_NAME);
    }

    private List<IPrintPubDocumentElement> renderSynonyms(PrintPubDocumentRequest request,
            PrintPubCitationRegistry citations, PrintPubTaxonSummaryDTO taxonDto) {

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        boolean oneLinePerHomotypicGroup = request.oneLinePerHomotypicGroup();

        if (taxonDto.homotypicSynonymGroup != null && taxonDto.homotypicSynonymGroup.synonyms != null) {

            List<Run> runs = new ArrayList<>();
            List<IPrintPubDocumentElement> additionalElements = new ArrayList<>();

            // The accepted name is already first in the homotypic group,
            // so all listed synonyms use the homotypic marker.
            boolean firstInGroup = false;

            for (PrintPubSynonymDTO synonym : taxonDto.homotypicSynonymGroup.synonyms) {

                if (synonym == null) {
                    continue;
                }

                SynonymModel model = renderSingleSynonym(request, citations, firstInGroup, synonym,
                        oneLinePerHomotypicGroup);

                runs.addAll(model.runs());
                additionalElements.addAll(model.additionalElements());
            }

            if (!runs.isEmpty()) {
                elements.add(new PrintPubTextRunElement(runs));
            }

            elements.addAll(additionalElements);
        }

        if (taxonDto.synonymGroups == null) {
            return elements;
        }

        for (PrintPubSynonymGroupDTO group : taxonDto.synonymGroups) {

            if (group == null || group.synonyms == null) {
                continue;
            }

            boolean firstInGroup = true;
            List<Run> runs = new ArrayList<>();
            List<IPrintPubDocumentElement> additionalElements = new ArrayList<>();

            for (PrintPubSynonymDTO synonym : group.synonyms) {

                if (synonym == null) {
                    continue;
                }

                SynonymModel model = renderSingleSynonym(request, citations, firstInGroup, synonym,
                        oneLinePerHomotypicGroup);

                runs.addAll(model.runs());
                additionalElements.addAll(model.additionalElements());

                firstInGroup = false;
            }

            if (!runs.isEmpty()) {
                elements.add(new PrintPubTextRunElement(runs));
            }

            elements.addAll(additionalElements);
        }

        return elements;
    }

    /**
     * Returns both the synonym runs and any separate model elements belonging to
     * the synonym, such as type-specimen paragraphs.
     */
    private SynonymModel renderSingleSynonym(PrintPubDocumentRequest request, PrintPubCitationRegistry citations,
            boolean firstInGroup, PrintPubSynonymDTO synonym, boolean oneLinePerHomotypicGroup) {
        String prefix;

        if (synonym.isInvalidDesignation) {
            prefix = INVALID_NAME_MARKER;
        } else if (firstInGroup) {
            prefix = SYNONYM_MARKER;
        } else {
            prefix = HOMOTYPIC_MARKER;
        }

        String suffix = "";

        if (request.includeSynonymConceptReferences() && StringUtils.isNotBlank(synonym.secReference)) {
            String citationSuffix = citations.incrementShortCitation(synonym.secReference);

            suffix = SYN_SEC_MARKER + synonym.secReference + citationSuffix;
        }

        boolean newLine = !firstInGroup && !oneLinePerHomotypicGroup;

        List<Run> runs = synonymRuns(synonym, prefix, suffix, newLine);

        List<IPrintPubDocumentElement> additionalElements = new ArrayList<>();

        if (StringUtils.isNotBlank(synonym.typeSpecimenString)) {
            additionalElements.add(new PrintPubParagraphElement(synonym.typeSpecimenString));
        }

        return new SynonymModel(runs, additionalElements);
    }

    private List<IPrintPubDocumentElement> renderTaxonDetails(PrintPubDocumentRequest request,
            PrintPubTaxonSummaryDTO dto) {

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        if (StringUtils.isNotBlank(dto.distributionString)) {
            elements.add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        if (dto.facts == null || dto.facts.isEmpty()) {
            return elements;
        }

        Map<PrintPubFeatureKey, List<PrintPubFactDTO>> groups =
                dto.facts.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                fact -> new PrintPubFeatureKey(
                                        fact.featureUuid,
                                        fact.label),
                                LinkedHashMap::new,
                                Collectors.toList()));

        for (Map.Entry<PrintPubFeatureKey, List<PrintPubFactDTO>> entry : groups.entrySet()) {

            PrintPubFeatureKey key = entry.getKey();
            List<PrintPubFactDTO> facts = entry.getValue();

            List<Run> combinedRuns = buildFactRuns(facts);

            if (combinedRuns.isEmpty()) {
                continue;
            }

            String label = StringUtils.defaultIfBlank(key.getLabel(), "Facts");

            elements.add(new PrintPubTextRunElement(label, combinedRuns,
                    PrintPubTextRunElement.PrintPubTextRole.FACT_GROUP));
        }

        return elements;
    }

    private List<Run> buildFactRuns(List<PrintPubFactDTO> facts) {

        List<Run> runs = new ArrayList<>();
        boolean first = true;

        for (PrintPubFactDTO fact : facts) {
            if (fact == null) {
                continue;
            }

            List<Run> factRuns = PrintPubNonNestedHtmlTokenConverter
                    .toRuns(PrintPubNonNestedHtmlTokenizer.tokenize(fact.text));

            boolean hasCitations = fact.citations != null && !fact.citations.isEmpty();

            if (factRuns.isEmpty() && !hasCitations) {
                continue;
            }

            if (!first) {
                runs.add(new Run(RunType.TEXT, " "));
            }

            first = false;
            runs.addAll(factRuns);

            if (hasCitations) {
                runs.add(new Run(RunType.TEXT, " [" + String.join("; ", fact.citations) + "]"));
            }
        }

        return runs;
    }

    protected List<IPrintPubDocumentElement> buildBibliography(List<Reference> bibliography) {

        if (bibliography == null || bibliography.isEmpty()) {
            return List.of();
        }

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.add(new PrintPubPageBreakElement());
        elements.add(new PrintPubSectionHeaderElement("Bibliography", 1));

        for (Reference reference : bibliography) {
            if (reference == null) {
                continue;
            }

            elements.add(new PrintPubParagraphElement(reference.getTitleCache()));
        }

        return elements;
    }

    protected List<IPrintPubDocumentElement> buildScientificNameIndex(List<PrintPubTaxonSummaryDTO> taxa) {

        List<String> names = scientificNames(taxa).toList();

        if (names.isEmpty()) {
            return List.of();
        }

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.add(new PrintPubPageBreakElement());
        elements.add(new PrintPubSectionHeaderElement("Index to Scientific Names", 1));

        for (String name : names) {
            elements.add(new PrintPubTextRunElement(List.of(new Run(RunType.ITALIC, name))));
        }

        return elements;
    }

    private Stream<String> scientificNames(List<PrintPubTaxonSummaryDTO> taxa) {

        if (taxa == null) {
            return Stream.empty();
        }

        return taxa.stream().filter(Objects::nonNull)
                .flatMap(taxon -> Stream.concat(Stream.ofNullable(taxon.scientificName), synonymScientificNames(taxon)))
                .filter(StringUtils::isNotBlank).map(String::trim).distinct().sorted(String.CASE_INSENSITIVE_ORDER);
    }

    private Stream<String> scientificNames(PrintPubExportState state) {

        return state.getTaxa().stream().filter(Objects::nonNull)
                .flatMap(taxon -> Stream.concat(Stream.ofNullable(taxon.scientificName), synonymScientificNames(taxon)))
                .filter(StringUtils::isNotBlank).map(String::trim).distinct().sorted(String.CASE_INSENSITIVE_ORDER);
    }

    private Stream<String> synonymScientificNames(PrintPubTaxonSummaryDTO taxon) {

        if (taxon.synonymGroups == null) {
            return Stream.empty();
        }

        return taxon.synonymGroups.stream().filter(Objects::nonNull).filter(group -> group.synonyms != null)
                .flatMap(group -> group.synonyms.stream()).filter(Objects::nonNull)
                .map(synonym -> synonym.scientificName);
    }

    protected List<IPrintPubDocumentElement> buildCommonNameIndex(PrintPubExportState state) {

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.add(new PrintPubPageBreakElement());
        elements.add(new PrintPubSectionHeaderElement("Index to Common Names", 1));

        state.getTaxa().stream().filter(Objects::nonNull).filter(dto -> dto.commonNames != null)
                .flatMap(dto -> dto.commonNames.stream()).filter(StringUtils::isNotBlank).map(String::trim)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(commonName -> elements.add(new PrintPubParagraphElement(commonName)));

        return elements;
    }

    protected List<IPrintPubDocumentElement> buildAppendix(PrintPubDocumentRequest request) {

        List<PrintPubTaxonSummaryDTO> taxa = request.taxa();

        if (!request.includeAppendix() || taxa.isEmpty()) {
            return List.of();
        }

        List<IPrintPubDocumentElement> rows = new ArrayList<>();

        for (PrintPubTaxonSummaryDTO dto : taxa) {
            if (dto == null) {
                continue;
            }

            if (!request.includeEmptyIds() && !hasAnySelectedIdentifier(dto, request)) {
                continue;
            }

            List<Run> runs = new ArrayList<>();
            List<Run> nameRuns = runsFromTaggedName(dto.taggedNameList);

            if (!nameRuns.isEmpty()) {
                runs.addAll(nameRuns);
            } else if (StringUtils.isNotBlank(dto.titleCache)) {
                runs.add(new Run(RunType.TEXT, dto.titleCache.trim()));
            }

            StringBuilder suffix = new StringBuilder();

            if (request.includeWfoId()) {
                appendAppendixField(suffix, "WFO", dto.wfoIds);
            }

            if (request.includeIpniId()) {
                appendAppendixField(suffix, "IPNI", dto.ipniIds);
            }

            if (request.includeProtologueUris()) {
                appendAppendixField(suffix, "URL", dto.links);
            }

            if (suffix.length() > 0) {
                runs.add(new Run(RunType.TEXT, "; " + suffix));
            }

            if (!runs.isEmpty()) {
                rows.add(new PrintPubTextRunElement(runs));
            }
        }

        if (rows.isEmpty()) {
            return List.of();
        }

        List<IPrintPubDocumentElement> elements = new ArrayList<>();

        elements.add(new PrintPubPageBreakElement());
        elements.add(new PrintPubSectionHeaderElement("Appendix: Digital Identifiers", 1));
        elements.addAll(rows);

        return elements;
    }

    private boolean hasAnySelectedIdentifier(PrintPubTaxonSummaryDTO dto, PrintPubDocumentRequest request) {

        if (dto == null) {
            return false;
        }

        return request.includeWfoId() && hasValues(dto.wfoIds) || request.includeIpniId() && hasValues(dto.ipniIds)
                || request.includeProtologueUris() && hasValues(dto.links);
    }

    private List<Run> runsFromTaggedName(List<TaggedText> taggedName) {

        List<Run> runs = new ArrayList<>();
        boolean first = true;

        if (taggedName == null) {
            return runs;
        }

        for (TaggedText taggedText : taggedName) {
            String text = taggedText.getText();

            if (StringUtils.isEmpty(text)) {
                continue;
            }

            if (!first && needsSpaceBefore(text)) {
                runs.add(new Run(RunType.TEXT, " "));
            }

            first = false;

            RunType type = taggedText.getType() == TagEnum.name ? RunType.ITALIC : RunType.TEXT;

            runs.add(new Run(type, text));
        }

        return runs;
    }

    private List<Run> runsFromTaggedNameForTitle(List<TaggedText> taggedName) {

        List<Run> runs = new ArrayList<>();
        boolean first = true;

        if (taggedName == null) {
            return runs;
        }

        for (TaggedText taggedText : taggedName) {
            String text = taggedText.getText();

            if (StringUtils.isEmpty(text)) {
                continue;
            }

            if (!first && needsSpaceBefore(text)) {
                runs.add(new Run(RunType.TEXT, " "));
            }

            first = false;

            RunType type = taggedText.getType() == TagEnum.name ? RunType.BOLD_ITALIC : RunType.BOLD;

            runs.add(new Run(type, text));
        }

        return runs;
    }

    private List<Run> synonymRuns(PrintPubSynonymDTO synonym, String prefix, String suffix, boolean newLine) {

        List<Run> runs = new ArrayList<>();

        runs.add(new Run(newLine ? RunType.LINE_BREAK : RunType.TEXT, newLine ? "" : " "));

        runs.add(new Run(RunType.TEXT, prefix));

        List<Run> nameRuns = runsFromTaggedName(synonym.taggedNameList);

        if (!nameRuns.isEmpty()) {
            runs.addAll(nameRuns);
        } else if (StringUtils.isNotBlank(synonym.titleCache)) {
            runs.add(new Run(RunType.TEXT, synonym.titleCache.trim()));
        }

        if (StringUtils.isNotEmpty(suffix)) {
            runs.add(new Run(RunType.TEXT, suffix));
        }

        return runs;
    }

    private boolean hasValues(List<String> values) {
        return values != null && values.stream().anyMatch(StringUtils::isNotBlank);
    }

    private void appendAppendixField(StringBuilder line, String label, List<String> values) {

        if (line.length() > 0) {
            line.append("; ");
        }

        line.append(label).append(": ").append(joinValuesOrDash(values));
    }

    private String joinValuesOrDash(List<String> values) {

        if (values == null) {
            return "--";
        }

        String result = values.stream().filter(StringUtils::isNotBlank).map(String::trim)
                .collect(Collectors.joining(", "));

        return result.isEmpty() ? "--" : result;
    }

    private boolean needsSpaceBefore(String text) {
        return !text.startsWith(",") && !text.startsWith(";") && !text.startsWith(")");
    }

    private record SynonymModel(List<Run> runs, List<IPrintPubDocumentElement> additionalElements) {

        private SynonymModel {
            runs = List.copyOf(runs);
            additionalElements = List.copyOf(additionalElements);
        }
    }
}
