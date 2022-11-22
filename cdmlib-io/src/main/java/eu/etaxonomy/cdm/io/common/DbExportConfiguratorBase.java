/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public abstract class DbExportConfiguratorBase<STATE extends ExportStateBase, TRANSFORM extends IExportTransformer, DEST extends Source>
        extends ExportConfiguratorBase<STATE, TRANSFORM, Source>
        implements IExportConfigurator<STATE, TRANSFORM>{

    private static final long serialVersionUID = 3776529518379378810L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

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

	public IdType getIdType() {
		return idType;
	}
	public void setIdType(IdType idType) {
		this.idType = idType;
	}

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

	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getDatabase();
		}
	}

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
