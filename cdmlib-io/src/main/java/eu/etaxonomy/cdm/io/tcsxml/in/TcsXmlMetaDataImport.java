/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.tcsxml.in;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;

/**
 * @author a.mueller
 *
 */
@Component
public class TcsXmlMetaDataImport extends TcsXmlImportBase implements ICdmIO<TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlMetaDataImport.class);

	private static int modCount = 1000;
	
	public TcsXmlMetaDataImport(){
		super();
	}
	
	@Override
	public boolean doCheck(TcsXmlImportState state){
		boolean result = true;
		//result &= checkArticlesWithoutJournal(config);
		//result &= checkPartOfJournal(config);
		
		return result;
	}
		

	
	@Override
	public void doInvoke(TcsXmlImportState state){
		logger.info("start make MetaData ...");
		boolean success = true;
		String childName;
		boolean obligatory;
		
//		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)stores.get(ICdmIO.REFERENCE_STORE);
		
		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();
		
		DoubleResult<Element, Boolean> doubleResult;
		childName = "MetaData";
		obligatory = false;
		doubleResult = XmlHelp.getSingleChildElement(elDataSet, childName, tcsNamespace, obligatory);
		success &= doubleResult.getSecondResult();
		Element elMetaData = doubleResult.getFirstResult();
		
		childName = "Simple";
		obligatory = true;
		doubleResult = XmlHelp.getSingleChildElement(elMetaData, childName, tcsNamespace, obligatory);
		success &= doubleResult.getSecondResult();
		Element elSimple = doubleResult.getFirstResult();
		//TODO do simple MetaData
		if (elSimple != null && (elSimple.getChildren().size() > 0 || elSimple.getAttributes().size() > 0 )){
			logger.warn("Simple Metadata not handled yet");
		}
		
		childName = "MetaDataDetailed";
		obligatory = false;
		doubleResult =  XmlHelp.getSingleChildElement(elMetaData, childName, tcsNamespace, obligatory);
		success &= doubleResult.getSecondResult();
		Element elMetaDataDetailed = doubleResult.getFirstResult();

		success &= config.getPlaceholderClass().makeMetaDataDetailed(config, elMetaDataDetailed);
//		try {
//			List<Object> args = Arrays.asList(tcsConfig, elMetaDataDetailed);
//			tcsConfig.getFunctionMetaDataDetailed().invoke(this, args);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//			success = false;
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//			success = false;
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//			success = false;
//		}
	
		logger.info("end make MetaData ...");
		if (!success){
			state.setUnsuccessfull();
		}
		return;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TcsXmlImportState state){
		TcsXmlImportConfigurator tcsConfig = state.getConfig();
		return (! tcsConfig.isDoMetaData());
	}
	
}
