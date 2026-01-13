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
            monitor.subTask("Collecting taxonomic data...");
            TaxonNodeOutStreamPartitioner<PrintPubExportState> partitioner = TaxonNodeOutStreamPartitioner
                    .NewInstance(this, state, state.getConfig().getTaxonNodeFilter(), 100, monitor, null);

            Integer referenceDepth = null;
            TaxonNode node = partitioner.next();

            while (node != null) {
                if (referenceDepth == null) {
                    referenceDepth = mapper.calculateDepth(node);
                }

                TaxonSummaryDTO dto = mapper.mapNodeToDto(node, referenceDepth, state, context);
                if (dto != null) {
                    context.addTaxon(dto);
                }

                node = partitioner.next();
            }

            monitor.subTask("Generating document layout...");
            builder.buildLayout(state, context);

        } catch (Exception e) {
            state.getResult().addException(e, "Error during PrintPub export: " + e.getMessage());
        } finally {
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