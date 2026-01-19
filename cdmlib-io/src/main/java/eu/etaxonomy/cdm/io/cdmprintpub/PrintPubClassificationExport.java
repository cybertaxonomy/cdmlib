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
                }

                TaxonSummaryDTO dto = mapper.mapNodeToDto(node, referenceDepth, state, context);
                if (dto != null) {
                    context.addTaxon(dto);
                }

                node = partitioner.next();
            }

            if (monitor.isCanceled()) {
                return;
            }

            monitor.subTask("Generating document layout (PDF/HTML)...");
            builder.buildLayout(state, context);

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
