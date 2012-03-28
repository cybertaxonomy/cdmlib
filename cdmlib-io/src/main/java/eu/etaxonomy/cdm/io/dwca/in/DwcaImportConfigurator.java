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
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.MappingType;
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
	private int defaultPartitionSize = 2000;
	private boolean deduplicateNamePublishedIn = true;
	private boolean scientificNameIdAsOriginalSourceId = false;
	private boolean datasetsAsClassifications = true;
	
	private IImportMapping.MappingType mappingType = MappingType.InMemoryMapping;
	
	
		


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

	public IImportMapping.MappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(IImportMapping.MappingType mappingType) {
		this.mappingType = mappingType;
	}

	public boolean isDeduplicateNamePublishedIn() {
		return deduplicateNamePublishedIn;
	}

	public void setDeduplicateNamePublishedIn(boolean deduplicateNamePublishedIn) {
		this.deduplicateNamePublishedIn = deduplicateNamePublishedIn;
	}

	public void setScientificNameIdAsOriginalSourceId(
			boolean scientificNameIdAsOriginalSourceId) {
		this.scientificNameIdAsOriginalSourceId = scientificNameIdAsOriginalSourceId;
	}

	public boolean isScientificNameIdAsOriginalSourceId() {
		return scientificNameIdAsOriginalSourceId;
	}

	public void setDatasetsAsClassifications(boolean datasetsAsClassifications) {
		this.datasetsAsClassifications = datasetsAsClassifications;
	}

	public boolean isDatasetsAsClassifications() {
		return datasetsAsClassifications;
	}
	
	
}
