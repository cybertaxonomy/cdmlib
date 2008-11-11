/**
 * 
 */
package eu.etaxonomy.cdm.io.tcsxml;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 *
 */
public class TcsXmlMetaDataIO extends TcsXmlIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsXmlMetaDataIO.class);

	private static int modCount = 1000;
	
	public TcsXmlMetaDataIO(){
		super();
	}
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		//result &= checkArticlesWithoutJournal(config);
		//result &= checkPartOfJournal(config);
		
		return result;
	}
		



	

	
	@Override
	public boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores){
		logger.info("start make MetaData ...");
		boolean success = true;
		String childName;
		boolean obligatory;
		
//		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		Element elDataSet = getDataSetElement(tcsConfig);
		Namespace tcsNamespace = tcsConfig.getTcsXmlNamespace();
		
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
		
		childName = "MetaDataDetailed";
		obligatory = false;
		doubleResult =  XmlHelp.getSingleChildElement(elMetaData, childName, tcsNamespace, obligatory);
		success &= doubleResult.getSecondResult();
		Element elMetaDataDetailed = doubleResult.getFirstResult();

		success &= tcsConfig.getPlaceholderClass().makeMetaDataDetailed(tcsConfig, elMetaDataDetailed);
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
		return success;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		TcsXmlImportConfigurator tcsConfig = (TcsXmlImportConfigurator)config;
		return (! tcsConfig.isDoMetaData());
	}
	
}
