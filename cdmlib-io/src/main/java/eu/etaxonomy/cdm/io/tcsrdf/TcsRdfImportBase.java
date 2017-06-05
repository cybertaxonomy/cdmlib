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
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.mapping.IXmlMapper;
import eu.etaxonomy.cdm.io.tcsxml.CdmSingleAttributeXmlMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 04.08.2008
 */
public abstract class TcsRdfImportBase  extends CdmImportBase<TcsRdfImportConfigurator, TcsRdfImportState> {
    private static final long serialVersionUID = -8765093240068562907L;

    private static final Logger logger = Logger.getLogger(TcsRdfImportBase.class);

	protected static String nsTcom = "http://rs.tdwg.org/ontology/voc/Common#";
	protected static String nsTn = "http://rs.tdwg.org/ontology/voc/TaxonName#";
	protected static String nsTgeo = "http://rs.tdwg.org/ontology/voc/GeographicRegion#";
	protected static String nsTc = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
	protected static String nsTpub = "http://rs.tdwg.org/ontology/voc/PublicationCitation#";
	protected static String nsTm = "http://rs.tdwg.org/ontology/voc/Team";
	protected static String nsTpalm = "http://wp5.e-taxonomy.eu/import/palmae/common";


	@Override
    protected abstract void doInvoke(TcsRdfImportState state);

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

	protected boolean makeStandardMapper(Statement resource, CdmBase ref, Set<String> omitAttributes, CdmSingleAttributeRDFMapperBase[] classMappers){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;
		for (CdmSingleAttributeRDFMapperBase mapper : classMappers){
			Object value = getValue(mapper, resource);
			//write to destination
			if (value != null){
				String destinationAttribute = mapper.getDestinationAttribute();
				if (! omitAttributes.contains(destinationAttribute)){
					result &= ImportHelper.addValue(value, ref, destinationAttribute, mapper.getTypeClass(), OVERWRITE, OBLIGATORY);
				}
			}
		}
		return result;
	}

	public Object getValue(CdmSingleAttributeRDFMapperBase mapper, Statement resource){
		String sourceAttribute = mapper.getSourceAttribute();
		String sourceNamespace = mapper.getSourceNameSpace(resource);
		String value = resource.getProperty(resource.getModel().createProperty(sourceNamespace+sourceAttribute)).getString();

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
