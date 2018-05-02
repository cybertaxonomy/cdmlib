/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 30.10.2008
 * @version 1.0
 */
public class DefaultTcsXmlPlaceholders implements ITcsXmlPlaceholderClass {
	private static final Logger logger = Logger.getLogger(DefaultTcsXmlPlaceholders.class);
	
	/* (non-Javadoc)
	 * @see tcsxml.ITcsXmlPlaceholderClass#makeMetaDataDetailed(tcsxml.TcsXmlImportConfigurator, org.jdom.Element)
	 */
	public boolean makeMetaDataDetailed(TcsXmlImportConfigurator tcsConfig, Element elMetaDataDetailed){
		if (tcsConfig == null){
			return false;
		}
		if (elMetaDataDetailed == null){
			return true;
		}
		
		//Do nothing
		//TODO implement EDIT TcsMetaData extension
		logger.info("MetaDataElement found: " +  elMetaDataDetailed.getName());
		return true;
	}

	/* (non-Javadoc)
	 * @see tcsxml.ITcsXmlPlaceholderClass#makePublicationDetailed(tcsxml.TcsXmlImportConfigurator, org.jdom.Element, eu.etaxonomy.cdm.model.reference.Reference)
	 */
	public boolean makePublicationDetailed(TcsXmlImportConfigurator config, Element elPublicationDetailed, Reference publication){
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		if (config == null){
			return false;
		}
		if (elPublicationDetailed == null){
			return true;
		}else{
			publication.setProtectedTitleCache(false);
		}
		
		String childName;
		Namespace tcsNs = config.getTcsXmlNamespace();
		for (Object o : elPublicationDetailed.getChildren()){
			Element element = (Element)o;
			String value = element.getTextNormalize();
			
			childName = "Title";
			if (element.getName().equalsIgnoreCase(childName) && element.getNamespace().equals(tcsNs)){
				publication.setTitle(value);
				continue;
			}

			childName = "DatePublished";
			if (element.getName().equalsIgnoreCase(childName) && element.getNamespace().equals(tcsNs)){
				TimePeriod datePublished = TimePeriodParser.parseString(value);
				publication.setDatePublished(datePublished);
				continue;
			}
			
			logger.warn("Unhandled Publication Detailed child element: " +  element.getName());
	
		}
//		String childName = "Title";
//		boolean obligatory = false;
//		Element elTitle = XmlHelp.getSingleChildElement(success, elPublicationDetailed, childName, ns, obligatory);
//		if (elTitle != null){
//			String strTitle = elTitle.getTextNormalize();
//			publication.setTitle(strTitle);
//		}

//		childName = "DatePublished";
//		obligatory = false;
//		ns = config.getTcsXmlNamespace();
//		Element elDatePublished = XmlHelp.getSingleChildElement(success, elPublicationDetailed, childName, ns, obligatory);
//		if (elDatePublished != null){
//			String strDatePublished = elDatePublished.getTextNormalize();
//			TimePeriod datePublished = TimePeriodParser.parseString(strDatePublished);
//			publication.setDatePublished(datePublished);
//		}
		
		//Do nothing
		//TODO implement EDIT TcsMetaData extension
		if (logger.isDebugEnabled()){logger.debug("PublicationDetailed element found: " +  elPublicationDetailed.getName());}
		return success.getValue();
	}
	
	
 
	

}
