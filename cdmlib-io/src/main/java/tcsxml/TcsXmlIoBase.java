/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package tcsxml;

import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @created 04.08.2008
 * @version 1.0
 */
public abstract class TcsXmlIoBase  extends CdmIoBase {
	private static final Logger logger = Logger.getLogger(TcsXmlIoBase.class);

	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");
	protected static Namespace nsTn = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonName#");
	protected static Namespace nsTgeo = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/GeographicRegion#");
	protected static Namespace nsTc = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonConcept#");
	protected static Namespace nsTpub = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/PublicationCitation#");
	protected static Namespace nsTpalm = Namespace.getNamespace("http://wp5.e-taxonomy.eu/import/palmae/common");
	
	
	protected boolean makeStandardMapper(Element parentElement, CdmBase ref, Set<String> omitAttributes, CdmIoXmlMapperBase[] classMappers){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;	
		for (CdmIoXmlMapperBase mapper : classMappers){
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
		String sourceAttribute = mapper.getSourceAttribute();
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
	
	protected boolean checkAdditionalContents(Element parentElement, CdmIoXmlMapperBase[] classMappers, CdmIoXmlMapperBase[] operationalMappers, CdmIoXmlMapperBase[] unclearMappers){
		List<Content> additionalContentList = new ArrayList<Content>();
		List<Content> contentList = parentElement.getContent();
		List<CdmIoXmlMapperBase> mapperList = new ArrayList<CdmIoXmlMapperBase>();
		
		mapperList.addAll(Arrays.asList(classMappers));
		mapperList.addAll(Arrays.asList(operationalMappers));
		mapperList.addAll(Arrays.asList(unclearMappers));
		
		for(Content content: contentList){
			boolean contentExists = false;
			if (content instanceof Element){
				Element elementContent = (Element)content;
				for (CdmIoXmlMapperBase mapper : mapperList){
					if (mapper.mapsSource(content, parentElement)){
						contentExists = true;
						break;
					}
				}
				
			}else if (content instanceof Text){
				//empty Text
				if (((Text)content).getTextNormalize().equals("")){
					contentExists = true;
				}else{
					//
				}
			}
			
			if (contentExists == false){
				additionalContentList.add(content);
			}
		}
		for (Content additionalContent : additionalContentList){
			logger.warn("Additional content: " +  additionalContent);
		}
		return (additionalContentList.size() == 0);
	}
	
	protected Element getDataSetElement(TcsXmlImportConfigurator tcsConfig){
		Element root = tcsConfig.getSourceRoot();
		
		if (! "DataSet".equals(root.getName())){
			logger.error("Root element is not 'DataSet'");
			return null;
		}
		if (tcsConfig.getTcsXmlNamespace() == null){
			logger.error("No namespace defined for tcs");
			return null;
		}
		if (! tcsConfig.getTcsXmlNamespace().equals(root.getNamespace())){
			logger.error("Wrong namespace for element 'DataSet'");
			return null;
		}
		//TODO prevent multiple elements
		
		return root;
	}
	
	static public boolean checkFirstTwoFunctionElements(List<Object> objList){
		if (! (objList.get(0) instanceof TcsXmlImportConfigurator)){
			logger.error("first method object has wrong type. Must be " + TcsXmlImportConfigurator.class.getSimpleName() + " but is " + (objList.get(0) == null ? "null": objList.get(0).getClass().getSimpleName()));
			return false;
		}
		if (! (objList.get(1) == null) && ! (objList.get(1) instanceof Element)){
			logger.error("first method object has wrong type. Must be " + Element.class.getSimpleName() + " but is " + (objList.get(1) == null ? "null": objList.get(1).getClass().getSimpleName()));
			return false;
		}
		return true;
	}


}
