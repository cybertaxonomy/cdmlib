/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation.config;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.operation.DeleteNonReferencedReferencesUpdater;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author k.luther
 * @since 2015
 */
public class DeleteNonReferencedReferencesConfigurator
        extends ImportConfiguratorBase<DefaultImportState<DeleteNonReferencedReferencesConfigurator>, Object> {

    private static final long serialVersionUID = -3063590000817699527L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CacheUpdaterConfigurator.class);

	private boolean doReferences = true;
	private boolean doAuthors = true;

	public DeleteNonReferencedReferencesConfigurator() {
		super(null);
	}

	public DeleteNonReferencedReferencesConfigurator(ICdmDataSource destination) {
		super(null);
		this.setSource(destination);
		this.setDestination(destination);
		this.setDbSchemaValidation(DbSchemaValidation.UPDATE);
	}

	public static DeleteNonReferencedReferencesConfigurator NewInstance(ICdmDataSource destination){
		DeleteNonReferencedReferencesConfigurator result = new DeleteNonReferencedReferencesConfigurator(destination);
		return result;
	}

	@Override
	public <STATE extends ImportStateBase> STATE getNewState() {
		return (STATE) new DefaultImportState(this);
	}

	@SuppressWarnings("unchecked")
    @Override
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				 DeleteNonReferencedReferencesUpdater.class
		};
	}

    public boolean isDoReferences() {
        return doReferences;
    }

    public void setDoReferences(boolean doReferences) {
        this.doReferences = doReferences;
    }

    public boolean isDoAuthors() {
        return doAuthors;
    }

    public void setDoAuthors(boolean doAuthors) {
        this.doAuthors = doAuthors;
    }

	@Override
	public Reference getSourceReference() {
		return null;
	}
}
