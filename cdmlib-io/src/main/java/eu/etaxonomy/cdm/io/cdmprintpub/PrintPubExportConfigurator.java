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

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.out.IFactExportConfigurator;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportConfiguratorBase;

/**
 * The configurator for the Print/Publication export.
 * Defines input source, output destination, and content filters.
 *
 * @author veldmap97
 * @date Dec 2, 2025
 */
public class PrintPubExportConfigurator
        extends TaxonTreeExportConfiguratorBase<PrintPubExportState, PrintPubExportConfigurator>
        implements IFactExportConfigurator {

    private static final long serialVersionUID = -5958099339227666207L;

    // General
    private String documentTitle = "Taxonomic Export";
    private boolean doFactualData = true;
    private boolean includeUnpublishedFacts = false;

    // 1. Taxonomic Scope & Concepts
    private boolean includeMisappliedNames = true;
    private boolean includeTaxonomicConceptReference = true; // "Secundum" for accepted taxa
    private boolean includeSynonymConceptReference = false;  // "Secundum" for synonyms

    // 2. Type Information & Formatting
    private boolean includeSupraspecificTypes = true;
    private boolean includeSpeciesTypes = true;
    private boolean startSupraspecificTypesOnNewLine = false;

    // 3. Indices
    private boolean generateCommonNameIndex = false;
    private boolean generateScientificNameIndex = true;

    // 4. Appendix: Digital Identifiers
    private boolean appendIdentifierList = true;
    private boolean includeWfoId = true;
    private boolean includeProtologueUris = true;

    public static PrintPubExportConfigurator NewInstance(ICdmDataSource source, File destination){
        PrintPubExportConfigurator result = new PrintPubExportConfigurator(null);
        result.setSource(source);
        result.setDestination(destination);
        return result;
    }

    public PrintPubExportConfigurator(IExportTransformer transformer) {
        super(transformer);
        this.resultType = ExportResultType.BYTE_ARRAY;
        this.setTarget(TARGET.EXPORT_DATA);
        setUserFriendlyIOName("Print/Publication Export");
    }

    @Override
    public PrintPubExportState getNewState() {
        return new PrintPubExportState(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList() {
        ioClassList = new Class[] {
            PrintPubClassificationExport.class
        };
    }

    @Override
    public String getDestinationNameString() {
        if (this.getDestination() != null) {
            return this.getDestination().getName();
        }
        return null;
    }

    // --- Getters and Setters ---

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    @Override
    public boolean isDoFactualData() {
        return doFactualData;
    }

    @Override
    public void setDoFactualData(boolean doFactualData) {
        this.doFactualData = doFactualData;
    }

    @Override
    public boolean isIncludeUnpublishedFacts() {
        return includeUnpublishedFacts;
    }

    @Override
    public void setIncludeUnpublishedFacts(boolean includeUnpublishedFacts) {
        this.includeUnpublishedFacts = includeUnpublishedFacts;
    }

    public boolean isIncludeMisappliedNames() {
        return includeMisappliedNames;
    }

    public void setIncludeMisappliedNames(boolean includeMisappliedNames) {
        this.includeMisappliedNames = includeMisappliedNames;
    }

    public boolean isIncludeTaxonomicConceptReference() {
        return includeTaxonomicConceptReference;
    }

    public void setIncludeTaxonomicConceptReference(boolean includeTaxonomicConceptReference) {
        this.includeTaxonomicConceptReference = includeTaxonomicConceptReference;
    }

    public boolean isIncludeSynonymConceptReference() {
        return includeSynonymConceptReference;
    }

    public void setIncludeSynonymConceptReference(boolean includeSynonymConceptReference) {
        this.includeSynonymConceptReference = includeSynonymConceptReference;
    }

    public boolean isIncludeSupraspecificTypes() {
        return includeSupraspecificTypes;
    }

    public void setIncludeSupraspecificTypes(boolean includeSupraspecificTypes) {
        this.includeSupraspecificTypes = includeSupraspecificTypes;
    }

    public boolean isIncludeSpeciesTypes() {
        return includeSpeciesTypes;
    }

    public void setIncludeSpeciesTypes(boolean includeSpeciesTypes) {
        this.includeSpeciesTypes = includeSpeciesTypes;
    }

    public boolean isStartSupraspecificTypesOnNewLine() {
        return startSupraspecificTypesOnNewLine;
    }

    public void setStartSupraspecificTypesOnNewLine(boolean startSupraspecificTypesOnNewLine) {
        this.startSupraspecificTypesOnNewLine = startSupraspecificTypesOnNewLine;
    }

    public boolean isGenerateCommonNameIndex() {
        return generateCommonNameIndex;
    }

    public void setGenerateCommonNameIndex(boolean generateCommonNameIndex) {
        this.generateCommonNameIndex = generateCommonNameIndex;
    }

    public boolean isGenerateScientificNameIndex() {
        return generateScientificNameIndex;
    }

    public void setGenerateScientificNameIndex(boolean generateScientificNameIndex) {
        this.generateScientificNameIndex = generateScientificNameIndex;
    }

    public boolean isAppendIdentifierList() {
        return appendIdentifierList;
    }

    public void setAppendIdentifierList(boolean appendIdentifierList) {
        this.appendIdentifierList = appendIdentifierList;
    }

    public boolean isIncludeWfoId() {
        return includeWfoId;
    }

    public void setIncludeWfoId(boolean includeWfoId) {
        this.includeWfoId = includeWfoId;
    }

    public boolean isIncludeProtologueUris() {
        return includeProtologueUris;
    }

    public void setIncludeProtologueUris(boolean includeProtologueUris) {
        this.includeProtologueUris = includeProtologueUris;
    }
}