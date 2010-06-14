/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IXmlMapper;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 04.08.2008
 * @version 1.0
 */
public abstract class TcsRdfImportBase  extends CdmImportBase<TcsRdfImportConfigurator, TcsRdfImportState> {
	private static final Logger logger = Logger.getLogger(TcsRdfImportBase.class);

	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");
	protected static Namespace nsTn = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonName#");
	protected static Namespace nsTgeo = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/GeographicRegion#");
	protected static Namespace nsTc = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonConcept#");
	protected static Namespace nsTpub = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/PublicationCitation#");
	protected static Namespace nsTpalm = Namespace.getNamespace("http://wp5.e-taxonomy.eu/import/palmae/common");
	
	
	protected abstract boolean doInvoke(TcsRdfImportState state);

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
//	 */
//	@Override
//	protected boolean doInvoke(IImportConfigurator config, 
//			Map<String, MapWrapper<? extends CdmBase>> stores){ 
//		TcsRdfImportState state = ((TcsRdfImportConfigurator)config).getState();
//		state.setConfig((TcsRdfImportConfigurator)config);
//		return doInvoke(state);
//	}
	
	protected boolean makeStandardMapper(Element parentElement, CdmBase ref, Set<String> omitAttributes, CdmSingleAttributeXmlMapperBase[] classMappers){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;	
		for (CdmSingleAttributeXmlMapperBase mapper : classMappers){
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
	
	private Object getValue(CdmSingleAttributeXmlMapperBase mapper, Element parentElement){
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
	
	protected boolean checkAdditionalContents(Element parentElement, IXmlMapper[] classMappers, CdmSingleAttributeXmlMapperBase[] operationalMappers, CdmSingleAttributeXmlMapperBase[] unclearMappers){
		List<Content> additionalContentList = new ArrayList<Content>();
		List<Content> contentList = parentElement.getContent();
		List<IXmlMapper> mapperList = new ArrayList<IXmlMapper>();
		
		mapperList.addAll(Arrays.asList(classMappers));
		mapperList.addAll(Arrays.asList(operationalMappers));
		mapperList.addAll(Arrays.asList(unclearMappers));
		
		for(Content content: contentList){
			boolean contentExists = false;
			if (content instanceof Element){
				Element elementContent = (Element)content;
				for (IXmlMapper mapper : mapperList){
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
