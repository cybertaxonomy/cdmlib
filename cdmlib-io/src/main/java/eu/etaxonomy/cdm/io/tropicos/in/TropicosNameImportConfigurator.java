/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tropicos.in;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.csv.in.CsvImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 \* @since 15.11.2017
 */
public class TropicosNameImportConfigurator extends CsvImportConfiguratorBase {

    private static final long serialVersionUID = -648787716062337242L;

    private boolean createTaxa = false;
    private UUID parentNodeUuid;
    private String classificationName;
    private boolean unplaced = true;  //if new nodes should be created, should they be marked as unplaced?

    private boolean allowTropicosDuplicates = false;
    private boolean allowIpniDuplicates = false;
    private boolean allowWfoDuplicates = false;
    private boolean reportDuplicateIdentifier = true;

    private boolean addAuthorsToReference = true;

    /**
     * @param inputStream
     * @param object
     * @return
     */
    public static TropicosNameImportConfigurator NewInstance(InputStreamReader inputStream, ICdmDataSource cdmDestination) {
        return new TropicosNameImportConfigurator(inputStream, cdmDestination);
    }

    public static TropicosNameImportConfigurator NewInstance(URI uri, ICdmDataSource cdmDestination)
            throws IOException {
        return new TropicosNameImportConfigurator(uri, cdmDestination);
    }

    /**
     * @param inputStream
     * @param cdmDestination
     */
    private TropicosNameImportConfigurator(InputStreamReader inputStream, ICdmDataSource cdmDestination) {
        super(inputStream, cdmDestination);
    }

    /**
     * @param uri
     * @param cdmDestination
     * @param transformer
     * @throws IOException
     */
    private TropicosNameImportConfigurator(URI uri, ICdmDataSource cdmDestination)
            throws IOException {
        super(uri, cdmDestination, null);
        this.setNomenclaturalCode(NomenclaturalCode.ICNAFP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                TropicosNameImport.class
            };
    }

    @Override
    public TropicosNameImportState getNewState() {
        return new TropicosNameImportState(this);
    }

    /**
     * If <code>true</code> additional to the name a
     * taxon is created. Depending on further parameters
     * {@link #getParentNodeUuid()} and/or {@link #getClassificationName()}
     * it is decided where to put the taxon in a classification.
     * @return <code>true</code> if a taxon is to be created.
     */
    public boolean isCreateTaxa() {
        return createTaxa;
    }
    /**
     * {@link #setEditor(eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR)}
     */
    public void setCreateTaxa(boolean createTaxa) {
        this.createTaxa = createTaxa;
    }

    /**
     * If {@link #isCreateTaxa()} is <code>true</code> the taxon
     * to be created will be imported below the {@link TaxonNode taxon node}
     * with the given taxon node uuid.
     */
    public UUID getParentNodeUuid() {
        return parentNodeUuid;
    }
    public void setParentNodeUuid(UUID parentNodeUuid) {
        this.parentNodeUuid = parentNodeUuid;
    }

    /**
     * If {@link #isCreateTaxa()} is <code>true</code> and if no
     * {@link #getParentNodeUuid() parent node uuid} is given
     * the taxon to be created will be imported into a newly created
     * classification with the given classification name.
     */
    @Override
    public String getClassificationName() {
        return classificationName;
    }
    @Override
    public void setClassificationName(String classificationName) {
        this.classificationName = classificationName;
    }

    /**
     * If taxa are created ({@link #isCreateTaxa()} should the according
     * taxon nodes be marked as unplaced?
     * @see #isCreateTaxa()
     * @see #getClassificationName()
     * @see #getParentNodeUuid()
     * @return the unplaced
     */
    public boolean isUnplaced() {
        return unplaced;
    }
    public void setUnplaced(boolean unplaced) {
        this.unplaced = unplaced;
    }

    /**
     * If <code>false</code> and if the name has a Tropicos ID
     * and if this Tropicos ID already exists in the database or in previously
     * imported data the import of the given record will be stopped.
     * @return <code>true</code> if Tropicos duplicates are allowed for this import
     */
    public boolean isAllowTropicosDuplicates() {
        return allowTropicosDuplicates;
    }

    /**
     * @see #isAllowTropicosDuplicates()
     */
    public void setAllowTropicosDuplicates(boolean allowTropicosDuplicates) {
        this.allowTropicosDuplicates = allowTropicosDuplicates;
    }

    /**
     * If <code>false</code> and if the name has an IPNI ID
     * and if this IPNI ID already exists in the database or in previously
     * imported data the import of the given record will be stopped.
     * @return <code>true</code> if IPNI duplicates are allowed for this import
     */
    public boolean isAllowIpniDuplicates() {
        return allowIpniDuplicates;
    }

    /**
     * @see #isAllowIpniDuplicates()
     */
    public void setAllowIpniDuplicates(boolean allowIpniDuplicates) {
        this.allowIpniDuplicates = allowIpniDuplicates;
    }

    /**
     * If <code>false</code> and if the name has a World Flora Online (WFO) ID
     * and if this WFO ID already exists in the database or in previously
     * imported data the import of the given record will be stopped.
     * @return <code>true</code> if WFO duplicates are allowed for this import
     */
    public boolean isAllowWfoDuplicates() {
        return allowWfoDuplicates;
    }
    /**
     * @see #isAllowWfoDuplicates()
     * @param allowWfoDuplicates
     */
    public void setAllowWfoDuplicates(boolean allowWfoDuplicates) {
        this.allowWfoDuplicates = allowWfoDuplicates;
    }

    /**
     * If <code>true</code> the name authors will be added
     * to the nomenclatural reference (Book or Article) though
     * it might not be the exact same author.<BR>
     * Default is <code>true</code>
     */
    public boolean isAddAuthorsToReference() {
        return addAuthorsToReference;
    }
    /**
     * @see #isAddAuthorsToReference()
     */
    public void setAddAuthorsToReference(boolean addAuthorsToReference) {
        this.addAuthorsToReference = addAuthorsToReference;
    }

    /**
     * if <code>true</code> duplicate identifiers like
     * {@link #isAllowTropicosDuplicates() Tropicos IDs}
     * {@link #isAllowIpniDuplicates() IPNI IDs} or
     * {@link #isAllowWfoDuplicates() WFO IDs} will be reported.
     * This is only relevant if duplicates are allowed,
     * otherwise the duplicates will be reported anyway.
     */
    public boolean isReportDuplicateIdentifier() {
        return reportDuplicateIdentifier;
    }
    /**
     * @see #isReportDuplicateIdentifier()
     */
    public void setReportDuplicateIdentifier(boolean reportDuplicateIdentifier) {
        this.reportDuplicateIdentifier = reportDuplicateIdentifier;
    }

}
