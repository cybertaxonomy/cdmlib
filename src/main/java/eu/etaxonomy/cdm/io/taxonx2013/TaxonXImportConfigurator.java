/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.taxonx2013;


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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author p.kelbert
 * @created 08.03.2013
 * @version 1.0
 */
public class TaxonXImportConfigurator extends ImportConfiguratorBase<TaxonXImportState, URI> implements IImportConfigurator, IMatchingImportConfigurator {
	private static final Logger logger = Logger.getLogger(TaxonXImportConfigurator.class);
	private boolean doParsing = false;
	private boolean reuseMetadata = false;
	private boolean reuseTaxon = true;
	private String taxonReference = null;
	private boolean doCreateIndividualsAssociations = true;
	private boolean doReuseExistingDescription = false;
	private boolean doMatchTaxa = true;
	private final Map<UUID, UUID> taxonToDescriptionMap = new HashMap<UUID, UUID>();


	//TODO
	private static IInputTransformer defaultTransformer = null;

	@Override
    @SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		System.out.println("makeIOClassList");
		ioClassList = new Class[]{
			TaxonXImport.class,
		};
	};

	public static TaxonXImportConfigurator NewInstance(URI uri,
			ICdmDataSource destination){
		return new TaxonXImportConfigurator(uri, destination);
	}


	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private TaxonXImportConfigurator(URI uri, ICdmDataSource destination) {
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
	public TaxonXImportState getNewState() {
		return new TaxonXImportState(this);
	}


	@Override
    public URI getSource(){
		return super.getSource();
	}

	/**
	 * @param file
	 */
	@Override
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
