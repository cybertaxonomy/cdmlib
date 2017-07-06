/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import static eu.etaxonomy.cdm.io.common.ImportHelper.NO_OVERWRITE;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.IRdfMapper;
import eu.etaxonomy.cdm.io.common.mapping.berlinModel.CdmOneToManyMapper;
import eu.etaxonomy.cdm.io.tcsxml.CdmSingleAttributeXmlMapperBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 29.05.2008
 */
@Component
public class TcsRdfReferenceImport extends TcsRdfImportBase implements ICdmIO<TcsRdfImportState> {

    private static final long serialVersionUID = -2441804641147165510L;

    private static final Logger logger = Logger.getLogger(TcsRdfReferenceImport.class);

	private static int modCount = 1000;

	public TcsRdfReferenceImport(){
		super();
	}

	@Override
	public boolean doCheck(TcsRdfImportState state){
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


	protected static IRdfMapper[] standardMappers = new IRdfMapper[]{
		//new CdmTextElementMapper("edition", "edition"),
		new CdmTextElementMapper("volume", "volume"),
		new CdmTextElementMapper("placePublished", "placePublished"),
		new CdmTextElementMapper("publisher", "publisher"),
		//new CdmTextElementMapper("isbn", "isbn"),
		new CdmTextElementMapper("pages", "pages"),
		//new CdmTextElementMapper("series", "series"),
		//new CdmTextElementMapper("issn", "issn"),
		//new CdmTextElementMapper("url", "uri")
	};

	protected static CdmSingleAttributeRDFMapperBase[] operationalMappers = new CdmSingleAttributeRDFMapperBase[]{
		new CdmUnclearMapper("year")
		, new CdmUnclearMapper("title")
		, new CdmUnclearMapper("shortTitle")
		, new CdmUnclearMapper("publicationType")
		, new CdmUnclearMapper("parentPublication")
		, new CdmUnclearMapper("authorship")

	};

//	protected static String[] createdAndNotesAttributes = new String[]{
//			"created_When", "updated_When", "created_Who", "updated_Who", "notes"
//	};

	protected static CdmSingleAttributeXmlMapperBase[] unclearMappers = new CdmSingleAttributeXmlMapperBase[]{

	};



	private boolean makeStandardMapper(Statement resource, Reference ref, Set<String> omitAttributes){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;
		for (IRdfMapper mapper : standardMappers){
			if (mapper instanceof CdmSingleAttributeMapperBase){
				makeSingleAttributeMapper((CdmSingleAttributeRDFMapperBase)mapper, resource, ref, omitAttributes);
			}else if (mapper instanceof CdmOneToManyMapper){
				makeMultipleAttributeMapper((CdmOneToManyMapper)mapper, resource, ref, omitAttributes);
			}else{
				logger.error("Unrecognized mapper type");
				return false;
			}


		}
		return true;
	}

	private boolean makeSingleAttributeMapper(CdmSingleAttributeRDFMapperBase mapper, Statement resource, Reference ref, Set<String> omitAttributes){
		boolean result = true;
		Object value = getValue(mapper, resource);
		//write to destination
		if (value != null){
			String destinationAttribute = mapper.getDestinationAttribute();
			if (! omitAttributes.contains(destinationAttribute)){
				result &= ImportHelper.addValue(value, ref, destinationAttribute, mapper.getTypeClass(), OVERWRITE, OBLIGATORY);
			}
		}
		return result;
	}

	private boolean makeMultipleAttributeMapper(CdmOneToManyMapper<?,?,CdmTextElementMapper> mapper, Statement parentElement, Reference ref, Set<String> omitAttributes){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;
		String destinationAttribute = mapper.getSingleAttributeName();
		List<Object> sourceValues = new ArrayList<Object>();
		List<Class> classes = new ArrayList<Class>();
		for (CdmTextElementMapper singleMapper : mapper.getSingleMappers()){
			String sourceAttribute = singleMapper.getSourceAttribute();
			Object value = getValue(singleMapper, parentElement);
			//Object value = valueMap.get(sourceAttribute);
			sourceValues.add(value);
			Class clazz = singleMapper.getTypeClass();
			classes.add(clazz);
		}

		result &= ImportHelper.addMultipleValues(sourceValues, ref, destinationAttribute, classes, NO_OVERWRITE, OBLIGATORY);
		return result;
	}

//	private Object getValue(CdmSingleAttributeRDFMapperBase mapper, Element parentElement){
//		String sourceAttribute = mapper.getSourceAttribute().toLowerCase();
//		Namespace sourceNamespace = mapper.getSourceNamespace(parentElement);
//		Element child = parentElement.getChild(sourceAttribute, sourceNamespace);
//		if (child == null){
//			return null;
//		}
//		if (child.getContentSize() > 1){
//			logger.warn("Element is not String");
//		}
//		Object value = child.getTextTrim();
//		return value;
//	}

	@Override
    protected void doInvoke(TcsRdfImportState state){
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<Reference> nomRefMap = (MapWrapper<Reference>)state.getStore(ICdmIO.NOMREF_STORE);

		TcsRdfImportConfigurator config = state.getConfig();
		Model root = config.getSourceRoot();
		logger.info("start makeReferences ...");

		String tcsElementName;
		Namespace tcsNamespace;

		String rdfNamespace = config.getRdfNamespaceURIString();
		String publicationNamespace = config.getPublicationNamespaceURI();

		String idNamespace = "PublicationCitation";
		tcsElementName = "PublicationCitation";

		/*List<Element> elPublicationCitations = root.l

		int nomRefCount = 0;
		int biblioRefsCount = 0;

		int i = 0;
		//for each publication
		for (Element elPublicationCitation : elPublicationCitations){

			if ((++i % modCount) == 0){ logger.info("references handled: " + (i-1));}

			//create publication element
			String strAbout = elPublicationCitation.getAttributeValue("about", rdfNamespace);

			tcsElementName = "publicationType";
			tcsNamespace = publicationNamespace;
			String strPubType = XmlHelp.getChildAttributeValue(elPublicationCitation, tcsElementName, tcsNamespace, "resource", rdfNamespace);

			try {
				Reference<?> ref = TcsRdfTransformer.pubTypeStr2PubType(strPubType);
				if (ref==null){
					ref = ReferenceFactory.newGeneric();
				}

				Set<String> omitAttributes = null;
				makeStandardMapper(elPublicationCitation, ref, omitAttributes);


				tcsElementName = "authorship";
				tcsNamespace = publicationNamespace;
				String strAuthorship = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				//TODO
				TeamOrPersonBase<?> authorship = Team.NewInstance();
				authorship.setTitleCache(strAuthorship, true);
				ref.setAuthorship(authorship);

				tcsElementName = "year";
				tcsNamespace = publicationNamespace;
				String strYear = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				TimePeriod datePublished = ImportHelper.getDatePublished(strYear);
				ref.setDatePublished(datePublished);

				//Reference
				//TODO
				tcsElementName = "parentPublication";
				tcsNamespace = publicationNamespace;
				String strParent = XmlHelp.getChildAttributeValue(elPublicationCitation, tcsElementName, tcsNamespace, "resource", rdfNamespace);
				Reference<?> parent = referenceMap.get(strParent);
				if (parent != null){
					if ((ref.getType().equals(ReferenceType.Article)) && (parent.getType().equals(ReferenceType.Journal))){
						((IArticle)ref).setInJournal(parent);
					}else if ((ref.getType().equals(ReferenceType.BookSection)) && (parent.getType().equals(ReferenceType.Book))){
						((IBookSection)ref).setInBook(parent);
					}else{
						logger.warn("parent type (parent: " + parent.getType() +", child("+strAbout+"): " + ref.getType() +  ")not yet implemented");
						//ref.setParent(parent);
					}
				}


				//FIXME
				//nomRef and reference
				tcsElementName = "shortTitle";
				tcsNamespace = publicationNamespace;
				boolean nomRefExists = false;
				String strShortTitle = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				if (! CdmUtils.Nz(strShortTitle).trim().equals("")){
					ref.setTitle(strShortTitle);
					ImportHelper.setOriginalSource(ref, config.getSourceReference(), strAbout, idNamespace);
					nomRefMap.put(strAbout, ref);
					nomRefCount++;
					nomRefExists = true;
				}

				tcsElementName = "title";
				tcsNamespace = publicationNamespace;
				String strTitle = elPublicationCitation.getChildText(tcsElementName, tcsNamespace);
				tcsNamespace = publicationNamespace;
				if (! CdmUtils.Nz(strTitle).trim().equals("")  || nomRefExists == false){
					//TODO
					Reference<?> biblioRef = (Reference<?>)ref.clone();
					biblioRef.setTitle(strTitle);
					ImportHelper.setOriginalSource(biblioRef, config.getSourceReference(), strAbout, idNamespace);
					referenceMap.put(strAbout, biblioRef);
					biblioRefsCount++;
				}


				checkAdditionalContents(elPublicationCitation, standardMappers, operationalMappers, unclearMappers);

				if (state.getConfig().isPublishReferences()){
					ref.addMarker(Marker.NewInstance(MarkerType.IN_BIBLIOGRAPHY(), false));
				}

				//ImportHelper.setOriginalSource(nameBase, tcsConfig.getSourceReference(), nameId);

			} catch (UnknownCdmTypeException e) {
				//FIXME
				logger.warn("Name with id " + strAbout + " has unknown type " + strPubType + " and could not be saved.");
				state.setUnsuccessfull();
			}
		}

		//change conceptRef uuid
		Reference<?> sec = referenceMap.get(config.getSourceSecId());
		if (sec == null){
			sec = nomRefMap.get(config.getSourceSecId());
		}
		if (sec != null){
			sec.setUuid(config.getSecUuid());
			logger.info("concept reference uuid changed to: " + config.getSecUuid());
		}

		//save and store in map
		logger.info("Save nomenclatural references (" + nomRefCount + ")");
		getReferenceService().save(nomRefMap.objects());
		logger.info("Save bibliographical references (" + biblioRefsCount +")");
		getReferenceService().save(referenceMap.objects());

		//referenceService.saveReferenceAll(referenceMap.objects());
		logger.info("end makeReferences ...");*/
		return;

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
    protected boolean isIgnore(TcsRdfImportState state){
		return (state.getConfig().getDoReferences() == IImportConfigurator.DO_REFERENCES.NONE);
	}

}
