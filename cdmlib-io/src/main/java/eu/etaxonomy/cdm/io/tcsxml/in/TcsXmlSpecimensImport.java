/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.Specimen;

/**
 * @author a.mueller
 *
 */
@Component
public class TcsXmlSpecimensImport extends TcsXmlImportBase implements ICdmIO<TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlSpecimensImport.class);

	private static int modCount = 1000;
	
	public TcsXmlSpecimensImport(){
		super();
	}
	
	@Override
	public boolean doCheck(TcsXmlImportState config){
		boolean result = true;
		//result &= checkArticlesWithoutJournal(config);
		//result &= checkPartOfJournal(config);
		
		return result;
	}
		
	private static boolean checkXXX(IImportConfigurator tcsConfig){
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
	public boolean doInvoke(TcsXmlImportState state){
		logger.info("start make Specimens ...");
		
		MapWrapper<Specimen> specimenMap = (MapWrapper<Specimen>)state.getStore(ICdmIO.SPECIMEN_STORE);

		boolean success = true;
		String childName;
		boolean obligatory;

		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();
		
		DoubleResult<Element, Boolean> doubleResult;
		childName = "Specimens";
		obligatory = false;
		doubleResult = XmlHelp.getSingleChildElement(elDataSet, childName, tcsNamespace, obligatory);
		success &= doubleResult.getSecondResult();
		Element elSpecimens = doubleResult.getFirstResult();
		
		String tcsElementName = "Specimen";
		List<Element> elSpecimenList = elSpecimens.getChildren(tcsElementName, tcsNamespace);

		
		int i = 0;
		//for each taxonName
		for (Element elSpecimen : elSpecimenList){
			
			if ((++i % modCount) == 0){ logger.info("specimen handled: " + (i-1));}
			
			//create TaxonName element
			String strId = elSpecimen.getAttributeValue("id");
			
			childName = "Simple";
			obligatory = true;
			doubleResult = XmlHelp.getSingleChildElement(elSpecimen, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elSimple = doubleResult.getFirstResult();

			String simple = elSimple.getTextNormalize();
			Specimen specimen = Specimen.NewInstance();
			specimen.setTitleCache(simple);
			
			
			childName = "Collection";
			obligatory = false;
			doubleResult = XmlHelp.getSingleChildElement(elSpecimen, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elCollection = doubleResult.getFirstResult();
			success &= makeCollection(specimen, elCollection);
			Collection collection = specimen.getCollection();
			
			childName = "Institution";
			obligatory = false;
			doubleResult = XmlHelp.getSingleChildElement(elSpecimen, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elInstitution = doubleResult.getFirstResult();
			success &= makeInstitution(specimen, elInstitution);

			childName = "SpecimenItem";
			obligatory = true;
			doubleResult = XmlHelp.getSingleChildElement(elSpecimen, childName, tcsNamespace, obligatory);
			success &= doubleResult.getSecondResult();
			Element elSpecimenItem = doubleResult.getFirstResult();
			makeSpecimenItem(specimen, elSpecimenItem);
			
			specimenMap.put(strId, specimen);
		}
		
		logger.info("Save specimen (" + i +")");
	    getOccurrenceService().save((java.util.Collection)specimenMap.objects());

		logger.info("end make Specimens ...");
		return success;

	}
	
	private boolean makeInstitution(Specimen specimen, Element elInstitution){
		boolean success = true;
		Institution institution = null;
		if (specimen == null){
			logger.warn("No specimen defined");
			return false;
		}
		if (elInstitution != null){
			Namespace ns = elInstitution.getNamespace();
			institution = Institution.NewInstance();

			String childName = "InstitutionName";
			boolean obligatory = false;
			DoubleResult<Element, Boolean> doubleResult = XmlHelp.getSingleChildElement(elInstitution, childName, ns, obligatory);
			success &= doubleResult.getSecondResult();
			Element elInstitutionName = doubleResult.getFirstResult();
			if(elInstitutionName != null){
				String institutionName = elInstitutionName.getTextNormalize();
				institution.setName(institutionName);
			
				childName = "Code";
				obligatory = true;
				doubleResult = XmlHelp.getSingleChildElement(elInstitution, childName, ns, obligatory);
				success &= doubleResult.getSecondResult();
				Element elCode = doubleResult.getFirstResult();
				String code = elCode.getTextNormalize();
				institution.setName(code);
	
				childName = "Address";
				obligatory = true;
				doubleResult = XmlHelp.getSingleChildElement(elInstitution, childName, ns, obligatory);
				success &= doubleResult.getSecondResult();
				Element elAddress = doubleResult.getFirstResult();
				String address = elAddress.getTextNormalize();
				institution.setName(address);
	
				childName = "URL";
				obligatory = true;
				doubleResult = XmlHelp.getSingleChildElement(elInstitution, childName, ns, obligatory);
				success &= doubleResult.getSecondResult();
				Element elUrl = doubleResult.getFirstResult();
				String url = elUrl.getTextNormalize();
				institution.setName(url);
	
				childName = "Phone";
				obligatory = true;
				doubleResult = XmlHelp.getSingleChildElement(elInstitution, childName, ns, obligatory);
				success &= doubleResult.getSecondResult();
				Element elPhone = doubleResult.getFirstResult();
				String phone = elPhone.getTextNormalize();
				institution.setName(phone);
	
				childName = "Email";
				obligatory = true;
				doubleResult = XmlHelp.getSingleChildElement(elInstitution, childName, ns, obligatory);
				success &= doubleResult.getSecondResult();
				Element elEmail = doubleResult.getFirstResult();
				String email = elEmail.getTextNormalize();
				institution.setName(email);
			}
			

		}
		
		Collection collection = specimen.getCollection();
		if (collection == null){
			collection = Collection.NewInstance();
			specimen.setCollection(collection);
		}
		//Institution
		collection.setInstitute(institution);

		return success;
	}
	
	private boolean makeCollection(Specimen specimen, Element elCollection){
		boolean success = true;
		Collection  collection = null;
		if (elCollection != null){
			Namespace ns = elCollection.getNamespace();
			collection = Collection.NewInstance();

			//TODO collection placeholder 
			specimen.setCollection(collection);
		}
		return success;
	}
	
	private boolean makeSpecimenItem(Specimen specimen, Element elSpecimenItem){
		boolean success = true;
		Namespace ns = elSpecimenItem.getNamespace();
		if (specimen == null){
			logger.warn("No specimen");
			return false;
		}else if (elSpecimenItem != null){
			
			//TODO specimenItem placeholder 
			
		}
		return success;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TcsXmlImportState state){
		TcsXmlImportConfigurator tcsConfig = state.getConfig();
		return (! tcsConfig.isDoSpecimen());
	}
	
}
