// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class BerlinModelExportState extends DbExportStateBase<BerlinModelExportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BerlinModelExportState.class);

	
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.IoStateBase#initialize(eu.etaxonomy.cdm.io.common.IIoConfigurator)
//	 */
//	@Override
//	public void initialize(BerlinModelExportConfigurator config) {
//				
//	}

	
	public BerlinModelExportState(BerlinModelExportConfigurator config) {
		super(config);
	}


	private Integer nextRefDetailId = null;
	private Integer nextFactCategoryId = null;
	
	/**
	 * @return the nextRefDetailId
	 */
	public Integer getNextRefDetailId() {
		if (nextRefDetailId == null){
			//TODO
			nextRefDetailId = 1;
		}
		return nextRefDetailId++;
	}
	
	/**
	 * @return the nextRefDetailId
	 */
	public Integer getNextFactCategoryId() {
		if (nextFactCategoryId == null){
			//TODO
			nextFactCategoryId = 30;
		}
		return nextFactCategoryId++;
	}


	/**
	 * @param nextRefDetailId the nextRefDetailId to set
	 */
	public void setNextFactCategoryId(Integer nextFactCategoryId) {
		this.nextFactCategoryId = nextFactCategoryId;
	}

}
