/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author k.luther
 * @since 2015
 */
public class NonReferencedObjectsDeleterConfigurator
        extends ImportConfiguratorBase<DefaultImportState<NonReferencedObjectsDeleterConfigurator>, Object> {

    private static final long serialVersionUID = -3063590000817699527L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private boolean doReferences = true;
	private boolean doAuthors = true;

	private boolean isKeepReferencesWithTitle = false;
	private boolean isKeepRisSources = false;

	//if true, records are not deleted but only reported (later this will be part of the analyzing step
	private boolean doOnlyReport = false;

	public NonReferencedObjectsDeleterConfigurator() {
		super(null);
	}

	public NonReferencedObjectsDeleterConfigurator(ICdmDataSource destination) {
		super(null);
		this.setSource(destination);
		this.setDestination(destination);
		this.setDbSchemaValidation(DbSchemaValidation.UPDATE);
	}

	public static NonReferencedObjectsDeleterConfigurator NewInstance(ICdmDataSource destination){
		NonReferencedObjectsDeleterConfigurator result = new NonReferencedObjectsDeleterConfigurator(destination);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public <STATE extends ImportStateBase> STATE getNewState() {
		return (STATE) new DefaultImportState(this);
	}

	@SuppressWarnings("unchecked")
    @Override
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				 NonReferencedObjectsDeleter.class
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

    public boolean isKeepReferencesWithTitle() {
        return isKeepReferencesWithTitle;
    }
    public void setKeepReferencesWithTitle(boolean isKeepReferencesWithTitle) {
        this.isKeepReferencesWithTitle = isKeepReferencesWithTitle;
    }

    public boolean isKeepRisSources() {
        return isKeepRisSources;
    }
    public void setKeepRisSources(boolean isKeepRisSources) {
        this.isKeepRisSources = isKeepRisSources;
    }

    //doOnlyReport
    public boolean isDoOnlyReport() {
        return doOnlyReport;
    }
    public void setDoOnlyReport(boolean doOnlyReport) {
        this.doOnlyReport = doOnlyReport;
    }

    @Override
    public Reference getSourceReference() {
        return null;
    }
}
