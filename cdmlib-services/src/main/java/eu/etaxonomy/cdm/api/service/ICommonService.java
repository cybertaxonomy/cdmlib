// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.OriginalSource;


public interface ICommonService extends IService<OriginalSource>{
//	
//	/** find cdmBase by UUID**/
//	public abstract CdmBase getCdmBaseByUuid(UUID uuid);
//
//	/** save a reference and return its UUID**/
//	public abstract UUID saveCdmBase(CdmBase cdmBase);

	/** find cdmBase by UUID**/
	public abstract ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace);
}
