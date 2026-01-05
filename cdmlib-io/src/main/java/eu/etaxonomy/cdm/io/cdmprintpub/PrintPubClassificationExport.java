/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cdmprintpub;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubSectionHeader;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubUnorderedListElement;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

@Component
public class PrintPubClassificationExport
		extends CdmExportBase<PrintPubExportConfigurator, PrintPubExportState, IExportTransformer, File> {

	private static final long serialVersionUID = 1L;

	@Autowired
	private ITaxonNodeService taxonNodeService;

	public PrintPubClassificationExport() {
		this.ioName = this.getClass().getSimpleName();
	}

	private class PrintPubContext {
		List<TaxonSummaryDTO> taxonList = new ArrayList<>();
		Map<UUID, Reference> referenceStore = new HashMap<>();

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
	}

	private static class TaxonSummaryDTO {
	    UUID uuid;
	    String titleCache;
	    int relativeDepth; // Changed from depthLevel to clarify it's relative
	    List<String> synonyms = new ArrayList<>();
	    List<String> commonNames = new ArrayList<>();
	    String distributionString;
	    List<String> simpleFacts = new ArrayList<>();
	    String secReferenceCitation;
	}

	@Override
	@Transactional(readOnly = true)
	protected void doInvoke(PrintPubExportState state) {
		IProgressMonitor monitor = state.getConfig().getProgressMonitor();
		PrintPubContext context = new PrintPubContext();

		try {
			// Phase 1: Data extraction within the active transaction
			monitor.subTask("Collecting taxonomic data...");
			TaxonNodeOutStreamPartitioner<PrintPubExportState> partitioner = TaxonNodeOutStreamPartitioner
					.NewInstance(this, state, state.getConfig().getTaxonNodeFilter(), 100, monitor, null);

			Integer referenceDepth = null;
			
			TaxonNode node = partitioner.next();
			while (node != null) {
	            if (referenceDepth == null) {
	                referenceDepth = calculateDepth(node);
	            }
	            
	            processNodeIntoContext(state, context, node, referenceDepth);
	            node = partitioner.next();
	        }

			// Phase 2: Building the document model from extracted DTOs
			monitor.subTask("Generating document layout...");
			generateDocumentLayout(state, context);

		} catch (Exception e) {
			state.getResult().addException(e, "Error during PrintPub export: " + e.getMessage());
		} finally {
			state.getProcessor().createFinalResult();
		}
	}

	private void processNodeIntoContext(PrintPubExportState state, PrintPubContext context, TaxonNode node, int referenceDepth) {
		if (node == null || node.getTaxon() == null)
			return;

		Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon(), Taxon.class);
		TaxonSummaryDTO dto = new TaxonSummaryDTO();
		dto.uuid = taxon.getUuid();
		dto.relativeDepth = calculateDepth(node) - referenceDepth;
		
		// Extract nomenclature information
		TaxonName name = HibernateProxyHelper.deproxy(taxon.getName(), TaxonName.class);
	    dto.titleCache = (name != null) ? name.getTitleCache() : taxon.getTitleCache();

		// Populate synonymy and capture secondary references for the bibliography
		if (state.getConfig().isDoSynonyms() && taxon.hasSynonyms()) {
			for (Synonym syn : taxon.getSynonyms()) {
				syn = CdmBase.deproxy(syn);
				dto.synonyms
						.add("= " + ((syn.getName() != null) ? syn.getName().getTitleCache() : syn.getTitleCache()));
				if (syn.getSec() != null) {
					context.addReference(HibernateProxyHelper.deproxy(syn.getSec(), Reference.class));
				}
			}
		}

		if (state.getConfig().isDoFactualData()) {
			extractDescriptionData(state, context, taxon, dto);
		}

		// Store taxon citation reference
		if (taxon.getSec() != null) {
			Reference ref = HibernateProxyHelper.deproxy(taxon.getSec(), Reference.class);
			context.addReference(ref);
			dto.secReferenceCitation = ref.getTitleCache();
		}

		context.addTaxon(dto);
	}

	private void extractDescriptionData(PrintPubExportState state, PrintPubContext context, Taxon taxon,
			TaxonSummaryDTO dto) {
		for (TaxonDescription desc : taxon.getDescriptions()) {
			if (!state.getConfig().isIncludeUnpublishedFacts() && !desc.isPublish())
				continue;

			for (DescriptionElementBase element : desc.getElements()) {
				element = CdmBase.deproxy(element);
				Feature feature = element.getFeature();

				// Handle common names
				if (feature.equals(Feature.COMMON_NAME()) && element instanceof CommonTaxonName) {
					CommonTaxonName ctn = (CommonTaxonName) element;
					dto.commonNames.add(ctn.getName()
							+ (ctn.getLanguage() != null ? " [" + ctn.getLanguage().getLabel() + "]" : ""));
				}
				// Handle geographic distribution
				else if (feature.equals(Feature.DISTRIBUTION()) && element instanceof Distribution) {
					Distribution d = (Distribution) element;
					if (d.getArea() != null) {
						dto.distributionString = (dto.distributionString == null) ? d.getArea().getLabel()
								: dto.distributionString + ", " + d.getArea().getLabel();
					}
				}
				// Handle descriptive text facts
				else if (element instanceof TextData) {
					String text = ((TextData) element).getText(Language.DEFAULT());
					if (text != null)
						dto.simpleFacts.add("**" + feature.getLabel() + "**: " + text);
				}
			}
		}
	}

	private void generateDocumentLayout(PrintPubExportState state, PrintPubContext context) {
		// Document header and metadata
		state.getProcessor().add(new PrintPubSectionHeader(state.getConfig().getDocumentTitle(), 1));
		state.getProcessor().add(new PrintPubParagraphElement("Total Taxa: " + context.taxonList.size()));
		state.getProcessor().add(new PrintPubPageBreakElement());

		// Taxonomic treatment blocks
		for (TaxonSummaryDTO dto : context.taxonList) {
			int headerLevel = Math.min(dto.relativeDepth + 2, 6);
			state.getProcessor().add(new PrintPubSectionHeader(dto.titleCache, headerLevel));
			
			if (!dto.synonyms.isEmpty()) {
				PrintPubUnorderedListElement list = new PrintPubUnorderedListElement();
				dto.synonyms.forEach(list::addItem);
				state.getProcessor().add(list);
			}

			if (dto.distributionString != null) {
				state.getProcessor().add(new PrintPubParagraphElement("**Distribution:** " + dto.distributionString));
			}

			dto.simpleFacts.forEach(fact -> state.getProcessor().add(new PrintPubParagraphElement(fact)));
		}

		// Bibliography section
		if (!context.referenceStore.isEmpty()) {
			state.getProcessor().add(new PrintPubPageBreakElement());
			state.getProcessor().add(new PrintPubSectionHeader("Bibliography", 1));
			for (Reference ref : context.getSortedBibliography()) {
				state.getProcessor().add(new PrintPubParagraphElement(ref.getTitleCache()));
			}
		}
	}

	private int calculateDepth(TaxonNode node) {
		int depth = 1;
		TaxonNode parent = node.getParent();
		while (parent != null) {
			depth++;
			parent = parent.getParent();
		}
		return depth;
	}

	@Override
	protected boolean doCheck(PrintPubExportState state) {
		return state.getConfig().getDestination() != null;
	}

	@Override
	protected boolean isIgnore(PrintPubExportState state) {
		return false;
	}
}