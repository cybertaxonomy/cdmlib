/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DbImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class CentralAfricaFernsImportConfigurator extends DbImportConfiguratorBase<CentralAfricaFernsImportState> implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CentralAfricaFernsImportConfigurator.class);

	public static CentralAfricaFernsImportConfigurator NewInstance(Source ermsSource, ICdmDataSource destination){
			return new CentralAfricaFernsImportConfigurator(ermsSource, destination);
	}

	private boolean doVernaculars = true;
	private boolean doLinks = true;
	private boolean doNotes = true;
	private boolean doImages = true;
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;

	
	private static IInputTransformer defaultTransformer = new CentralAfricaFernsTransformer();
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				//ErmsGeneralImportValidator.class
//				 CentralAfricaFernsReferenceImport.class ,
				 CentralAfricaFernsTaxonImport.class,
				 CentralAfricaFernsTaxonRelationImport.class
		};	
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImportStateBase getNewState() {
		return new CentralAfricaFernsImportState(this);
	}



	private CentralAfricaFernsImportConfigurator(Source source, ICdmDataSource destination) {
	   super(source, destination,NomenclaturalCode.ICBN,  defaultTransformer);//default for FdAC
	}
	

	/**
	 * @param doVernaculars the doVernaculars to set
	 */
	public void setDoVernaculars(boolean doVernaculars) {
		this.doVernaculars = doVernaculars;
	}
	/**
	 * @return the doVernaculars
	 */
	public boolean isDoVernaculars() {
		return doVernaculars;
	}



	/**
	 * @param doLinks the doLinks to set
	 */
	public void setDoLinks(boolean doLinks) {
		this.doLinks = doLinks;
	}
	/**
	 * @return the doLinks
	 */
	public boolean isDoLinks() {
		return doLinks;
	}



	/**
	 * @param doNotes the doNotes to set
	 */
	public void setDoNotes(boolean doNotes) {
		this.doNotes = doNotes;
	}
	/**
	 * @return the doNotes
	 */
	public boolean isDoNotes() {
		return doNotes;
	}

	/**
	 * @param doImages the doImages to set
	 */
	public void setDoImages(boolean doImages) {
		this.doImages = doImages;
	}
	/**
	 * @return the doImages
	 */
	public boolean isDoImages() {
		return doImages;
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

	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}
	

}
