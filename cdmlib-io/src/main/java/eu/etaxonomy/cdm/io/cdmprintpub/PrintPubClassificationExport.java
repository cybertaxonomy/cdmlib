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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.cdmprintpub.documentBuilder.PrintPubDocumentBuilder;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.mapper.PrintPubDtoMapper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Entry point for the Print Publication export.
 *
 * Streams {@link eu.etaxonomy.cdm.model.taxon.TaxonNode} objects from the
 * classification tree, maps them into DTOs, and accumulates them in the export
 * state. Manages progress reporting, cancellation handling, and the main taxon
 * traversal loop. Triggers document layout creation via the document builder
 * and final result generation.
 */

@Component
public class PrintPubClassificationExport
		extends CdmExportBase<PrintPubExportConfigurator, PrintPubExportState, IExportTransformer, File> {

	private static final long serialVersionUID = 1L;

	@Autowired
	private PrintPubDtoMapper mapper;
	@Autowired
	private PrintPubDocumentBuilder builder;

	public PrintPubClassificationExport() {
		this.ioName = this.getClass().getSimpleName();
	}

	@Override
	@Transactional(readOnly = true)
	protected void doInvoke(PrintPubExportState state) {
		IProgressMonitor monitor = state.getConfig().getProgressMonitor();

		try {

			monitor.beginTask("Exporting Classification to Print/Pub", IProgressMonitor.UNKNOWN);

			if (monitor.isCanceled()) {
				return;
			}

			monitor.subTask("Initializing data stream...");

			TaxonNodeOutStreamPartitioner<PrintPubExportState> partitioner = TaxonNodeOutStreamPartitioner
					.NewInstance(this, state, state.getConfig().getTaxonNodeFilter(), 100, monitor, null);

			Integer referenceDepth = null;
			TaxonNode node = partitioner.next();

			int nodesProcessed = 0;

			while (node != null) {

				if (monitor.isCanceled()) {
					return;
				}

				nodesProcessed++;
				if (nodesProcessed % 10 == 0) {
					String nodeLabel = (node.getTaxon() != null && node.getTaxon().getName() != null)
							? node.getTaxon().getName().getTitleCache()
							: "Node ID: " + node.getId();
					monitor.subTask("Processing: " + nodeLabel);
				}

				monitor.worked(1);

				if (referenceDepth == null) {
					referenceDepth = mapper.calculateDepth(node);

					if (node.getTaxon() != null && node.getTaxon().getName() != null) {
						state.getConfig().setDocumentTitle(node.getTaxon().getName().getTitleCache());
					}
				}

				PrintPubTaxonSummaryDTO dto = mapper.mapNodeToDto(node, referenceDepth, state);
				if (dto != null) {
					state.addTaxon(dto);
				}

				node = partitioner.next();
			}

			if (monitor.isCanceled()) {
				return;
			}

			monitor.subTask("Generating document layout (PDF/HTML)...");
			builder.buildLayout(state);

			monitor.worked(10);
		} catch (Exception e) {
			state.getResult().addException(e, "Error during PrintPub export: " + e.getMessage());
			monitor.warning("Export failed: " + e.getMessage(), e);
		} finally {
			monitor.done();
			state.getProcessor().createFinalResult();
		}
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
