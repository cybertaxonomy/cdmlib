package eu.etaxonomy.cdm.io.print;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.io.print.dto.PrintPubReferenceEntryDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubReferenceEntryDTO.PrintPubReferenceSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;

public class PrintPubReferenceCollector {

    private final Map<UUID, PrintPubReferenceEntryDTO> references =
            new HashMap<>();

    public void add(
            Reference reference,
            PrintPubReferenceSourceType sourceType) {

        if (reference == null || reference.getUuid() == null) {
            return;
        }

        PrintPubReferenceEntryDTO entry =
                references.computeIfAbsent(
                        reference.getUuid(),
                        uuid -> new PrintPubReferenceEntryDTO(reference));

        entry.addSourceType(sourceType);
    }

    public List<Reference> getSortedReferences() {

        List<Reference> result = new ArrayList<>();

        for (PrintPubReferenceEntryDTO entry : references.values()) {
            result.add(entry.getReference());
        }

        result.sort(Comparator.comparing(
                Reference::getTitleCache,
                Comparator.nullsLast(
                        String.CASE_INSENSITIVE_ORDER)));

        return List.copyOf(result);
    }

    public boolean isEmpty() {
        return references.isEmpty();
    }
}