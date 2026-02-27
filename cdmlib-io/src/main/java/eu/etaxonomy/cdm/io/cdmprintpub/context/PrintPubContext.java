/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cdmprintpub.context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.Reference;

public class PrintPubContext {
	public List<PrintPubTaxonSummaryDTO> taxonList = new ArrayList<>();
	public Map<UUID, Reference> referenceStore = new HashMap<>();

	public void addTaxon(PrintPubTaxonSummaryDTO dto) {
		taxonList.add(dto);
	}

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
}