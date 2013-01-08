/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
public class Abcd206ImportConfigurator extends ImportConfiguratorBase<Abcd206ImportState, URI> implements IImportConfigurator, IMatchingImportConfigurator {
	private static final Logger logger = Logger.getLogger(Abcd206ImportConfigurator.class);
	private boolean doParsing = false;
	private boolean reuseMetadata = false;
	private boolean reuseTaxon = true;
	private String taxonReference = null;
	private boolean doCreateIndividualsAssociations = true;
	private boolean doReuseExistingDescription = false;
	private boolean doMatchTaxa = true;
	private Map<UUID, UUID> taxonToDescriptionMap = new HashMap<UUID, UUID>();

	
	//TODO
	private static IInputTransformer defaultTransformer = null;
		
	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		System.out.println("makeIOClassList");
		ioClassList = new Class[]{
			Abcd206Import.class,
		};
	};
	
	public static Abcd206ImportConfigurator NewInstance(URI uri,
			ICdmDataSource destination){
		return new Abcd206ImportConfigurator(uri, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private Abcd206ImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
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
	@Override
	public Abcd206ImportState getNewState() {
		return new Abcd206ImportState(this);
	}
	

	public URI getSource(){
		return super.getSource();
	}
	
	/**
	 * @param file
	 */
	public void setSource(URI uri) {
		super.setSource(uri);
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
			sourceReference.setTitleCache("ABCD specimen import", true);
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

	public void setDoAutomaticParsing(boolean doParsing){
		this.doParsing=doParsing;
	}
	
	public boolean isDoAutomaticParsing(){
		return this.doParsing;
	}
	
	public void setReUseExistingMetadata(boolean reuseMetadata){
		this.reuseMetadata = reuseMetadata;
	}
	
	public boolean isReUseExistingMetadata(){
		return this.reuseMetadata;
	}
	
	public void setReUseTaxon(boolean reuseTaxon){
		this.reuseTaxon = reuseTaxon;
	}
	
	/**
	 * if {@link #doMatchTaxa} is set false or no matching taxon is found new
	 * taxa will be created. If this flag is set <code>true</code> the newly created taxa
	 * will be reused if possible. Setting this flag to <code>false</code> may lead to
	 * multiple identical taxa.
	 * 
	 * @return
	 */
	public boolean isDoReUseTaxon(){
		return this.reuseTaxon;
	}

	public void setDoCreateIndividualsAssociations(
			boolean doCreateIndividualsAssociations) {
		this.doCreateIndividualsAssociations = doCreateIndividualsAssociations;
	}

	/**
	 * Create an IndividualsAssociations for each determination element in the ABCD data. ABCD has no such concept as IndividualsAssociations so the only way to 
	 * 
	 * @return
	 */
	public boolean isDoCreateIndividualsAssociations() {
		return doCreateIndividualsAssociations;
	}

	/**
	 * @param doReuseExistingDescription the doReuseExistingDescription to set
	 */
	public void setDoReuseExistingDescription(boolean doReuseExistingDescription) {
		this.doReuseExistingDescription = doReuseExistingDescription;
	}

	/**
	 * @return the doReuseExistingDescription
	 */
	public boolean isDoMatchToExistingDescription() {
		return doReuseExistingDescription;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator#isDoMatchTaxa()
	 */
	@Override
	public boolean isDoMatchTaxa() {
		return doMatchTaxa;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator#setDoMatchTaxa(boolean)
	 */
	@Override
	public void setDoMatchTaxa(boolean doMatchTaxa) {
		this.doMatchTaxa = doMatchTaxa;
	}

	/**
	 * @return
	 */
	public Map<UUID, UUID> getTaxonToDescriptionMap() {
		// TODO Auto-generated method stub
		return taxonToDescriptionMap ;
	}
	
	
}
