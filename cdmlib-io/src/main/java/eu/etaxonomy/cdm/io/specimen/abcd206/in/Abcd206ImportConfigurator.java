/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
public class Abcd206ImportConfigurator extends ImportConfiguratorBase<Abcd206ImportState> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(Abcd206ImportConfigurator.class);
	private boolean doParsing = false;
	private boolean reuseMetadata = false;
	private boolean reuseTaxon = false;
	private String taxonReference = null;

	
	//TODO
	private static IInputTransformer defaultTransformer = null;
		
	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		System.out.println("makeIOClassList");
		ioClassList = new Class[]{
			Abcd206Import.class,
		};
	};
	
	public static Abcd206ImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new Abcd206ImportConfigurator(url, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private Abcd206ImportConfigurator(String url, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(url);
		setDestination(destination);
	}
	

	
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
//	 */
//	public String getSource() {
//		return (String)super.getSource();
//	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public Abcd206ImportState getNewState() {
		return new Abcd206ImportState(this);
	}

	public String getSource(){
		return (String)super.getSource();
	}
	
	/**
	 * @param file
	 */
	public void setSource(String file) {
		super.setSource(file);
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			sourceReference = refFactory.newDatabase();
			sourceReference.setTitleCache("ABCD specimen import", true);
		}
		return sourceReference;
	}
	
	public void setTaxonReference(String taxonReference) {
		this.taxonReference = taxonReference;
	}
	
	public ReferenceBase getTaxonReference() {
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
			return (String)this.getSource();
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
