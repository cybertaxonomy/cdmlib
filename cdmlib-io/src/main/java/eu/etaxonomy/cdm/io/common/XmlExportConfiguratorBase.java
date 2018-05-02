/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public abstract class  XmlExportConfiguratorBase<STATE extends XmlExportState<?>>
        extends ExportConfiguratorBase<STATE, IExportTransformer, File>
        implements IExportConfigurator<STATE, IExportTransformer>{

    private static final long serialVersionUID = 6078292713506530756L;
    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(XmlExportConfiguratorBase.class);

	public enum IdType{
		CDM_ID,
		ORIGINAL_SOURCE_ID
	}

	private IdType idType = IdType.CDM_ID;


	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	protected XmlExportConfiguratorBase(File destination, ICdmDataSource cdmSource, IExportTransformer transformer) {
	   super(transformer);
	   setSource(cdmSource);
	   setDestination(destination);

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

	@Override
    public String getDestinationNameString() {
		if (getDestination() != null){
			return getDestination().getName();
		}else{
			return "";
		}
	}

}
