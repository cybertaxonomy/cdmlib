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

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
public class TaxonXImportConfigurator extends ImportConfiguratorBase<TaxonXImportState, URI> implements IImportConfigurator {
//	private static final Logger logger = Logger.getLogger(TaxonXImportConfigurator.class);

	//if true the information in the mods part (taxonxHeader)
	private boolean doMods = true;
	private boolean doFacts = true;
	private boolean doTypes = true;


	//TODO
	private static IInputTransformer defaultTransformer = null;


	//if false references in this rdf file are not published in the bibliography list
	private boolean isPublishReferences = true;

	private String originalSourceTaxonNamespace = "TaxonConcept";
	private String originalSourceId;

	private static Reference<?> sourceRef = null;

	@SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList(){
		ioClassList = new Class[]{
		        TaxonXImport.class,
		};
	}

	/**
	 * @param uri
	 * @param destination
	 * @return
	 */
	public static TaxonXImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new TaxonXImportConfigurator(uri, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private TaxonXImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}

	/**
     * @param url
     * @param destination
     */
    private TaxonXImportConfigurator(ICdmDataSource destination) {
        super(defaultTransformer);
        setDestination(destination);
    }



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	@SuppressWarnings("unchecked")
    @Override
    public TaxonXImportState getNewState() {
		return new TaxonXImportState(this);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@SuppressWarnings("rawtypes")
    @Override
	public Reference getSourceReference() {
	    if (sourceReference == null){
            sourceReference =  ReferenceFactory.newGeneric();

            if (getSourceRefUuid() != null){
                sourceReference.setUuid(getSourceRefUuid());
            }
            if (sourceRef != null){
                sourceReference.setTitleCache(sourceRef.getTitleCache(), true);
            }
        }
        return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}

	public String getOriginalSourceTaxonNamespace() {
		return originalSourceTaxonNamespace;
	}

	public void setOriginalSourceTaxonNamespace(String originalSourceTaxonNamespace) {
		this.originalSourceTaxonNamespace = originalSourceTaxonNamespace;
	}

	public String getOriginalSourceId() {
		return originalSourceId;
	}

	public void setOriginalSourceId(String originalSourceId) {
		this.originalSourceId = originalSourceId;
	}


	/**
	 * @return the doMods
	 */
	public boolean isDoMods() {
		return doMods;
	}

	/**
	 * @param doMods the doMods to set
	 */
	public void setDoMods(boolean doMods) {
		this.doMods = doMods;
	}


	public boolean isDoFacts() {
		return doFacts;
	}
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}



	public boolean isDoTypes() {
		return doTypes;
	}
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
	}


	/**
	 * @return the isPublishReferences
	 */
	public boolean isPublishReferences() {
		return isPublishReferences;
	}

	/**
	 * @param isPublishReferences the isPublishReferences to set
	 */
	public void setPublishReferences(boolean isPublishReferences) {
		this.isPublishReferences = isPublishReferences;
	}

    /**
     * @param b
     */
    public void setDoAutomaticParsing(boolean b) {

    }

    /**
     * @param destination
     * @return
     */
    public static TaxonXImportConfigurator NewInstance(ICdmDataSource destination) {
        return new TaxonXImportConfigurator(destination);
    }

    /**
     * @param reference
     */
    public static void setSourceRef(Reference<?> reference) {
       sourceRef = reference;

    }


}
