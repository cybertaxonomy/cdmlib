package eu.etaxonomy.cdm.io.unitsPortal;


import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;

public class SpecimenIoBase  extends CdmIoBase {
	private static final Logger logger = Logger.getLogger(SpecimenIoBase.class);

	@Override
	protected boolean doCheck(IImportConfigurator config) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
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