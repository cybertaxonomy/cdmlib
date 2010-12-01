/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
public class SpecimenExcelImportConfigurator extends ImportConfiguratorBase<SpecimenExcelImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(SpecimenExcelImportConfigurator.class);
	private boolean doParsing = false;
	private boolean reuseMetadata = false;
	private boolean reuseTaxon = false;
	private String taxonReference = null;
	
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			SpecimenExcelImport.class,
		};
	};
	
	public static SpecimenExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new SpecimenExcelImportConfigurator(uri, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private SpecimenExcelImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public SpecimenExcelImportState getNewState() {
		return new SpecimenExcelImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			sourceReference = refFactory.newDatabase();
			sourceReference.setTitleCache("SYNTHESYS specimen import", true);
		}
		return sourceReference;
	}
	
	public void setTaxonReference(String taxonReference) {
		this.taxonReference = taxonReference;
	}
	
	public Reference getTaxonReference() {
		//TODO
		if (this.taxonReference == null){
			logger.info("getTaxonReference not yet fully implemented");
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}
	
	public void setDoAutomaticParsing(boolean doParsing){
		this.doParsing=doParsing;
	}
	
	public boolean getDoAutomaticParsing(){
		return this.doParsing;
	}
	
	public void setReUseExistingMetadata(boolean reuseMetadata){
		this.reuseMetadata = reuseMetadata;
	}
	
	public boolean getReUseExistingMetadata(){
		return this.reuseMetadata;
	}
	
	public void setReUseTaxon(boolean reuseTaxon){
		this.reuseTaxon = reuseTaxon;
	}
	
	public boolean getDoReUseTaxon(){
		return this.reuseTaxon;
	}
	
	
	
}
