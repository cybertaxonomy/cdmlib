package eu.etaxonomy.cdm.io.cdmprintpub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportStateBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author veldmap97
 * @date Dec 2, 2025
 */
public class PrintPubExportState extends TaxonTreeExportStateBase<PrintPubExportConfigurator, PrintPubExportState> {

    private PrintPubDocumentModel documentModel;

    private PrintPubExportResultProcessor processor;
    private ExportResult result;

    private TaxonBase<?> currentTaxon;

    private Set<String> printedElementIds = new HashSet<>();

    private Map<String, Integer> shortCitationCounter = new HashMap<>();

    protected PrintPubExportState(PrintPubExportConfigurator config) {
        super(config);
        this.result = ExportResult.NewInstance(config.getResultType());
        this.documentModel = new PrintPubDocumentModel();
        this.processor = new PrintPubExportResultProcessor(this);
    }

    @Override
    public ExportResult getResult() {
        return result;
    }

    @Override
    public void setResult(ExportResult result) {
        this.result = result;
    }

    public PrintPubDocumentModel getDocumentModel() {
        return documentModel;
    }

    public PrintPubExportResultProcessor getProcessor() {
        return processor;
    }

    public TaxonBase<?> getCurrentTaxon() {
        return currentTaxon;
    }

    public void setCurrentTaxon(TaxonBase<?> currentTaxon) {
        this.currentTaxon = currentTaxon;
    }

    public boolean hasPrinted(UUID uuid) {
        return printedElementIds.contains(uuid.toString());
    }

    public void markAsPrinted(UUID uuid) {
        printedElementIds.add(uuid.toString());
    }


    public String incrementShortCitation(String shortCitation) {
        Integer counter = shortCitationCounter.getOrDefault(shortCitation, 0);
        shortCitationCounter.put(shortCitation, counter + 1);
        return counterToString(counter);
    }

    private String counterToString(Integer counter) {
        if (counter == 0) {
            return "";
        }
        int finalCounter = 'a' + counter - 1;
        if (finalCounter >= 'j') {
            finalCounter++;
        }
        return Character.toString((char) (finalCounter));
    }
}