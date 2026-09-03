package eu.etaxonomy.cdm.io.print.mapper;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.print.PrintPubExportState;
import eu.etaxonomy.cdm.io.print.dto.PrintPubReferenceEntryDTO.PrintPubReferenceSourceType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;

@Component
public class PrintPubBibliographyCollector {

    public void collectAcceptedNameSources(PrintPubExportState state, TaxonName name) {

        collectNameRelationshipSources(state, name, true);
        collectTypeDesignationSources(state, name, true);
    }

    public void collectSynonymNameSources(PrintPubExportState state, TaxonName name) {

        collectNameRelationshipSources(state, name, false);
        collectTypeDesignationSources(state, name, false);
    }

    private void collectTypeDesignationSources(PrintPubExportState state, TaxonName name, boolean acceptedName) {

        PrintPubReferenceSourceType designationCategory = acceptedName
                ? PrintPubReferenceSourceType.ACCEPTED_NAME_TYPE_DESIGNATION_SOURCE
                : PrintPubReferenceSourceType.SYNONYM_NAME_TYPE_DESIGNATION_SOURCE;

        PrintPubReferenceSourceType otherCategory = acceptedName
                ? PrintPubReferenceSourceType.ACCEPTED_NAME_TYPE_DESIGNATION_OTHER_SOURCE
                : PrintPubReferenceSourceType.SYNONYM_NAME_TYPE_DESIGNATION_OTHER_SOURCE;

        for (TypeDesignationBase<?> designation : name.getTypeDesignations()) {

            designation = CdmBase.deproxy(designation);

            NamedSource designationSource = designation.getDesignationSource();

            collectSourceReference(state, designationSource, designationCategory);

            for (OriginalSourceBase source : designation.getSources()) {

                collectSourceReference(state, source, otherCategory);
            }
        }
    }

    private void collectNameRelationshipSources(PrintPubExportState state, TaxonName name, boolean acceptedName) {

        PrintPubReferenceSourceType category = acceptedName
                ? PrintPubReferenceSourceType.ACCEPTED_NAME_RELATIONSHIP_SOURCE
                : PrintPubReferenceSourceType.SYNONYM_NAME_RELATIONSHIP_SOURCE;

        Set<NameRelationship> processed = new HashSet<NameRelationship>();

        collectNameRelationships(state, name.getRelationsFromThisName(), category, processed);

        collectNameRelationships(state, name.getRelationsToThisName(), category, processed);
    }

    private void collectNameRelationships(PrintPubExportState state, Set<NameRelationship> relationships,
            PrintPubReferenceSourceType category, Set<NameRelationship> processed) {

        if (relationships == null) {
            return;
        }

        for (NameRelationship relationship : relationships) {

            if (relationship == null || !processed.add(relationship)) {
                continue;
            }

            relationship = CdmBase.deproxy(relationship);

            collectSourceReference(state, relationship.getSource(), category);

        }
    }

    private void collectSourceReference(PrintPubExportState state, OriginalSourceBase source,
            PrintPubReferenceSourceType category) {

        if (source == null || source.getCitation() == null) {
            return;
        }

        Reference ref = HibernateProxyHelper.deproxy(source.getCitation());

        state.addReference(ref, category);
    }
}
