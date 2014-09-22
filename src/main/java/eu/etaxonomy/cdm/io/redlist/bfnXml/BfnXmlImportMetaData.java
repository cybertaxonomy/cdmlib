/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.redlist.bfnXml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.management.ObjectInstance;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.id.UUIDGenerator;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
/**
 * 
 * @author a.oppermann
 * @date 04.07.2013
 *
 */
@Component
public class BfnXmlImportMetaData extends BfnXmlImportBase implements ICdmIO<BfnXmlImportState> {
	private static final Logger logger = Logger.getLogger(BfnXmlImportMetaData.class);

	public BfnXmlImportMetaData(){
		super();
	}

	@Override
	public boolean doCheck(BfnXmlImportState state){
		boolean result = true;
		//TODO needs to be implemented
		return result;
	}

	@Override
	public void doInvoke(BfnXmlImportState state){
		logger.info("start import MetaData...");
		
		
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);

		BfnXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace bfnNamespace = config.getBfnXmlNamespace();
		
		List contentXML = elDataSet.getContent();
		Element currentElement = null;
		for(Object object:contentXML){
		
			if(object instanceof Element){
				currentElement = (Element)object;

				if(currentElement.getName().equalsIgnoreCase("METADATEN")){
					
					TransactionStatus tx = startTransaction();

					String bfnElementName = "METADATEN";
					List<Element> elMetaDataList  = (List<Element>)currentElement.getChildren();
					//for each taxonName
					for (Element elMetaData : elMetaDataList){
						if( elMetaData.getAttributeValue("standardname").equalsIgnoreCase("KurzLit_A")){
							List<Element> children = (List<Element>)elMetaData.getChildren();
							String kurzlitA = children.get(0).getTextNormalize();
							Reference sourceReference = ReferenceFactory.newDatabase();
							sourceReference.setTitle(kurzlitA);
							state.setFirstListSecRef(sourceReference);

						}
						else if( elMetaData.getAttributeValue("standardname").equalsIgnoreCase("KurzLit_B")){
							List<Element> children = (List<Element>)elMetaData.getChildren();
							String kurzlitB = children.get(0).getTextNormalize();
							Reference sourceReference = ReferenceFactory.newDatabase();
							sourceReference.setTitle(kurzlitB);
							state.setSecondListSecRef(sourceReference);
						}
					}
					
					commitTransaction(tx);
					
					logger.info("end import MetaData ...");
					
					
					
					if (!success.getValue()){
						state.setUnsuccessfull();
					}
					//FIXME: Only take the first RoteListeData Features
					
					
					return;
				}
			}
		}
		return;

	}

	@Override
	protected boolean isIgnore(BfnXmlImportState state) {
		return ! state.getConfig().isDoTaxonNames();
	}

}
