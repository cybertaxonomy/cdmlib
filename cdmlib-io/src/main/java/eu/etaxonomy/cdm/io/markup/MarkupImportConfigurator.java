/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.markup;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.XmlImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

@Component
public class MarkupImportConfigurator extends XmlImportConfiguratorBase<MarkupImportState> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(MarkupImportConfigurator.class);
	
	public static MarkupImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new MarkupImportConfigurator(uri, destination);
	}
	
	private boolean doTaxa = true;

	
	//TODO
	private static IInputTransformer defaultTransformer = null;
	private String classificationTitle = "E-Flora Import";
	private String sourceReferenceTitle = "E-Flora";
	private UUID defaultLanguageUuid;
	
	//TODO move to state, but a state gets lost after each import.invoke, so I can't move this information
	//from the one import to another import in case I run 2 imports in line
	private UUID lastTaxonUuid;
	
	//if true, the keys will be printed after they have been created	
	private boolean doPrintKeys = false;

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			MarkupDocumentImport.class
		};
	};
	
	protected MarkupImportConfigurator() {
		super(defaultTransformer);
	}
	
	
	/**
	 * 
	 */
	protected MarkupImportConfigurator(IInputTransformer transformer) {
		super(transformer);
	}
	

	/**
	 * @param url
	 * @param destination
	 */
	protected MarkupImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}
	
	/**
	 * @param url
	 * @param destination
	 */
	protected MarkupImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
		super(transformer);
		setSource(uri);
		setDestination(destination);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public MarkupImportState getNewState() {
		return new MarkupImportState(this);
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
			sourceReference.setTitleCache(sourceReferenceTitle, true);
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

	public void setClassificationTitle(String classificationTitle) {
		this.classificationTitle = classificationTitle;
	}

	public String getClassificationTitle() {
		return classificationTitle;
	}
	
	

	public UUID getLastTaxonUuid() {
		return lastTaxonUuid;
	}
	
	public void setLastTaxonUuid(UUID lastTaxonUuid) {
		this.lastTaxonUuid = lastTaxonUuid;
	}

	public void setDoPrintKeys(boolean doPrintKeys) {
		this.doPrintKeys = doPrintKeys;
	}

	public boolean isDoPrintKeys() {
		return doPrintKeys;
	}

	public UUID getDefaultLanguageUuid() {
		return this.defaultLanguageUuid;
	}

	public void setDefaultLanguageUuid(UUID defaultLanguageUuid) {
		this.defaultLanguageUuid = defaultLanguageUuid;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}




	
}
