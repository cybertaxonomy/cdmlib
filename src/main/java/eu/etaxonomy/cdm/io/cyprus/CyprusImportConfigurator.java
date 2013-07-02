/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cyprus;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class CyprusImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CyprusImportConfigurator.class);

	private UUID uuidCyprusReference = UUID.fromString("b5281cd3-9d5d-4ae2-8d55-b62a592ce846");
	
	private String cyprusReferenceTitle = "Cyprus Distributions Excel Import";

	private boolean doDistribution;
	private boolean isDoTaxa;
	
	private static IInputTransformer defaultTransformer = new CyprusTransformer();
	
	public static CyprusImportConfigurator NewInstance(URI source, ICdmDataSource destination){
		return new CyprusImportConfigurator(source, destination);
}


	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				CyprusUserImport.class,
				CyprusExcelImport.class ,
				CyprusDistributionImport.class ,
				
		};	
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImportStateBase getNewState() {
		return new CyprusImportState(this);
	}



	private CyprusImportConfigurator(URI source, ICdmDataSource destination) {
	   super(source, destination, defaultTransformer);
	   setNomenclaturalCode(NomenclaturalCode.ICBN); 
	   setSource(source);
	   setDestination(destination);
	}
	
	
	public URI getSource() {
		return (URI)super.getSource();
	}
	public void setSource(URI source) {
		super.setSource(source);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReference()
	 */
	public Reference getSourceReference() {
		if (sourceReference == null){
			sourceReference =  ReferenceFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getCyprusReferenceTitle(), true);
			}
		}
		return sourceReference;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		return getSource().toString();
	}


	public void setUuidCyprusReference(UUID uuidCyprusReference) {
		this.uuidCyprusReference = uuidCyprusReference;
	}


	public UUID getUuidCyprusReference() {
		return uuidCyprusReference;
	}


	public void setCyprusReferenceTitle(String cyprusReferenceTitle) {
		this.cyprusReferenceTitle = cyprusReferenceTitle;
	}


	public String getCyprusReferenceTitle() {
		return cyprusReferenceTitle;
	}



	public void setDoDistribution(boolean doDistribution) {
		this.doDistribution = doDistribution;
	}
	
	public boolean isDoDistribution(){
		return this.doDistribution;
	}
	

	public void setDoTaxa(boolean isDoTaxa) {
		this.isDoTaxa = isDoTaxa;
	}

	public boolean isDoTaxa() {
		return isDoTaxa;
	}
	
	

}
