/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.redlist;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DbImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 27.08.2012
 * @version 1.0
 */
public class RoteListeDbImportConfigurator extends DbImportConfiguratorBase<RoteListeDbImportState> implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RoteListeDbImportConfigurator.class);
	
	public static RoteListeDbImportConfigurator NewInstance(Source ermsSource, ICdmDataSource destination){
			return new RoteListeDbImportConfigurator(ermsSource, destination);
	}

	/* Max number of records to be saved with one service call */
	private int recordsPerTransaction = 1000;  //defaultValue

	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	private boolean doTaxa = true;

	
	private static IInputTransformer defaultTransformer = new RoteListeDbTransformer();
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				 RoteListeDbTaxonImport.class,
		};	
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImportStateBase getNewState() {
		return new RoteListeDbImportState(this);
	}



	private RoteListeDbImportConfigurator(Source source, ICdmDataSource destination) {
	   super(source, destination, NomenclaturalCode.ICBN, defaultTransformer);
	}
	

	/**
	 * @return the limitSave
	 */
	public int getRecordsPerTransaction() {
		return recordsPerTransaction;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setRecordsPerTransaction(int recordsPerTransaction) {
		this.recordsPerTransaction = recordsPerTransaction;
	}
	
	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}
	
	

}
