package eu.etaxonomy.cdm.io.cdmprintpub;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.TaxonSummaryDTO;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

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
        PrintPubContext context = new PrintPubContext();

        try {
            // 1. Start the main task.
            // We use 'UNKNOWN' total work because we don't know how many nodes are in the stream
            // without running a heavy count query first. This sets the bar to "indeterminate" mode.
            monitor.beginTask("Exporting Classification to Print/Pub", IProgressMonitor.UNKNOWN);

            if (monitor.isCanceled()) {
                return;
            }

            monitor.subTask("Initializing data stream...");

            // Note: The partitioner might use the monitor to tick during database fetches
            TaxonNodeOutStreamPartitioner<PrintPubExportState> partitioner = TaxonNodeOutStreamPartitioner
                    .NewInstance(this, state, state.getConfig().getTaxonNodeFilter(), 100, monitor, null);

            Integer referenceDepth = null;
            TaxonNode node = partitioner.next();

            int nodesProcessed = 0;

            // --- DATA COLLECTION PHASE ---
            while (node != null) {
                // 2. CHECK FOR CANCELLATION
                // This is crucial. If the user clicks cancel, we must stop the loop.
                if (monitor.isCanceled()) {
                    return;
                }

                // 3. PROVIDE FEEDBACK
                // Update the subtask to show the user what is happening.
                // We use a counter to avoid updating the UI too rapidly (flickering).
                nodesProcessed++;
                if (nodesProcessed % 10 == 0) {
                    String nodeLabel = (node.getTaxon() != null && node.getTaxon().getName() != null)
                            ? node.getTaxon().getName().getTitleCache()
                            : "Node ID: " + node.getId();
                    monitor.subTask("Processing: " + nodeLabel);
                }

                // Tick the monitor to show activity
                monitor.worked(1);

                // --- LOGIC ---
                if (referenceDepth == null) {
                    referenceDepth = mapper.calculateDepth(node);
                }

                TaxonSummaryDTO dto = mapper.mapNodeToDto(node, referenceDepth, state, context);
                if (dto != null) {
                    context.addTaxon(dto);
                }

                node = partitioner.next();
            }

            // --- LAYOUT PHASE ---
            if (monitor.isCanceled()) {
                return;
            }

            monitor.subTask("Generating document layout (PDF/HTML)...");
            builder.buildLayout(state, context);

            monitor.worked(10); // Final tick
        } catch (Exception e) {
            state.getResult().addException(e, "Error during PrintPub export: " + e.getMessage());
            // It is good practice to log the error to the monitor warning as well
            monitor.warning("Export failed: " + e.getMessage(), e);
        } finally {
            // 4. CLEANUP
            // Always call done(). This tells the UI the process is finished/closed.
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
