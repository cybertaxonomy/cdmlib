/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 *
 */
@Component
public class TcsXmlPublicationsImport extends TcsXmlImportBase implements ICdmIO<TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlPublicationsImport.class);

	private static int modCount = 1000;


	public TcsXmlPublicationsImport(){
		super();

	}

	@Override
	public boolean doCheck(TcsXmlImportState state){
		boolean result = true;
		result &= checkArticlesWithoutJournal(state.getConfig());
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



	@Override
	public void doInvoke(TcsXmlImportState state){

		logger.info("start make Publications ...");
		boolean success = true;
		String childName;
		boolean obligatory;

		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		IReferenceService referenceService = getReferenceService();

		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();

		DoubleResult<Element, Boolean> doubleResult;
		childName = "Publications";
		obligatory = false;
		doubleResult = XmlHelp.getSingleChildElement(elDataSet, childName, tcsNamespace, obligatory);
		success &= doubleResult.getSecondResult();
		Element elPublications = doubleResult.getFirstResult();

		String tcsElementName = "Publication";
		String idNamespace = "Publication";
		List<Element> elPublicationList = elPublications == null ? new ArrayList<Element>() : elPublications.getChildren(tcsElementName, tcsNamespace);

		int i = 0;
		//for each taxonName
		for (Element elPublication : elPublicationList){
			if ((++i % modCount) == 0){ logger.info("publications handled: " + (i-1));}

			//create TaxonName element
			String strId = elPublication.getAttributeValue("id");

			childName = "Simple";
			obligatory = true;
			doubleResult = XmlHelp.getSingleChildElement(elPublication, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elSimple = doubleResult.getFirstResult();

			String simple = elSimple.getTextNormalize();

			Reference reference = ReferenceFactory.newGeneric();
			reference.setTitleCache(simple, true);

			childName = "PublicationDetailed";
			obligatory = false;
			doubleResult =  XmlHelp.getSingleChildElement(elPublication, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elPublicationDetailed = doubleResult.getFirstResult();

			success &= config.getPlaceholderClass().makePublicationDetailed(config, elPublicationDetailed, reference);
			ImportHelper.setOriginalSource(reference, config.getSourceReference(), strId, idNamespace);

			referenceMap.put(strId, reference);


		}
//		//save and store in map
//		logger.info("Save nomenclatural references (" + nomRefCount + ")");
//		referenceService.saveReferenceAll(nomRefMap.objects());
		logger.info("Save bibliographical references (" + i +")");
		referenceService.save(referenceMap.objects());

		logger.info("end make publications ...");
		if (!success){
			state.setUnsuccessfull();
		}
		return;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
    protected boolean isIgnore(TcsXmlImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
