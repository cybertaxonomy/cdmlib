/**
* Copyright (C) 2025 EDIT
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.Reference;

class PrintPubContext {
	protected List<TaxonSummaryDTO> taxonList = new ArrayList<>();
	protected Map<UUID, Reference> referenceStore = new HashMap<>();

	public void addTaxon(TaxonSummaryDTO dto) {
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

	static class SynonymGroupDTO {
		boolean isHomotypic; // True = '≡', False = '='
		List<SynonymDTO> synonyms = new ArrayList<>();
	}

	protected static class SynonymDTO {
		String titleCache;
		String secReference;
		String typeSpecimenString;
		String typeStatementString;
	}

	protected static class FactDTO {
		String label;
		String text;
		String citation;
	}

	public static class TaxonSummaryDTO {
		UUID uuid;
		String titleCache;
		int relativeDepth;

		String typeSpecimenString;
		String typeStatementString;

		List<SynonymGroupDTO> synonymGroups = new ArrayList<>();

		List<FactDTO> facts = new ArrayList<>();

		List<String> commonNames = new ArrayList<>();
		String distributionString;
		String secReferenceCitation;
	}
}