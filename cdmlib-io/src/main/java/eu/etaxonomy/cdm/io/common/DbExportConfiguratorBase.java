/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.03.2008
 */
public abstract class DbExportConfiguratorBase<STATE extends ExportStateBase, TRANSFORM extends IExportTransformer, DEST extends Source>
        extends ExportConfiguratorBase<STATE, TRANSFORM, Source>
        implements IExportConfigurator<STATE, TRANSFORM>{

    private static final long serialVersionUID = 3776529518379378810L;
    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DbExportConfiguratorBase.class);


	public enum IdType{
		CDM_ID,
		CDM_ID_WITH_EXCEPTIONS,
		ORIGINAL_SOURCE_ID,
		MAX_ID
	}

	private IdType idType = IdType.CDM_ID;

	public DbExportConfiguratorBase(TRANSFORM transformer) {
		super(transformer);
	}


	/**
	 * @return the idType
	 */
	public IdType getIdType() {
		return idType;
	}

	/**
	 * @param idType the idType to set
	 */
	public void setIdType(IdType idType) {
		this.idType = idType;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoConfigurator#getDestinationNameString()
	 */
	@Override
    public String getDestinationNameString() {
		if (getDestination() != null){
			return getDestination().getDatabase();
		}else{
			return null;
		}
	}


	@Override
    public ICdmDataSource getSource() {
		return super.getSource();
	}
	@Override
    public void setSource(ICdmDataSource cdmSource) {
		super.setSource(cdmSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getDatabase();
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getSourceReference()
	 */
	@Override
    public IDatabase getSourceReference() {

		if (sourceReference == null){
			sourceReference =  ReferenceFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase(), true);
			}
		}
		return sourceReference;
	}
}
