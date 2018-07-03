/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.in;

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
 * @since 15.11.2017
 */
public class WfoAccessImportConfigurator extends CsvImportConfiguratorBase {

    private static final long serialVersionUID = -1176968167300861330L;

    private UUID parentNodeUuid;
    private String classificationName;
    private boolean unplaced = true;  //if new nodes should be created, should they be marked as unplaced?

    private boolean reportDuplicateIdentifier = true;

    private boolean addAuthorsToReference = true;

    /**
     * @param inputStream
     * @param object
     * @return
     */
    public static WfoAccessImportConfigurator NewInstance(InputStreamReader inputStream, ICdmDataSource cdmDestination) {
        return new WfoAccessImportConfigurator(inputStream, cdmDestination);
    }

    public static WfoAccessImportConfigurator NewInstance(URI uri, ICdmDataSource cdmDestination)
            throws IOException {
        return new WfoAccessImportConfigurator(uri, cdmDestination);
    }

    /**
     * @param inputStream
     * @param cdmDestination
     */
    private WfoAccessImportConfigurator(InputStreamReader inputStream, ICdmDataSource cdmDestination) {
        super(inputStream, cdmDestination);
    }

    /**
     * @param uri
     * @param cdmDestination
     * @param transformer
     * @throws IOException
     */
    private WfoAccessImportConfigurator(URI uri, ICdmDataSource cdmDestination)
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
                WfoAccessTaxonImport.class
            };
    }

    @Override
    public WfoAccessImportState getNewState() {
        return new WfoAccessImportState(this);
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
