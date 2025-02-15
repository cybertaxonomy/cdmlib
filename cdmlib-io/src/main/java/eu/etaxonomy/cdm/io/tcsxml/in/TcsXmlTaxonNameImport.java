/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsxml.TcsXmlTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

@Component("tcsXmlTaxonNameIO")
public class TcsXmlTaxonNameImport
        extends TcsXmlImportBase {

    private static final long serialVersionUID = -1978871518114999061L;
    private static final Logger logger = LogManager.getLogger();

	private static int modCount = 5000;

	public TcsXmlTaxonNameImport(){
		super();
	}

	@Override
	public boolean doCheck(TcsXmlImportState state){
		boolean result = true;
		logger.warn("BasionymRelations not yet implemented");
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);

		return result;
	}

	//@SuppressWarnings("unchecked")
	@Override
	public void doInvoke(TcsXmlImportState state){

		logger.info("start make TaxonNames ...");
		@SuppressWarnings("unchecked")
        MapWrapper<Person> personMap = (MapWrapper<Person>)state.getStore(ICdmIO.PERSON_STORE);
		@SuppressWarnings("unchecked")
        MapWrapper<Person> teamMap = (MapWrapper<Person>)state.getStore(ICdmIO.TEAM_STORE);
        @SuppressWarnings("unchecked")
        MapWrapper<TaxonName> taxonNameMap = (MapWrapper<TaxonName>)state.getStore(ICdmIO.TAXONNAME_STORE);
		@SuppressWarnings("unchecked")
        MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
//		String idNamespace = "TaxonName";

		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();

		childName = "TaxonNames";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);

		String tcsElementName = "TaxonName";
		List<Element> elTaxonNameList =  elTaxonNames == null ? new ArrayList<>() : (List<Element>)elTaxonNames.getChildren(tcsElementName, tcsNamespace);

		int i = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNameList){
			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}
			String strId = elTaxonName.getAttributeValue("id");
			/*List<String> elementList = new ArrayList<>();

			//create TaxonName element
			String strId = elTaxonName.getAttributeValue("id");
			String strNomenclaturalCode = elTaxonName.getAttributeValue("nomenclaturalCode");

			childName = "Rank";
			obligatory = false;
			Element elRank = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			Rank rank = null;
			if (elRank != null){
				rank = makeRank(elRank);
				if (rank == null){
					logger.warn("Unknown rank for" + strId);
				}
			}
			elementList.add(childName.toString());


			try {
				TaxonName nameBase;
				NomenclaturalCode nomCode = TcsXmlTransformer.nomCodeString2NomCode(strNomenclaturalCode);
				if (nomCode != null){
					nameBase = nomCode.getNewTaxonNameInstance(rank);
				}else{
					nameBase = TaxonNameFactory.NewNonViralInstance(rank);
				}
				childName = "Simple";
				obligatory = true;
				Element elSimple = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
				nameBase.setTitleCache(simple, false);
				elementList.add(childName.toString());

				childName = "CanonicalName";
				obligatory = false;
				Element elCanonicalName = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeCanonicalName(nameBase, elCanonicalName, taxonNameMap, success);
				elementList.add(childName.toString());

				childName = "CanonicalAuthorship";
				obligatory = false;
				Element elCanonicalAuthorship = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeCanonicalAuthorship(nameBase, elCanonicalAuthorship, authorMap, success);
				elementList.add(childName.toString());

				childName = "PublishedIn";
				obligatory = false;
				Element elPublishedIn = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makePublishedIn(nameBase, elPublishedIn, referenceMap, success);
				elementList.add(childName.toString());

				childName = "Year";
				obligatory = false;
				Element elYear = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeYear(nameBase, elYear, success);
				elementList.add(childName.toString());

				childName = "MicroReference";
				obligatory = false;
				Element elMicroReference = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeMicroReference(nameBase, elMicroReference, success);
				elementList.add(childName.toString());

				childName = "Typification";
				obligatory = false;
				Element elTypification = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeTypification(nameBase, elTypification, success);
				elementList.add(childName.toString());

				childName = "PublicationStatus";
				obligatory = false;
				Element elPublicationStatus = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makePublicationStatus(nameBase, elPublicationStatus, success);
				elementList.add(childName.toString());

				childName = "ProviderLink";
				obligatory = false;
				Element elProviderLink = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeProviderLink(nameBase, elProviderLink, success);
				elementList.add(childName.toString());

				childName = "ProviderSpecificData";
				obligatory = false;
				Element elProviderSpecificData = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
				makeProviderSpecificData(nameBase, elProviderSpecificData, success);
				elementList.add(childName.toString());


				ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), strId, idNamespace);
				if (strId != null){
					if (strId.matches("urn:lsid:ipni.org:.*:.*:.*")){
						strId = strId.substring(0,strId.lastIndexOf(":"));
					}
				}
				taxonNameMap.put(strId, nameBase);

			} catch (UnknownCdmTypeException e) {
				logger.warn("Name with id " + strId + " has unknown nomenclatural code.");
				success.setValue(false);
			}*/

			taxonNameMap.put(removeVersionOfRef(strId), handleTaxonNameElement(elTaxonName, success, state));
		}
		logger.info(i + " names handled");
		getAgentService().save(personMap.getAllValues());
		getAgentService().save(teamMap.getAllValues());
		getReferenceService().save(referenceMap.objects());
		Collection<TaxonName> names = taxonNameMap.objects();
		getNameService().save(names);

		logger.info("end makeTaxonNames ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}

		return;
	}

	/**
	 * Returns the rank represented by the rank element.<br>
	 * Returns <code>null</code> if the element is null.<br>
	 * Returns <code>null</code> if the code and the text are both either empty or do not exists.<br>
	 * Returns the rank represented by the code attribute, if the code attribute is not empty and could be resolved.<br>
	 * If the code could not be resolved it returns the rank represented most likely by the elements text.<br>
	 * Returns UNKNOWN_RANK if code attribute and element text could not be resolved.
	 * @param elRank tcs rank element
	 * @return
	 */
	protected static Rank makeRank(Element elRank){
		Rank result;
 		if (elRank == null){
			return null;
		}
		String strRankCode = elRank.getAttributeValue("code");
		String strRankString = elRank.getTextNormalize();
		if ( StringUtils.isBlank(strRankCode) && StringUtils.isBlank(strRankString)){
			return null;
		}

		Rank codeRank = null;
		try {
			codeRank = TcsXmlTransformer.rankCode2Rank(strRankCode);
		} catch (UnknownCdmTypeException e1) {
			codeRank = Rank.UNKNOWN_RANK();
		}
		Rank stringRank = null;
		try {
//			boolean useUnknown = true;
			stringRank = TcsXmlTransformer.rankString2Rank(strRankString);
			//stringRank = Rank.getRankByNameOrIdInVoc(strRankString, useUnknown);
		} catch (UnknownCdmTypeException e1) {
			//does not happen because of useUnknown = true
		}

		//codeRank exists
		if ( (codeRank != null) && ! codeRank.equals(Rank.UNKNOWN_RANK())){
			result = codeRank;
			if (! codeRank.equals(stringRank) && ! Rank.UNKNOWN_RANK().equals(stringRank)){
				logger.warn("code rank and string rank are unequal. code: " + codeRank.getLabel() + " <-> string: " + (stringRank == null? "null": stringRank.getLabel()));
			}
		}
		//codeRank does not exist
		else{
			result = stringRank;
			logger.warn("string rank used, because code rank does not exist or was not recognized: " + strRankString );
		}
		return result;
	}

	public TaxonName handleTaxonNameElement(Element elTaxonName, ResultWrapper<Boolean> success, TcsXmlImportState state){

	    Namespace tcsNamespace = state.getConfig().getTcsXmlNamespace();

		@SuppressWarnings("unchecked")
        MapWrapper<Person> personMap = (MapWrapper<Person>)state.getStore(ICdmIO.PERSON_STORE);
		@SuppressWarnings("unchecked")
        MapWrapper<Team> teamMap = (MapWrapper<Team>)state.getStore(ICdmIO.TEAM_STORE);
        @SuppressWarnings("unchecked")
        MapWrapper<TaxonName> taxonNameMap = (MapWrapper<TaxonName>)state.getStore(ICdmIO.TAXONNAME_STORE);
		@SuppressWarnings("unchecked")
        MapWrapper<Reference> referenceMap =  (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);

		List<String> elementList = new ArrayList<>();
//		String idNamespace = "TaxonName";
		//create TaxonName element
		String strId = elTaxonName.getAttributeValue("id");
		String strNomenclaturalCode = elTaxonName.getAttributeValue("nomenclaturalCode");
		//Content elTaxName = elTaxonName.getChild(name, ns);
		String childName = "Rank";
		boolean obligatory = false;
		Element elRank = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
		Rank rank = null;
		if (elRank != null){
			rank = TcsXmlTaxonNameImport.makeRank(elRank);
			if (rank == null){
				logger.warn("Unknown rank for" + strId);
			}
		}
		elementList.add(childName.toString());

		try {
			TaxonName nameBase;
			NomenclaturalCode nomCode = TcsXmlTransformer.nomCodeString2NomCode(strNomenclaturalCode);
			if (nomCode != null){
				nameBase = nomCode.getNewTaxonNameInstance(rank);
			}else{
				nameBase = TaxonNameFactory.NewNonViralInstance(rank);
			}
			childName = "Simple";
			obligatory = true;
			Element elSimple = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
			nameBase.setTitleCache(simple, false);
			elementList.add(childName.toString());

			childName = "CanonicalName";
			obligatory = false;
			Element elCanonicalName = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeCanonicalName(nameBase, elCanonicalName, taxonNameMap, success);
			elementList.add(childName.toString());

			childName = "CanonicalAuthorship";
			obligatory = false;
			Element elCanonicalAuthorship = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeCanonicalAuthorship(nameBase, elCanonicalAuthorship, personMap, teamMap, success);
			elementList.add(childName.toString());

			childName = "PublishedIn";
			obligatory = false;
			Element elPublishedIn = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makePublishedIn(nameBase, elPublishedIn, referenceMap, success);
			elementList.add(childName.toString());

			childName = "Year";
			obligatory = false;
			Element elYear = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeYear(nameBase, elYear, success);
			elementList.add(childName.toString());

			childName = "MicroReference";
			obligatory = false;
			Element elMicroReference = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeMicroReference(nameBase, elMicroReference, success);
			elementList.add(childName.toString());

			childName = "Typification";
			obligatory = false;
			Element elTypification = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeTypification(nameBase, elTypification, success);
			elementList.add(childName.toString());

			childName = "PublicationStatus";
			obligatory = false;
			Element elPublicationStatus = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makePublicationStatus(nameBase, elPublicationStatus, success);
			elementList.add(childName.toString());

			childName = "ProviderLink";
			obligatory = false;
			Element elProviderLink = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeProviderLink(nameBase, elProviderLink, success);
			elementList.add(childName.toString());

			childName = "ProviderSpecificData";
			obligatory = false;
			Element elProviderSpecificData = XmlHelp.getSingleChildElement(success, elTaxonName, childName, tcsNamespace, obligatory);
			makeProviderSpecificData(nameBase, elProviderSpecificData, success, state);
			elementList.add(childName.toString());

			return nameBase;

		} catch (UnknownCdmTypeException e) {
			logger.warn("Name with id " + strId + " has unknown nomenclatural code.");
			success.setValue(false);
		}
		return null;
	}

	private void makeCanonicalAuthorship(TaxonName name, Element elCanonicalAuthorship, MapWrapper<Person> personMap, MapWrapper<Team> teamMap, ResultWrapper<Boolean> success){

	    if (elCanonicalAuthorship != null){
			Namespace ns = elCanonicalAuthorship.getNamespace();

			if (name.isNonViral()){
				INonViralName nonViralName = name;

				String childName = "Simple";
				boolean obligatory = true;
				Element elSimple = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
				//TODO
				//logger.warn("authorship cache cache protected not yet implemented");
				//nonViralName.setAuthorshipCache(simple, cacheProtected);

				childName = "Authorship";
				obligatory = false;
				Element elAuthorship = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				TeamOrPersonBase<?> author = makeAuthorship(elAuthorship, personMap, teamMap, success);
				nonViralName.setCombinationAuthorship(author);
				//setCombinationAuthorship(author);
				testNoMoreElements();

				childName = "BasionymAuthorship";
				obligatory = false;
				Element elBasionymAuthorship = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				TeamOrPersonBase<?> basionymAuthor = makeAuthorship(elBasionymAuthorship, personMap, teamMap, success);
				nonViralName.setBasionymAuthorship(basionymAuthor);
				testNoMoreElements();

				childName = "CombinationAuthorship";
				obligatory = false;
				Element elCombinationAuthorship = XmlHelp.getSingleChildElement(success, elCanonicalAuthorship, childName, ns, obligatory);
				TeamOrPersonBase<?> combinationAuthor = makeAuthorship(elCombinationAuthorship, personMap, teamMap, success);
				if (combinationAuthor != null){
					nonViralName.setCombinationAuthorship(combinationAuthor);
				}
				testNoMoreElements();

				if (elAuthorship != null && (elBasionymAuthorship != null || elCombinationAuthorship != null) ){
					logger.warn("Authorship and (BasionymAuthorship or CombinationAuthorship) must not exist at the same time in CanonicalAuthorship");
					success.setValue(false);
				}
			}
		}
	}

	private void makeMicroReference(TaxonName name, Element elMicroReference,
	        @SuppressWarnings("unused") ResultWrapper<Boolean> success){
		if (elMicroReference != null){
			String microReference = elMicroReference.getTextNormalize();
			name.setNomenclaturalMicroReference(microReference);
		}
	}

	private void makeCanonicalName(TaxonName name, Element elCanonicalName, MapWrapper<TaxonName> taxonNameMap, ResultWrapper<Boolean> success){
		boolean cacheProtected = false;

		if (elCanonicalName == null){
			return;
		}
		Namespace ns = elCanonicalName.getNamespace();

		String childName = "Simple";
		boolean obligatory = true;
		Element elSimple = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
		String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
		name.setFullTitleCache(simple, cacheProtected);

		if (name.isNonViral()){
			INonViralName nonViralName = name;
			childName = "Uninomial";
			obligatory = false;
			Element elUninomial = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String uninomial = (elUninomial == null)? "" : elUninomial.getTextNormalize();
			if (StringUtils.isNotBlank(uninomial)){
				nonViralName.setGenusOrUninomial(uninomial);
				if (nonViralName.getRank() != null && nonViralName.getRank().isLowerThan(RankClass.Genus)){  // TODO check
					logger.warn("Name " + simple + " lower then 'genus' but has a canonical name part 'Uninomial'.");
				}
			}
			testNoMoreElements();

			childName = "Genus";
			obligatory = false;
			Element elGenus = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			if (elGenus != null){
				//TODO do Attributes reference
				makeGenusReferenceType(name, elGenus, taxonNameMap, success);
				String genus = elGenus.getTextNormalize();
				if (StringUtils.isNotBlank(genus)){
					nonViralName.setGenusOrUninomial(genus);
					if (nonViralName.getRank() != null &&  ! nonViralName.getRank().isLowerThan(RankClass.Genus )){  // TODO check
						logger.warn("Name " + simple + " is not lower then 'genus' but has canonical name part 'Genus'.");
					}
				}
			}

			childName = "InfragenericEpithet";
			obligatory = false;
			Element elInfrageneric = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String infraGenericEpithet = (elInfrageneric == null)? "" : elInfrageneric.getTextNormalize();
			if (! infraGenericEpithet.trim().equals("")){
				nonViralName.setInfraGenericEpithet(infraGenericEpithet);
				if (nonViralName.getRank() != null && ! name.getRank().isInfraGeneric()){
					logger.warn("Name " + simple + " is not infra generic but has canonical name part 'InfragenericEpithet'.");
				}
			}
			testNoMoreElements();

			childName = "SpecificEpithet";
			obligatory = false;
			Element elSpecificEpithet = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String specificEpithet = (elSpecificEpithet == null)? "" : elSpecificEpithet.getTextNormalize();
			if (! specificEpithet.trim().equals("")){
				nonViralName.setSpecificEpithet(specificEpithet);
				if (nonViralName.getRank() != null && name.isSupraSpecific()){
					logger.warn("Name " + simple + " is not species or below but has canonical name part 'SpecificEpithet'.");
				}
			}
			testNoMoreElements();

			childName = "InfraspecificEpithet";
			obligatory = false;
			Element elInfraspecificEpithet = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
			String infraspecificEpithet = (elInfraspecificEpithet == null)? "" : elInfraspecificEpithet.getTextNormalize();
			if (! infraspecificEpithet.trim().equals("")){
				nonViralName.setInfraSpecificEpithet(infraspecificEpithet);
				if (nonViralName.getRank() != null && ! name.isInfraSpecific() ){
					logger.warn("Name " + simple + " is not infraspecific but has canonical name part 'InfraspecificEpithet'.");
				}
			}
			testNoMoreElements();


		}else{ //ViralName
			//logger.warn("Non NonViralNames not yet supported by makeCanonicalName");
		}


		childName = "CultivarNameGroup";
		obligatory = false;
		Element elCultivarNameGroup = XmlHelp.getSingleChildElement(success, elCanonicalName, childName, ns, obligatory);
		String cultivarNameGroup = (elCultivarNameGroup == null)? "" : elCultivarNameGroup.getTextNormalize();
		if (! "".equals(cultivarNameGroup.trim())){
			if (name.isCultivar()){
				makeCultivarEpithet();
			}else{
				logger.warn("Non cultivar name has 'cultivar name group' element. Omitted");
			}
		}
		return;
	}

	private void makeCultivarEpithet(){
		//TODO
		//logger.warn("'makeCultivarName' Not yet implemented");
	}

	private void makeGenusReferenceType(TaxonName name, Element elGenus, MapWrapper<TaxonName> taxonNameMap, ResultWrapper<Boolean> success){
		if(name.isNonViral()){
			INonViralName nonViralName = name;
			if (elGenus != null){
			    INonViralName genusReferenceName;
				//TODO code
				Class<TaxonName> clazz = TaxonName.class;
				genusReferenceName = makeReferenceType(elGenus, clazz, taxonNameMap, success);
				genusReferenceName.setNameType(NomenclaturalCode.NonViral);
				//Genus is stored either in Genus part (if ref) or in titleCache (if plain text)
				String genus = genusReferenceName.getGenusOrUninomial()!= null ? genusReferenceName.getGenusOrUninomial(): genusReferenceName.getTitleCache();
				nonViralName.setGenusOrUninomial(genus);
			}else{
				logger.warn("Missing Genus information");
			}
		}else{
			//TODO   (can be changed if Viral Name also has Genus in future
			//logger.warn("Genus ref type for Viral Name not implemented yet");
		}

	}

	private void makePublishedIn(TaxonName name, Element elPublishedIn,
	        MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){

	    if (elPublishedIn != null && name != null){
			Class<Reference> clazz = Reference.class;
			Reference ref = makeReferenceType(elPublishedIn, clazz, referenceMap, success);
			if (ref == null){
			    logger.warn("Nomecl. reference could not be created for '" + name.getTitleCache() + "'");
			}else {
			    name.setNomenclaturalReference(ref);
			    if (!ref.isPersisted()) {
			        referenceMap.put(ref.getUuid(), ref);
			    }
			}
		}else if (name == null){
			logger.warn("TaxonName must not be 'null'");
			success.setValue(false);
		}
	}

	private void makeYear(TaxonName name, Element elYear,
	        @SuppressWarnings("unused") ResultWrapper<Boolean> success){

	    if (elYear != null){
			String year = elYear.getTextNormalize();
			if (year != null){
    			if (name.isZoological()){
    				name.setPublicationYear(getIntegerYear(year));
    			}else{
    			    VerbatimTimePeriod period = VerbatimTimePeriod.NewVerbatimInstance(getIntegerYear(year));
    			    if (name.getNomenclaturalReference()!= null){
    			        name.getNomenclaturalReference().setDatePublished(period);
    			    } else{
    			        Reference nomRef = ReferenceFactory.newGeneric();
    			        nomRef.setDatePublished(period);
    			    }
    				logger.debug("Year can be set only for a zoological name, add the year to the nomenclatural reference.");
    			}
			}
		}
	}
}