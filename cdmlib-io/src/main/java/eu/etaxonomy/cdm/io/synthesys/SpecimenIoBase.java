/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.synthesys;


import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.taxonx.TaxonXImportConfigurator;
import eu.etaxonomy.cdm.io.taxonx.TaxonXImportState;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author p.kelbert
 * @created 29.10.2008
 * @version 1.0
 */
public class SpecimenIoBase extends CdmIoBase<IImportConfigurator> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenIoBase.class);

	@Override
	protected boolean doCheck(IImportConfigurator config) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 
		SpecimenImportState state = ((SpecimenImportConfigurator)config).getState();
		state.setConfig((SpecimenImportConfigurator)config);
		return doInvoke(state);
	}
	
	public boolean doInvoke(SpecimenImportState state){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean invoke(IImportConfigurator config, Map stores) {
		// TODO Auto-generated method stub
		return false;
	}

}