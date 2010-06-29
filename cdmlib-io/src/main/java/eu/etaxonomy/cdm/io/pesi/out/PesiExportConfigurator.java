// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public class PesiExportConfigurator extends DbExportConfiguratorBase implements IExportConfigurator {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PesiExportConfigurator.class);
	private int limitSave = 1000;
	private ReferenceBase<?> auctReference;

	public static PesiExportConfigurator NewInstance(Source pesiDestination, ICdmDataSource source) {
			return new PesiExportConfigurator(pesiDestination, source);
	}
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				PesiSourceExport.class,
				PesiTaxonExport.class,
				PesiRelTaxonExport.class, // RelTaxonId's could be deleted from state hashmap
				PesiNoteExport.class,
				PesiNoteSourceExport.class, // NoteId's could be deleted from state hashmap
				PesiAdditionalTaxonSourceExport.class,
				PesiOccurrenceExport.class,
				PesiOccurrenceSourceExport.class
		};

	}
	
	/**
	 * @param pesiSource
	 * @param cdmSource
	 */
	private PesiExportConfigurator(Source pesiSource, ICdmDataSource cdmSource) {
	   super();
	   setSource(cdmSource);
	   setDestination(pesiSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getNewState()
	 */
	@SuppressWarnings("unchecked")
	public ExportStateBase getNewState() {
		return new PesiExportState(this);
	}
	
	/**
	 * @return the limitSave
	 */
	public int getLimitSave() {
		return limitSave;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setLimitSave(int limitSave) {
		this.limitSave = limitSave;
	}

	/**
	 * Returns the Reference for a Misapplied Name.
	 * Copied from FaunaEuropaeaImportConfigurator.
	 * @return
	 */
	public ReferenceBase<?> getAuctReference() {
		if (auctReference == null){
			auctReference = ReferenceFactory.newGeneric();
			
			auctReference.setTitleCache("auct.", true);
		}
		return auctReference;
	}

}
