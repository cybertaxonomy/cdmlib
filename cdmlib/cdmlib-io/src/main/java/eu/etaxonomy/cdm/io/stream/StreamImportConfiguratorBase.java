/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.stream;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImport;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.MappingType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * Base class for all configurators for stream based imports.
 * @author a.mueller
 * @created 14.05.2013
 */
public abstract class StreamImportConfiguratorBase<STATE extends StreamImportStateBase, SOURCE extends Object> extends ImportConfiguratorBase<STATE, SOURCE> implements IImportConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StreamImportConfiguratorBase.class);
	
	//partitions
	private boolean usePartitions = true;
	private int defaultPartitionSize = 2000;
	
	private String sourceReferenceTitle = null;

	//mapping
	private IImportMapping.MappingType mappingType = MappingType.InMemoryMapping;
	
	/**
	 * Constructor.
	 * @param transformer
	 */
	public StreamImportConfiguratorBase(IInputTransformer transformer) {
		super(transformer);
	}


	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			DwcaImport.class
		};
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		if (this.sourceReference == null){
			sourceReference = ReferenceFactory.newGeneric();
			if (StringUtils.isBlank(this.sourceReferenceTitle )){
				sourceReference.setTitleCache(getDefaultSourceReferenceTitle(), true);
			}else{
				sourceReference.setTitleCache(this.sourceReferenceTitle, true);
			}
		}
		if (getSourceRefUuid() != null){
			sourceReference.setUuid(getSourceRefUuid());
		}else{
			setSourceRefUuid(sourceReference.getUuid());
		}
		return sourceReference;
	}
	
	protected abstract String getDefaultSourceReferenceTitle();

	
	
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
	
	public String getSourceReferenceTitle() {
		return sourceReferenceTitle;
	}

	public void setSourceReferenceTitle(String sourceReferenceTitle) {
		this.sourceReferenceTitle = sourceReferenceTitle;
	}
	
	
}
