/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public class DwcaImportConfigurator extends ImportConfiguratorBase<DwcaImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(DwcaImportConfigurator.class);
	private static IInputTransformer defaultTransformer = new DwcaImportTransformer();
	
	private boolean usePartitions = true;
	private int defaultPartitionSize = 1000;
	
//	//new
//	private boolean doSpecimen = true;  //reads the specimen worksheet
//	private boolean doAreaLevels = true;  //reads the areaLevels worksheet
//	private boolean doExtensionTypes = true;  //reads the extensionType worksheet
	
	
//	private boolean useCountry;  //if isocountry and country is available, use country instead of isocountry 
//	//tries to match areas by the abbreviated label
//	//TODO still needs to be refined as this may hold only for certain vocabularies, levels, ...
//	//there also maybe more strategies like first label, then abbreviated label, ....
//	private TermMatchMode areaMatchMode = TermMatchMode.UUID_ONLY;
//	private PersonParserFormatEnum personParserFormat = PersonParserFormatEnum.POSTFIX;  //
//	
//	//if true, determinations are imported also as individualAssociations	
//	private boolean makeIndividualAssociations = true;
//	private boolean useMaterialsExaminedForIndividualsAssociations = true;
//	private boolean firstDeterminationIsStoredUnder = false;
//	private boolean determinationsAreDeterminationEvent = true;
//	
//	private boolean preferNameCache = true;
//	private boolean createTaxonIfNotExists = false;
////	private boolean includeSynonymsForTaxonMatching = false;

		


	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			DwcaImport.class
		};
	}
	
	public static DwcaImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new DwcaImportConfigurator(uri, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private DwcaImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		this.setSource(uri);
		this.setDestination(destination);
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public DwcaImportState getNewState() {
		return new DwcaImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newGeneric();
			sourceReference.setTitleCache("DwC-A import", true);
		}
		return sourceReference;
	}

	
	
	public boolean isUsePartitions() {
		return usePartitions;
	}

	public void setUsePartitions(boolean usePartitions) {
		this.usePartitions = usePartitions;
	}

	public void setDefaultPartitionSize(int defaultPartitionSize) {
		this.defaultPartitionSize = defaultPartitionSize;
	}

	public int getDefaultPartitionSize() {
		return defaultPartitionSize;
	}
	
	
}
