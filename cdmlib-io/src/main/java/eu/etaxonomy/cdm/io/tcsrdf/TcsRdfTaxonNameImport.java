/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import java.io.InputStream;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
@Component
public class TcsRdfTaxonNameImport  extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {
	private static final Logger logger = Logger.getLogger(TcsRdfTaxonNameImport.class);

	private static int modCount = 5000;
	INonViralNameParser nameParser = new NonViralNameParserImpl();

	public TcsRdfTaxonNameImport(){
		super();
	}

	@Override
	public boolean doCheck(TcsRdfImportState config){
		boolean result = true;
		logger.warn("BasionymRelations not yet implemented");
		logger.warn("Checking for TaxonNames not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);

		return result;
	}

	protected static CdmSingleAttributeRDFMapperBase[] standardMappers = new CdmSingleAttributeRDFMapperBase[]{
		new CdmTextElementMapper("genusPart", "genusOrUninomial")
		, new CdmTextElementMapper("uninomial", "genusOrUninomial")  //TODO make it a more specific Mapper for both attributes
		, new CdmTextElementMapper("specificEpithet", "specificEpithet")
		, new CdmTextElementMapper("infraspecificEpithet", "infraSpecificEpithet")
		, new CdmTextElementMapper("infragenericEpithet", "infraGenericEpithet")
		, new CdmTextElementMapper("microReference", nsTcom, "nomenclaturalMicroReference")

	};

	protected static CdmSingleAttributeRDFMapperBase[] operationalMappers = new CdmSingleAttributeRDFMapperBase[]{
		new CdmUnclearMapper("basionymAuthorship")
		, new CdmUnclearMapper("combinationAuthorship")
		, new CdmUnclearMapper("hasAnnotation")
		, new CdmUnclearMapper("rank")
		, new CdmUnclearMapper("nomenclaturalCode")
		, new CdmUnclearMapper("publishedIn", nsTcom)
		, new CdmUnclearMapper("year")
	};

	protected static CdmSingleAttributeRDFMapperBase[] unclearMappers = new CdmSingleAttributeRDFMapperBase[]{
		new CdmUnclearMapper("authorship")
		, new CdmUnclearMapper("rankString")
		, new CdmUnclearMapper("nameComplete")
		, new CdmUnclearMapper("hasBasionym")
		, new CdmUnclearMapper("dateOfEntry", nsTpalm)
	};

	@Override
	protected void doInvoke(TcsRdfImportState state){

		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<TeamOrPersonBase> authorMap = (MapWrapper<TeamOrPersonBase>)state.getStore(ICdmIO.TEAM_STORE);

		String tcsElementName;
		Namespace tcsNamespace;
		String value;

		logger.info("start makeTaxonNames ...");
		TcsRdfImportConfigurator config = state.getConfig();
		Model root = config.getSourceRoot();

		String rdfNamespace = config.getRdfNamespaceURIString();
		String taxonNameNamespace = config.getTnNamespaceURIString();

		String idNamespace = "TaxonName";

		Resource elTaxonName = root.getResource(taxonNameNamespace);

		int i = 0;

		TaxonNameBase name;
		Property property = root.getProperty(taxonNameNamespace+"authorship");

		ResIterator iterator = root.listSubjectsWithProperty(property, (RDFNode) null);
		String id ;
		while (iterator.hasNext()){

			Resource resource = iterator.next();

			name = handleNameResource(resource, config);
			id = resource.getNameSpace();
			taxonNameMap.put(id, name);
		}

		logger.info(i + " names handled");
		getNameService().save(taxonNameMap.objects());
//		makeNameSpecificData(nameMap);
		logger.info("end makeTaxonNames ...");
		return;

	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
    protected boolean isIgnore(TcsRdfImportState state){
		return ! state.getConfig().isDoTaxonNames();
	}

	protected TaxonNameBase handleNameModel(Model model, TcsRdfImportConfigurator config, MapWrapper<TaxonNameBase> taxonNameMap, String uri){
		Resource nameAbout = model.getResource(uri);
		TaxonNameBase result = handleNameResource(nameAbout, config);
		taxonNameMap.put(uri, result);
		return result;

	}

	private TaxonNameBase handleNameResource(Resource nameAbout, TcsRdfImportConfigurator config){
		String idNamespace = "TaxonName";

		StmtIterator stmts = nameAbout.listProperties();
		while(stmts.hasNext()){
			System.out.println(stmts.next().getPredicate().toString());
		}

		Property prop = nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"nomenclaturalCode");
		Statement stateNomenclaturalCode = nameAbout.getProperty(prop);
		String strNomenclaturalCode = stateNomenclaturalCode.getObject().toString();
		//Rank
		prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"rankString");
		String strRank = nameAbout.getProperty(prop).getString();



		try {

			Rank rank = TcsRdfTransformer.rankString2Rank(strRank);
			NomenclaturalCode nomCode;
			if (strNomenclaturalCode != null){
				nomCode = TcsRdfTransformer.nomCodeString2NomCode(strNomenclaturalCode);
			}else{
				nomCode = NomenclaturalCode.ICNAFP;
			}

			TaxonNameBase<?,?> nameBase = nomCode.getNewTaxonNameInstance(rank);

			Set<String> omitAttributes = null;
			//makeStandardMapper(nameAbout, nameBase, omitAttributes, standardMappers);

			prop = nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"nameComplete");
			String strNameComplete = nameAbout.getProperty(prop).getString();
			nameBase.setTitleCache(strNameComplete, true);

			prop =  nameAbout.getModel().getProperty(config.getCommonNamespaceURIString()+"publishedIn");
			String strPublishedIn = nameAbout.getProperty(prop).getString();
			if (strPublishedIn != null && strPublishedIn != ""){
				IGeneric nomRef = ReferenceFactory.newGeneric(); //TODO
				nomRef.setTitleCache(strPublishedIn, true);
				nameBase.setNomenclaturalReference(nomRef);
				try{
				prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"year");
				String strYear = nameAbout.getProperty(prop).getString();
				Integer year = null;
				if (strYear != null){
					try {
						year = Integer.valueOf(strYear);
						TimePeriod timeP = TimePeriod.NewInstance(year);
						nomRef.setDatePublished(timeP);
					} catch (RuntimeException e) {
						logger.warn("year could not be parsed");
					}
				}
				}catch(NullPointerException e){
				}
				if (config.isPublishReferences()){
					((Reference<?>)nomRef).addMarker(Marker.NewInstance(MarkerType.PUBLISH(), false));
				}
			}

