/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.cdmprintpub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.model.PrintPubDocumentModel;
import eu.etaxonomy.cdm.io.cdmprintpub.render.PrintPubExportResultProcessor;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportStateBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Central state container for the Print/Publication export.
 *
 * Collects taxon DTOs, bibliographic references, rendering metadata, and the
 * evolving document model during export execution. Also coordinates
 * deduplication, citation disambiguation, and export result creation.
 */
public class PrintPubExportState extends TaxonTreeExportStateBase<PrintPubExportConfigurator, PrintPubExportState> {

	// ======================
	// Collected export output (formerly PrintPubContext)
	// ======================

	private final List<PrintPubTaxonSummaryDTO> taxonList = new ArrayList<>();
	private final Map<UUID, Reference> referenceStore = new HashMap<>();


	// ======================
    // Sorting
    // ======================

	private transient Map<UUID, Integer> featureOrderIndex;

	// ======================
	// Export lifecycle
	// ======================

	private final PrintPubDocumentModel documentModel;
	private final PrintPubExportResultProcessor processor;
	private ExportResult result;

	private TaxonBase<?> currentTaxon;

	// ======================
	// Rendering / bookkeeping helpers
	// ======================

	private final Set<String> printedElementIds = new HashSet<>();
	private final Map<String, Integer> shortCitationCounter = new HashMap<>();

	// ======================
	// Constructor
	// ======================

	public PrintPubExportState(PrintPubExportConfigurator config) {
		super(config);
		this.result = ExportResult.NewInstance(config.getResultType());
		this.documentModel = new PrintPubDocumentModel();
		this.processor = new PrintPubExportResultProcessor(this);
	}

	// ======================
	// ExportResult handling
	// ======================

	@Override
	public ExportResult getResult() {
		return result;
	}

	@Override
	public void setResult(ExportResult result) {
		this.result = result;
	}

	// ======================
	// Document / processor access
	// ======================

	public PrintPubDocumentModel getDocumentModel() {
		return documentModel;
	}

	public PrintPubExportResultProcessor getProcessor() {
		return processor;
	}

	// ======================
	// Taxon traversal context
	// ======================

	public TaxonBase<?> getCurrentTaxon() {
		return currentTaxon;
	}

	public void setCurrentTaxon(TaxonBase<?> currentTaxon) {
		this.currentTaxon = currentTaxon;
	}

    // ======================
    // Sorting
    // ======================

    public Map<UUID, Integer> getFeatureOrderIndex() {
        return featureOrderIndex;
    }

    public void setFeatureOrderIndex(Map<UUID, Integer> featureOrderIndex) {
        this.featureOrderIndex = featureOrderIndex;
    }


	// ======================
	// Collected taxa
	// ======================

	public void addTaxon(PrintPubTaxonSummaryDTO dto) {
		if (dto != null) {
			taxonList.add(dto);
		}
	}

	public List<PrintPubTaxonSummaryDTO> getTaxa() {
		return taxonList;
	}

	// ======================
	// Bibliographic references
	// ======================

	public void addReference(Reference ref) {
		if (ref != null) {
			referenceStore.putIfAbsent(ref.getUuid(), ref);
		}
	}

	public List<Reference> getSortedBibliography() {
		List<Reference> refs = new ArrayList<>(referenceStore.values());
		refs.sort(Comparator.comparing(Reference::getTitleCache, Comparator.nullsLast(String::compareTo)));
		return refs;
	}

	// ======================
	// Rendering helpers
	// ======================

	public boolean hasPrinted(UUID uuid) {
		return printedElementIds.contains(uuid.toString());
	}

	public void markAsPrinted(UUID uuid) {
		printedElementIds.add(uuid.toString());
	}

	// ======================
	// Short citation disambiguation
	// ======================

	public String incrementShortCitation(String shortCitation) {
		Integer counter = shortCitationCounter.getOrDefault(shortCitation, 0);
		shortCitationCounter.put(shortCitation, counter + 1);
		return counterToString(counter);
	}

	// ======================
	// Additional Methods
	// ======================

	private String counterToString(Integer counter) {
		if (counter == 0) {
			return "";
		}
		int finalCounter = 'a' + counter - 1;
		if (finalCounter >= 'j') { // skip 'j'
			finalCounter++;
		}
		return Character.toString((char) finalCounter);
	}

	public void clearCollectedReferences() {
		referenceStore.clear();
	}

}