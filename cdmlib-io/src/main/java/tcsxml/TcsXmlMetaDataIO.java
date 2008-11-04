/**
 * 
 */
package tcsxml;

import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

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
		
	private static boolean checkArticlesWithoutJournal(IImportConfigurator bmiConfig){
		try {
			boolean result = true;
			//TODO
			//				result = firstRow = false;
//			}
//			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	protected static CdmIoXmlMapperBase[] standardMappers = new CdmIoXmlMapperBase[]{
		//new CdmTextElementMapper("edition", "edition"),
		new CdmTextElementMapper("volume", "volume"),
		new CdmTextElementMapper("placePublished", "placePublished"),
		new CdmTextElementMapper("publisher", "publisher"),
		//new CdmTextElementMapper("isbn", "isbn"),
		new CdmTextElementMapper("pages", "pages"),
		//new CdmTextElementMapper("series", "series"),
		//new CdmTextElementMapper("issn", "issn"),
		//new CdmTextElementMapper("url", "uri")
	};
	
	protected static CdmIoXmlMapperBase[] operationalMappers = new CdmIoXmlMapperBase[]{
		new CdmUnclearMapper("year")
		, new CdmUnclearMapper("title")
		, new CdmUnclearMapper("shortTitle")
		, new CdmUnclearMapper("publicationType")
		, new CdmUnclearMapper("parentPublication")
		, new CdmUnclearMapper("authorship")
		
	};
	
//	protected static String[] createdAndNotesAttributes = new String[]{
//			"created_When", "updated_When", "created_Who", "updated_Who", "notes"
//	};
	
	protected static CdmIoXmlMapperBase[] unclearMappers = new CdmIoXmlMapperBase[]{
		
	};


	
	private boolean makeStandardMapper(Element parentElement, StrictReferenceBase ref, Set<String> omitAttributes){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;	
		for (CdmIoXmlMapperBase mapper : standardMappers){
			Object value = getValue(mapper, parentElement);
			//write to destination
			if (value != null){
				String destinationAttribute = mapper.getDestinationAttribute();
				if (! omitAttributes.contains(destinationAttribute)){
					result &= ImportHelper.addValue(value, ref, destinationAttribute, mapper.getTypeClass(), OVERWRITE, OBLIGATORY);
				}
			}
		}
		return true;
	}
	
	private Object getValue(CdmIoXmlMapperBase mapper, Element parentElement){
		String sourceAttribute = mapper.getSourceAttribute().toLowerCase();
		Namespace sourceNamespace = mapper.getSourceNamespace(parentElement);
		Element child = parentElement.getChild(sourceAttribute, sourceNamespace);
		if (child == null){
			return null;
		}
		if (child.getContentSize() > 1){
			logger.warn("Element is not String");
		}
		Object value = child.getTextTrim();
		return value;
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
