/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.io.common.mapping.IXmlMapper;
import eu.etaxonomy.cdm.io.common.mapping.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.tcsxml.CdmSingleAttributeXmlMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 24.03.2009
 */
public class CdmOneToManyXmlMapper<ONE extends CdmBase, MANY extends CdmBase, SINGLE_MAPPER extends CdmSingleAttributeXmlMapperBase> extends
		CdmOneToManyMapper<ONE, MANY, SINGLE_MAPPER> implements IXmlMapper{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmOneToManyXmlMapper.class);

	public CdmOneToManyXmlMapper(Class<ONE> oneClass, Class<MANY> manyClass, String singleAttributeName, SINGLE_MAPPER[] singleAttributesMappers) {
		super(oneClass, manyClass, singleAttributeName, singleAttributesMappers);
	}


	@Override
    public boolean mapsSource(Content content, Element parentElement) {
		if (! (content instanceof Element)){
			return false;
		}
		Element element = (Element)content;
		if (content == null){
			return false;
		}else if (! getSourceAttributes().contains(element.getName())){
			return false;
		}
		for (String sourceElement: getSourceAttributeList()){
			Namespace thisNamespace = getSourceNamespace(sourceElement, parentElement);
			if (thisNamespace == null){
				if (element.getNamespace() == null){
					return true;
				}
			}else if (thisNamespace.equals(element.getNamespace())){
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns the namespace for the source element sourceElement. If not defined it returns the namespace
	 * of the parent element.
	 * @param sourceElement
	 * @param parentElement
	 * @return
	 */
	private Namespace getSourceNamespace(String sourceElement, Element parentElement){
		//TODO
		//namespaces for single attributes not yet implemented
		return parentElement.getNamespace();
	}

}