			if (nameBase instanceof NonViralName){
				NonViralName<?> nonViralName = (NonViralName<?>)nameBase;
				prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"genusPart");
				String strGenusPart;
				try{
					strGenusPart = nameAbout.getProperty(prop).getString();
				}catch(NullPointerException e){
					prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"uninomial");
					strGenusPart = nameAbout.getProperty(prop).getString();
				}

				nonViralName.setGenusOrUninomial(strGenusPart);

				prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"infragenericEpithet");
				try{
					String strInfragenericEpithet = nameAbout.getProperty(prop).getString();
					nonViralName.setInfraGenericEpithet(strInfragenericEpithet);
				}catch(NullPointerException e){

				}
				try {
					prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"specificEpithet");
					String strSpecificEpithet = nameAbout.getProperty(prop).getString();
					nonViralName.setSpecificEpithet(strSpecificEpithet);
				}catch(NullPointerException e){

				}
				try{
				prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"infraspecificEpithet");
				String strInfraspecificEpithet = nameAbout.getProperty(prop).getString();
				nonViralName.setInfraSpecificEpithet(strInfraspecificEpithet);
				}catch(NullPointerException e){

				}
				//Authorships
				//TODO
				/*
				 * <tn:authorteam>
						<tm:Team>
							<tm:name>(Raf.) Fernald</tm:name>
							<tm:hasMember rdf:resource="urn:lsid:ipni.org:authors:2691-1"
								tm:index="1"
								tm:role="Combination Author"/>
							<tm:hasMember rdf:resource="urn:lsid:ipni.org:authors:8096-1"
								tm:index="1"
								tm:role="Basionym Author"/>
						</tm:Team>
					</tn:authorteam>
				 */
				prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"authorship");
				Statement stateAuthorship = nameAbout.getProperty(prop);
				prop =  nameAbout.getModel().getProperty(config.getTnNamespaceURIString()+"authorteam");
				Statement stateAuthorTeam = nameAbout.getProperty(prop);
				Team authorTeam = new Team();
				authorTeam.setTitleCache(stateAuthorship.getObject().toString(), true);
				Statement stateAutorTeamTeam = null;
				Statement stateAutorTeamName = null;
				StmtIterator stateTeamMember = null;
				if (stateAuthorTeam != null){
					prop =  stateAuthorTeam.getModel().getProperty(config.getTeamNamespaceURIString()+"Team");
					try{
						stateAutorTeamTeam = stateAuthorTeam.getProperty(prop);
					}catch(Exception e){

					}
					try{
						prop =  stateAuthorTeam.getModel().getProperty(config.getTeamNamespaceURIString()+"name");
						stateAutorTeamName = stateAuthorTeam.getProperty(prop);
					}catch(Exception e){

					}
					try{
						prop =  nameAbout.getModel().getProperty(config.getTeamNamespaceURIString()+"hasMember");
						stateTeamMember = ((Resource)stateAuthorTeam.getObject()).listProperties(prop);
						String memberString = null;
						Person person;
						if (stateTeamMember.toList().size() ==1){
						    person = Person.NewTitledInstance(authorTeam.getTitleCache());
						}else {
						    nameParser.parseAuthors(nonViralName, authorTeam.getTitleCache());
						 }
						 /* for (Statement statement :stateTeamMember.toList()){
							memberString =statement.getObject().toString();
							if (memberString != null){
								person = Person.NewTitledInstance(memberString);
								authorTeam.addTeamMember(person);
							}
						}*/
					}catch(Exception e){
						System.err.println(e.getMessage());
					}
				}



				nonViralName.setCombinationAuthorship(authorTeam);

				//Annotations:
				/*
				 * <tn:hasAnnotation>
            <tn:NomenclaturalNote>
                <tn:noteType rdf:resource="http://rs.tdwg.org/ontology/voc/TaxonName#replacementNameFor"/>
                <tn:objectTaxonName rdf:resource="urn:lsid:ipni.org:names:151538-1"/>
            </tn:NomenclaturalNote>
        </tn:hasAnnotation>
				 */
				/*
				String strInfraspecificEpithet = nameAbout.getProperty(prop).getString();
				tcsElementName = "basionymAuthorship";
				String basionymAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				if (basionymAuthorValue != null){
					TeamOrPersonBase<?> basionymAuthor = Team.NewInstance();
					basionymAuthor.setNomenclaturalTitle(basionymAuthorValue);
					nonViralName.setBasionymAuthorship(basionymAuthor);
				}

				//TODO
				tcsElementName = "combinationAuthorship";
				String combinationAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				if (combinationAuthorValue != null){
					TeamOrPersonBase<?> combinationAuthor = Team.NewInstance();
					combinationAuthor.setNomenclaturalTitle(combinationAuthorValue);
					nonViralName.setCombinationAuthorship(combinationAuthor);
				}

				//set the authorshipCache
				tcsElementName = "authorship";
				String authorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				String cache = nonViralName.getAuthorshipCache();
				if ( authorValue != null){
					//compare existing authorship cache with new one and check if it is necessary to
					//make cache protected  //TODO refinement
					if (cache == null){
						nonViralName.setAuthorshipCache(authorValue);
					}else{
						cache = basionymAuthorValue == null ? cache : cache.replace(basionymAuthorValue, "");
						cache = combinationAuthorValue == null ? cache : cache.replace(combinationAuthorValue, "");
						cache = cache.replace("\\(|\\)", "");
						cache = cache.trim();
						if (! cache.equals("")){
							nonViralName.setAuthorshipCache(authorValue);
						}
					}
				}*/
			}
			//ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), nameAbout, idNamespace);

			//checkAdditionalContents(elTaxonName, standardMappers, operationalMappers, unclearMappers);

			return nameBase;

			}catch(Exception e){
			e.printStackTrace();
			return null;
		}
			/*
			//name
			String strNameComplete = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "nameComplete", rdfNamespace);
			nameBase.setTitleCache(strNameComplete, true);

			//Reference
			//TODO
			String tcsElementName = "publishedIn";
			Namespace tcsNamespace = config.getCommonNamespaceURIString();
			String value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
			if (value != null && value != ""){
				IGeneric nomRef = ReferenceFactory.newGeneric(); //TODO
				nomRef.setTitleCache(value, true);
				nameBase.setNomenclaturalReference(nomRef);

				//TODO
				tcsElementName = "year";
				tcsNamespace = taxonNameNamespace;
				Integer year = null;
				value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
				if (value != null){
					try {
						year = Integer.valueOf(value);
						TimePeriod timeP = TimePeriod.NewInstance(year);
						nomRef.setDatePublished(timeP);
					} catch (RuntimeException e) {
						logger.warn("year could not be parsed");
					}
				}
				if (config.isPublishReferences()){
					((Reference<?>)nomRef).addMarker(Marker.NewInstance(MarkerType.PUBLISH(), false));
				}
			}

			//Status
			tcsNamespace = taxonNameNamespace;
			Element elAnnotation = elTaxonName.getChild("hasAnnotation", tcsNamespace);
			if (elAnnotation != null){
				Element elNomenclaturalNote = elAnnotation.getChild("NomenclaturalNote", tcsNamespace);
				if (elNomenclaturalNote != null){
					String statusValue = (String)ImportHelper.getXmlInputValue(elNomenclaturalNote, "note", tcsNamespace);
					String type = XmlHelp.getChildAttributeValue(elNomenclaturalNote, "type", tcsNamespace, "resource", rdfNamespace);
					String tdwgType = "http://rs.tdwg.org/ontology/voc/TaxonName#PublicationStatus";
					if (tdwgType.equalsIgnoreCase(type)){
						try {
							NomenclaturalStatusType statusType = TcsRdfTransformer.nomStatusString2NomStatus(statusValue);
							//NomenclaturalStatusType statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusValue);
							if (statusType != null){
								nameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
							}
						} catch (UnknownCdmTypeException e) {
							if (! statusValue.equals("valid")){
								logger.warn("Unknown NomenclaturalStatusType: " +  statusValue);
							}
						}
					}
				}
			}

			if (nameBase instanceof NonViralName){
				NonViralName<?> nonViralName = (NonViralName<?>)nameBase;
				String strGenusPart =  XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "genusPart", rdfNamespace);

				//for names of rank genus the uninomial property should be used
				if (strGenusPart == null){
					strGenusPart =  XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "uninomial", rdfNamespace);
				}
				nonViralName.setGenusOrUninomial(strGenusPart);

				String strInfragenericEpithet =  XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "infragenericEpithet", rdfNamespace);
				nonViralName.setGenusOrUninomial(strInfragenericEpithet);



				String strSpecificEpithet = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "specificEpithet", rdfNamespace);
				nonViralName.setSpecificEpithet(strSpecificEpithet);

				String strInfraspecificEpithet = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "infraspecificEpithet", rdfNamespace);
				nonViralName.setInfraSpecificEpithet(strInfraspecificEpithet);
				//AuthorTeams
				//TODO
				tcsElementName = "basionymAuthorship";
				String basionymAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				if (basionymAuthorValue != null){
					TeamOrPersonBase<?> basionymAuthor = Team.NewInstance();
					basionymAuthor.setNomenclaturalTitle(basionymAuthorValue);
					nonViralName.setBasionymAuthorship(basionymAuthor);
				}

				//TODO
				tcsElementName = "combinationAuthorship";
				String combinationAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				if (combinationAuthorValue != null){
					TeamOrPersonBase<?> combinationAuthor = Team.NewInstance();
					combinationAuthor.setNomenclaturalTitle(combinationAuthorValue);
					nonViralName.setCombinationAuthorship(combinationAuthor);
				}

				//set the authorshipCache
				tcsElementName = "authorship";
				String authorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				String cache = nonViralName.getAuthorshipCache();
				if ( authorValue != null){
					//compare existing authorship cache with new one and check if it is necessary to
					//make cache protected  //TODO refinement
					if (cache == null){
						nonViralName.setAuthorshipCache(authorValue);
					}else{
						cache = basionymAuthorValue == null ? cache : cache.replace(basionymAuthorValue, "");
						cache = combinationAuthorValue == null ? cache : cache.replace(combinationAuthorValue, "");
						cache = cache.replace("\\(|\\)", "");
						cache = cache.trim();
						if (! cache.equals("")){
							nonViralName.setAuthorshipCache(authorValue);
						}
					}
				}
			}
			ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), nameAbout, idNamespace);

			checkAdditionalContents(elTaxonName, standardMappers, operationalMappers, unclearMappers);

			//nameId
			//TODO
			//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
			//taxonNameMap.put(nameAbout, nameBase);
			return nameBase;
		}catch(UnknownCdmTypeException e){
			e.printStackTrace();
		}
		return null;*/
	}

	protected TaxonNameBase handleNameElement(Element elTaxonName, Namespace rdfNamespace, Namespace taxonNameNamespace, TcsRdfImportConfigurator config, MapWrapper<TaxonNameBase> taxonNameMap){
		String idNamespace = "TaxonName";
		Attribute about = elTaxonName.getAttribute("about", rdfNamespace);

		//create TaxonName element


		String nameAbout = elTaxonName.getAttributeValue("about", rdfNamespace);
		if (nameAbout == null){
			nameAbout = XmlHelp.getChildAttributeValue(elTaxonName, "TaxonName", taxonNameNamespace, "about", rdfNamespace);
		}


		String strRank = XmlHelp.getChildAttributeValue(elTaxonName, "rankString", taxonNameNamespace, "rankString", rdfNamespace);
		if (strRank == null){
			strRank = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "rankString", rdfNamespace);

		}

		if (strRank == null){
			strRank = XmlHelp.getChildAttributeValue(elTaxonName, "rank", taxonNameNamespace, "resource", rdfNamespace);

		}

		String strNomenclaturalCode = XmlHelp.getChildContentAttributeValue(elTaxonName, "TaxonName", taxonNameNamespace, "nomenclaturalCode", rdfNamespace);
		if (strNomenclaturalCode == null){
			strNomenclaturalCode = XmlHelp.getChildAttributeValue(elTaxonName, "nomenclaturalCode", taxonNameNamespace, "resource", rdfNamespace);

		}
		try {

			Rank rank = TcsRdfTransformer.rankString2Rank(strRank);
			NomenclaturalCode nomCode;
			if (strNomenclaturalCode != null){
				nomCode = TcsRdfTransformer.nomCodeString2NomCode(strNomenclaturalCode);
			}else{
				nomCode = NomenclaturalCode.ICNAFP;
			}

			TaxonNameBase<?,?> nameBase = nomCode.getNewTaxonNameInstance(rank);

			Set<String> omitAttributes = null;
			//makeStandardMapper(elTaxonName, nameBase, omitAttributes, standardMappers);

			//name
			String strNameComplete = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "nameComplete", rdfNamespace);
			nameBase.setTitleCache(strNameComplete, true);

			//Reference
			//TODO
			String tcsElementName = "publishedIn";
			String tcsNamespace = config.getCommonNamespaceURIString();
			/*String value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
			if (value != null && value != ""){
				IGeneric nomRef = ReferenceFactory.newGeneric(); //TODO
				nomRef.setTitleCache(value, true);
				nameBase.setNomenclaturalReference(nomRef);

				//TODO
				tcsElementName = "year";
				tcsNamespace = taxonNameNamespace;
				Integer year = null;
				value = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, tcsNamespace);
				if (value != null){
					try {
						year = Integer.valueOf(value);
						TimePeriod timeP = TimePeriod.NewInstance(year);
						nomRef.setDatePublished(timeP);
					} catch (RuntimeException e) {
						logger.warn("year could not be parsed");
					}
				}
				if (config.isPublishReferences()){
					((Reference<?>)nomRef).addMarker(Marker.NewInstance(MarkerType.PUBLISH(), false));
				}
			}

			//Status
			tcsNamespace = taxonNameNamespace;
			Element elAnnotation = elTaxonName.getChild("hasAnnotation", tcsNamespace);
			if (elAnnotation != null){
				Element elNomenclaturalNote = elAnnotation.getChild("NomenclaturalNote", tcsNamespace);
				if (elNomenclaturalNote != null){
					String statusValue = (String)ImportHelper.getXmlInputValue(elNomenclaturalNote, "note", tcsNamespace);
					String type = XmlHelp.getChildAttributeValue(elNomenclaturalNote, "type", tcsNamespace, "resource", rdfNamespace);
					String tdwgType = "http://rs.tdwg.org/ontology/voc/TaxonName#PublicationStatus";
					if (tdwgType.equalsIgnoreCase(type)){
						try {
							NomenclaturalStatusType statusType = TcsRdfTransformer.nomStatusString2NomStatus(statusValue);
							//NomenclaturalStatusType statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusValue);
							if (statusType != null){
								nameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
							}
						} catch (UnknownCdmTypeException e) {
							if (! statusValue.equals("valid")){
								logger.warn("Unknown NomenclaturalStatusType: " +  statusValue);
							}
						}
					}
				}
			}

			if (nameBase instanceof NonViralName){
				NonViralName<?> nonViralName = (NonViralName<?>)nameBase;
				String strGenusPart =  XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "genusPart", rdfNamespace);

				//for names of rank genus the uninomial property should be used
				if (strGenusPart == null){
					strGenusPart =  XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "uninomial", rdfNamespace);
				}
				nonViralName.setGenusOrUninomial(strGenusPart);

				String strInfragenericEpithet =  XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "infragenericEpithet", rdfNamespace);
				nonViralName.setGenusOrUninomial(strInfragenericEpithet);



				String strSpecificEpithet = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "specificEpithet", rdfNamespace);
				nonViralName.setSpecificEpithet(strSpecificEpithet);

				String strInfraspecificEpithet = XmlHelp.getChildContent(elTaxonName, "TaxonName", taxonNameNamespace, "infraspecificEpithet", rdfNamespace);
				nonViralName.setInfraSpecificEpithet(strInfraspecificEpithet);
				//AuthorTeams
				//TODO
				tcsElementName = "basionymAuthorship";
				String basionymAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				if (basionymAuthorValue != null){
					TeamOrPersonBase<?> basionymAuthor = Team.NewInstance();
					basionymAuthor.setNomenclaturalTitle(basionymAuthorValue);
					nonViralName.setBasionymAuthorship(basionymAuthor);
				}

				//TODO
				tcsElementName = "combinationAuthorship";
				String combinationAuthorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				if (combinationAuthorValue != null){
					TeamOrPersonBase<?> combinationAuthor = Team.NewInstance();
					combinationAuthor.setNomenclaturalTitle(combinationAuthorValue);
					nonViralName.setCombinationAuthorship(combinationAuthor);
				}

				//set the authorshipCache
				tcsElementName = "authorship";
				String authorValue = (String)ImportHelper.getXmlInputValue(elTaxonName, tcsElementName, taxonNameNamespace);
				String cache = nonViralName.getAuthorshipCache();
				if ( authorValue != null){
					//compare existing authorship cache with new one and check if it is necessary to
					//make cache protected  //TODO refinement
					if (cache == null){
						nonViralName.setAuthorshipCache(authorValue);
					}else{
						cache = basionymAuthorValue == null ? cache : cache.replace(basionymAuthorValue, "");
						cache = combinationAuthorValue == null ? cache : cache.replace(combinationAuthorValue, "");
						cache = cache.replace("\\(|\\)", "");
						cache = cache.trim();
						if (! cache.equals("")){
							nonViralName.setAuthorshipCache(authorValue);
						}
					}
				}
			}
			ImportHelper.setOriginalSource(nameBase, config.getSourceReference(), nameAbout, idNamespace);

			checkAdditionalContents(elTaxonName, standardMappers, operationalMappers, unclearMappers);

			//nameId
			//TODO
			//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);
			//taxonNameMap.put(nameAbout, nameBase);
			return nameBase;
		*/}catch(UnknownCdmTypeException e){
			e.printStackTrace();
		}
		return null;
	}

	public TaxonNameBase handleRdfElementFromStream(InputStream is, TcsRdfImportConfigurator config, MapWrapper<TaxonNameBase> taxonNameMap, String uri){
	Model model = ModelFactory.createDefaultModel();
		try{
			model.read(is, null);

			config.makeNamespaces(model);

			String rdfNamespace = config.getRdfNamespaceURIString();
			String taxonNameNamespace = config.getTnNamespaceURIString();
			return handleNameModel(model, config, taxonNameMap, uri);


		}catch(Exception e){
			logger.debug("The file was no valid rdf file");
		}



		return null;
	}

}
