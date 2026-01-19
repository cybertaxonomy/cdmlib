package eu.etaxonomy.cdm.io.cdmprintpub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainerFormatter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.FactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.SynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.SynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.TaxonSummaryDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

@Component
public class PrintPubDtoMapper {

    public TaxonSummaryDTO mapNodeToDto(TaxonNode node, int referenceDepth, PrintPubExportState state,
            PrintPubContext context) {
        if (node == null || node.getTaxon() == null) {
            return null;
        }

        Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon(), Taxon.class);
        TaxonSummaryDTO dto = new TaxonSummaryDTO();
        dto.uuid = taxon.getUuid();
        dto.relativeDepth = calculateDepth(node) - referenceDepth;

        TaxonName name = HibernateProxyHelper.deproxy(taxon.getName(), TaxonName.class);
        dto.titleCache = (name != null) ? name.getTitleCache() : taxon.getTitleCache();

        if (name != null) {
            extractTypeData(name, dto);
        }

        if (state.getConfig().isDoSynonyms()) {
            extractSynonymGroups(state, context, taxon, dto);
        }

        if (state.getConfig().isDoFactualData()) {
            extractDescriptionData(state, context, taxon, dto);
        }

        if (taxon.getSec() != null) {
            Reference ref = HibernateProxyHelper.deproxy(taxon.getSec(), Reference.class);
            context.addReference(ref);
            dto.secReferenceCitation = ref.getTitleCache();
        }

        return dto;
    }

    private void extractSynonymGroups(PrintPubExportState state, PrintPubContext context, Taxon taxon,
            TaxonSummaryDTO dto) {
        HomotypicalGroup acceptedGroup = taxon.getHomotypicGroup();
        List<Synonym> homotypicSynonyms = taxon.getSynonymsInGroup(acceptedGroup);

        if (!homotypicSynonyms.isEmpty()) {
            SynonymGroupDTO homotypicGroupDTO = new SynonymGroupDTO();
            homotypicGroupDTO.isHomotypic = true;
            for (Synonym syn : homotypicSynonyms) {
                homotypicGroupDTO.synonyms.add(createSynonymDTO(state, context, syn));
            }
            dto.synonymGroups.add(homotypicGroupDTO);
        }

        List<HomotypicalGroup> heteroGroups = taxon.getHeterotypicSynonymyGroups();
        for (HomotypicalGroup group : heteroGroups) {
            List<Synonym> groupSynonyms = taxon.getSynonymsInGroup(group);
            if (!groupSynonyms.isEmpty()) {
                SynonymGroupDTO heteroGroupDTO = new SynonymGroupDTO();
                heteroGroupDTO.isHomotypic = false;
                for (Synonym syn : groupSynonyms) {
                    heteroGroupDTO.synonyms.add(createSynonymDTO(state, context, syn));
                }
                dto.synonymGroups.add(heteroGroupDTO);
            }
        }
    }

    private SynonymDTO createSynonymDTO(PrintPubExportState state, PrintPubContext context, Synonym syn) {
        syn = CdmBase.deproxy(syn);
        SynonymDTO synDTO = new SynonymDTO();

        TaxonName synName = HibernateProxyHelper.deproxy(syn.getName(), TaxonName.class);
        synDTO.titleCache = (synName != null) ? synName.getTitleCache() : syn.getTitleCache();

        if (syn.getSec() != null) {
            Reference ref = HibernateProxyHelper.deproxy(syn.getSec(), Reference.class);
            context.addReference(ref);
            synDTO.secReference = ref.getTitleCache();
        }

        if (synName != null) {
            TaxonSummaryDTO tempDto = new TaxonSummaryDTO();
            extractTypeData(synName, tempDto);
            synDTO.typeSpecimenString = tempDto.typeSpecimenString;
            synDTO.typeStatementString = tempDto.typeStatementString;
        }

        return synDTO;
    }

    private void extractTypeData(TaxonName name, TaxonSummaryDTO dto) {
        Set<TypeDesignationBase> designations = name.getTypeDesignations();
        List<SpecimenTypeDesignation> specimenTypes = new ArrayList<>();
        List<TextualTypeDesignation> textualTypes = new ArrayList<>();

        for (TypeDesignationBase<?> design : designations) {
            if (design instanceof SpecimenTypeDesignation) {
                specimenTypes.add((SpecimenTypeDesignation) design);
            } else if (design instanceof TextualTypeDesignation) {
                textualTypes.add((TextualTypeDesignation) design);
            }
        }

        if (!specimenTypes.isEmpty()) {
            try {
                TypeDesignationGroupContainer container = new TypeDesignationGroupContainer(specimenTypes, name, null);
                dto.typeSpecimenString = new TypeDesignationGroupContainerFormatter().withStartingTypeLabel(true)
                        .toTaggedText(container).toString();
            } catch (Exception e) {
                dto.typeSpecimenString = "Error retrieving type data: " + e.getMessage();
            }
        }

        if (!textualTypes.isEmpty()) {
            dto.typeStatementString = textualTypes.stream().map(t -> t.getPreferredText(Language.DEFAULT()))
                    .collect(Collectors.joining("; "));
        }
    }

    private void extractDescriptionData(PrintPubExportState state, PrintPubContext context, Taxon taxon,
            TaxonSummaryDTO dto) {
        for (TaxonDescription desc : taxon.getDescriptions()) {
            if (!state.getConfig().isIncludeUnpublishedFacts() && !desc.isPublish()) {
                continue;
            }

            for (DescriptionElementBase element : desc.getElements()) {
                element = CdmBase.deproxy(element);
                Feature feature = element.getFeature();

                if (feature.equals(Feature.COMMON_NAME()) && element instanceof CommonTaxonName) {
                    CommonTaxonName ctn = (CommonTaxonName) element;
                    dto.commonNames.add(ctn.getName()
                            + (ctn.getLanguage() != null ? " [" + ctn.getLanguage().getLabel() + "]" : ""));
                } else if (feature.equals(Feature.DISTRIBUTION()) && element instanceof Distribution) {
                    Distribution d = (Distribution) element;
                    if (d.getArea() != null) {
                        dto.distributionString = (dto.distributionString == null) ? d.getArea().getLabel()
                                : dto.distributionString + ", " + d.getArea().getLabel();
                    }
                } else if (element instanceof TextData) {
                    String text = ((TextData) element).getText(Language.DEFAULT());
                    if (text != null) {
                        FactDTO fact = new FactDTO();
                        fact.label = feature.getLabel();
                        fact.text = text;

                        for (DescriptionElementSource source : element.getSources()) {
                            if (source.getCitation() != null) {
                                Reference ref = HibernateProxyHelper.deproxy(source.getCitation(), Reference.class);
                                context.addReference(ref);
                                String shortCit = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(ref, null);
                                fact.citation = (fact.citation == null) ? shortCit : fact.citation + "; " + shortCit;
                            }
                        }
                        dto.facts.add(fact);
                    }
                }
            }
        }
    }

    public int calculateDepth(TaxonNode node) {
        String treeIndex = node.treeIndex();

        if (treeIndex != null && !treeIndex.isEmpty()) {
            String[] segments = treeIndex.split("#");
            int depth = 0;
            for (String segment : segments) {
                if (!segment.isEmpty()) {
                    depth++;
                }
            }
            return depth;
        }

        int depth = 1;
        TaxonNode parent = node.getParent();
        while (parent != null) {
            depth++;
            parent = parent.getParent();
        }
        return depth;
    }
}