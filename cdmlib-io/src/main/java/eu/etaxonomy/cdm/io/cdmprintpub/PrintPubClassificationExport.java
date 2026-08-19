/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cdmprintpub;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private PrintPubDtoMapper mapper;

    @Autowired
    private PrintPubDocumentBuilder builder;

    @Autowired
    private PrintPubFeatureOrderIndexService featureOrderIndexService;

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
                logger.info("PrintPub export cancelled before initialization");
                return;
            }
            
            state.clearCollectedReferences();

            // --------------------------------------------------
            // Initialize feature ordering
            // --------------------------------------------------
            initializeFeatureOrdering(state, monitor);

            if (monitor.isCanceled()) {
                logger.info("PrintPub export cancelled after feature ordering initialization");
                return;
            }

            // --------------------------------------------------
            // Main taxon stream
            // --------------------------------------------------
            monitor.subTask("Initializing data stream...");

            TaxonNodeOutStreamPartitioner<PrintPubExportState> partitioner =
                    TaxonNodeOutStreamPartitioner.NewInstance(
                            this,
                            state,
                            state.getConfig().getTaxonNodeFilter(),
                            100,
                            monitor,
                            null
                    );

            Integer referenceDepth = null;
            TaxonNode node = partitioner.next();
            int nodesProcessed = 0;

            while (node != null) {

                if (monitor.isCanceled()) {
                    logger.info("PrintPub export cancelled during taxon processing after {} nodes", nodesProcessed);
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
                }

                PrintPubTaxonSummaryDTO dto = mapper.mapNodeToDto(node, referenceDepth, state);

                if (nodesProcessed == 1
                        && dto != null
                        && dto.titleCache != null) { 
                    state.getConfig().setDocumentTitle(dto.titleCache);
                }
                
                if (dto != null) {
                    state.addTaxon(dto);
                }

                node = partitioner.next();
            }

            logger.info("Processed {} taxon nodes for PrintPub export", nodesProcessed);

            if (monitor.isCanceled()) {
                logger.info("PrintPub export cancelled before document layout generation");
                return;
            }

            // --------------------------------------------------
            // Build layout
            // --------------------------------------------------
            monitor.subTask("Generating document layout (PDF/HTML)...");
            builder.buildLayout(state);
            monitor.worked(10);

            logger.info("PrintPub document layout generated successfully");

        } catch (Exception e) {
            state.getResult().addException(e, "Error during PrintPub export: " + e.getMessage());
            monitor.warning("Export failed: " + e.getMessage(), e);
            logger.error("PrintPub export failed", e);

        } finally {
            monitor.done();
            state.getProcessor().createFinalResult();
        }
    }

    private void initializeFeatureOrdering(PrintPubExportState state, IProgressMonitor monitor) {
        UUID featureTreeUuid = state.getConfig().getFeatureTreeUuid();

        if (featureTreeUuid == null) {
            state.setFeatureOrderIndex(new HashMap<UUID, Integer>());
            logger.info("No feature tree configured; using alphabetical feature ordering");
            return;
        }

        try {
            monitor.subTask("Initializing feature ordering...");

            Map<UUID, Integer> featureIndex =
                    featureOrderIndexService.buildFeatureOrderIndex(featureTreeUuid);

            state.setFeatureOrderIndex(featureIndex);

            logger.info(
                    "Feature ordering initialized from tree {} with {} indexed features",
                    featureTreeUuid,
                    featureIndex.size()
            );

            if (featureIndex.isEmpty()) {
                logger.warn(
                        "Feature tree {} produced an empty feature order index; alphabetical fallback will be used",
                        featureTreeUuid
                );
            }

        } catch (Exception e) {
            state.setFeatureOrderIndex(new HashMap<UUID, Integer>());

            monitor.warning(
                    "Could not initialize feature ordering; falling back to alphabetical order: " + e.getMessage(),
                    e
            );

            logger.warn(
                    "Could not initialize feature ordering for tree {}; using alphabetical fallback",
                    featureTreeUuid,
                    e
            );
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