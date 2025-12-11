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

    // 1. The Output: Instead of 20 distinct Maps, we just hold the Document Model.
    private PrintPubDocumentModel documentModel;

    // 2. The Logic: Your processor
    private PrintPubExportResultProcessor processor;
    private ExportResult result;

    // 3. Context: Where are we currently?
    private TaxonBase<?> currentTaxon;

    // 4. Lightweight Deduplication: Just store UUIDs of things we've already
    // "printed"
    private Set<String> printedElementIds = new HashSet<>();

    // 5. Business Logic Helper: (Keep this if you need unique citations like
    // "Miller 1997a")
    private Map<String, Integer> shortCitationCounter = new HashMap<>();

    protected PrintPubExportState(PrintPubExportConfigurator config) {
        super(config);
        this.result = ExportResult.NewInstance(config.getResultType());
        this.documentModel = new PrintPubDocumentModel();
        // The processor gets a reference to this state
        this.processor = new PrintPubExportResultProcessor(this);
    }

    // ***************** GETTERS / SETTERS ***************** //

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

    // ***************** HELPER METHODS ***************** //

    /**
     * Checks if we have already printed a specific block (like a Reference or
     * Specimen). This replaces the complex Map<Integer, Object> stores.
     */
    public boolean hasPrinted(UUID uuid) {
        return printedElementIds.contains(uuid.toString());
    }

    public void markAsPrinted(UUID uuid) {
        printedElementIds.add(uuid.toString());
    }

    /**
     * Logic to generate "Miller (1977a)", "Miller (1977b)" Kept because this is
     * useful business logic for documents.
     */
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
        // Skip 'i'/'j' confusion if necessary, logic copied from original
        if (finalCounter >= 'j') {
            finalCounter++;
        }
        return Character.toString((char) (finalCounter));
    }
}