/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.taxa;


import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public class NormalExplicitImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator, IMatchingImportConfigurator {
	private static final Logger logger = Logger.getLogger(NormalExplicitImportConfigurator.class);

	private boolean isDoMatchTaxa = true;

	private UUID parentUUID;





	//	@SuppressWarnings("unchecked")
	@Override
    protected void makeIoClassList() {
		ioClassList = new Class[] {
				NormalExplicitImport.class
		};
	}

	public static NormalExplicitImportConfigurator NewInstance(URI uri, ICdmDataSource destination,
						NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation){
		return new NormalExplicitImportConfigurator(uri, destination, nomenclaturalCode, dbSchemaValidation);
	}



	/**
	 * @param url
	 * @param destination
	 */
	private NormalExplicitImportConfigurator(URI uri, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation) {
		super(uri, destination);
		if (dbSchemaValidation == null){
			dbSchemaValidation = DbSchemaValidation.CREATE;
		}
		setSource(uri);
		setDestination(destination);
		setDbSchemaValidation(dbSchemaValidation);
		setNomenclaturalCode(nomenclaturalCode);
	}

	/**
     * @param stream
     * @param destination
     */
    private NormalExplicitImportConfigurator(byte[] stream, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation) {
        super(null, destination);
        if (dbSchemaValidation == null){
            dbSchemaValidation = DbSchemaValidation.CREATE;
        }
        setStream(stream);
        setDestination(destination);
        setDbSchemaValidation(dbSchemaValidation);
        setNomenclaturalCode(nomenclaturalCode);
    }




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	@Override
    public TaxonExcelImportState getNewState() {
		return new TaxonExcelImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newGeneric();
			sourceReference.setTitleCache("Excel Taxon import", true);
		}
		return sourceReference;
	}


	@Override
    public boolean isReuseExistingTaxaWhenPossible() {
		return isDoMatchTaxa;
	}

	@Override
    public void setReuseExistingTaxaWhenPossible(boolean isDoMatchTaxa) {
		this.isDoMatchTaxa = isDoMatchTaxa;
	}


    public UUID getParentUUID(){
        return parentUUID;
    }


    public void setParentUUID(UUID parentUUID) {
        this.parentUUID = parentUUID;
    }




}
