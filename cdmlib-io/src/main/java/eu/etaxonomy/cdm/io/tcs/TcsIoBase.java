/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcs;

import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;

/**
 * @author a.mueller
 * @created 04.08.2008
 * @version 1.0
 */
public abstract class TcsIoBase  extends CdmIoBase {
	private static final Logger logger = Logger.getLogger(TcsIoBase.class);

	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");

	
	protected boolean makeStandardMapper(Element parentElement, CdmBase ref, Set<String> omitAttributes, CdIoXmlMapperBase[] classMappers){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;	
		for (CdIoXmlMapperBase mapper : classMappers){
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
	
	
	private Object getValue(CdIoXmlMapperBase mapper, Element parentElement){
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


	
	protected boolean checkAdditionalContents(Element parentElement, CdIoXmlMapperBase[] classMappers, CdIoXmlMapperBase[] unclearMappers){
		List<Content> additionalContentList = new ArrayList<Content>();
		List<Content> contentList = parentElement.getContent();
		for(Content content: contentList){
			boolean contentExists = false;
			if (content instanceof Element){
				Element elementContent = (Element)content;
				for (CdIoXmlMapperBase mapper : classMappers){
					if (mapper.mapsSource(content, parentElement)){
						contentExists = true;
						break;
					}
				}
				for (CdIoXmlMapperBase mapper : unclearMappers){
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

}
