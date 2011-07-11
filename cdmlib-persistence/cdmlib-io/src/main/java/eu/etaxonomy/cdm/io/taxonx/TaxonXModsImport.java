/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.IPublicationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
@Component
public class TaxonXModsImport extends CdmIoBase<TaxonXImportState> implements ICdmIO<TaxonXImportState> {
	private static final Logger logger = Logger.getLogger(TaxonXModsImport.class);

	@SuppressWarnings("unused")
	private static int modCount = 10000;
	private ReferenceFactory refFactory = ReferenceFactory.newInstance();
	public TaxonXModsImport(){
		super();
	}
	
	public boolean doCheck(TaxonXImportState state){
		boolean result = true;
		logger.warn("Checking for TaxonXMods not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		
		return result;
	}

	public boolean doInvoke(TaxonXImportState state){		
		boolean success = true;
		logger.info("start make mods reference ...");
		TaxonXImportConfigurator config = state.getConfig();
		Element root = config.getSourceRoot();
		Namespace nsTaxonx = root.getNamespace();
		Namespace nsMods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
		
		state.setModsReference(null);  //delete old reference
		Element elTaxonHeader = root.getChild("taxonxHeader", nsTaxonx);
		if (elTaxonHeader != null){
			Element elMods = elTaxonHeader.getChild("mods", nsMods);
			if (elMods != null){
				Reference<?> ref = refFactory.newGeneric();
				//TitleInfo
				Element elTitleInfo = elMods.getChild("titleInfo", nsMods);
				if (elTitleInfo != null){
					success &= makeTitleInfo(elTitleInfo, ref);
				}else{
					logger.warn("TitleInfo element is missing in " + state.getConfig().getSource());
				}
				//mods name
				Element elModsName = elMods.getChild("name", nsMods);
				success &= makeModsName(elModsName, ref);
				//origin info
				Element elOriginInfo = elMods.getChild("originInfo", nsMods);
				success &= makeOriginInfo(elOriginInfo, ref);
				
				//publish
				if (state.getConfig().isPublishReferences()){
					boolean publish = false;
					ref.addMarker(Marker.NewInstance(MarkerType.IN_BIBLIOGRAPHY(), publish));
				}
				
				//save
				state.setModsReference(ref);
				
			}
		}

		logger.info("end make mods reference ...");
		return true;
	}
	

	/**
	 * @param contentList
	 * @return
	 */
	private void removeEmptyContent(List<Content> contentList) {
		List<Content> removeList = new ArrayList<Content>();
		for (Content content: contentList){
			if (content instanceof Text){
				if ( CdmUtils.isEmpty(((Text) content).getTextNormalize())){
					removeList.add(content);
				}
			}
		}
		contentList.removeAll(removeList);
	}
	
	/**
	 * @param elModsName
	 * @param ref
	 */
	private boolean makeOriginInfo(Element elOriginInfo, Reference<?> ref) {
		Namespace nsMods = elOriginInfo.getNamespace();
		List<Content> contentList = elOriginInfo.getContent();
		
		//dateIssued
		Element elDateIssued = elOriginInfo.getChild("dateIssued", nsMods);
		if (elDateIssued != null){
			String dateIssued = elDateIssued.getTextNormalize();
			contentList.remove(elDateIssued);
			
			TimePeriod datePublished = TimePeriod.parseString(dateIssued);
			if (ref.getType().isPublication()){
				((IPublicationBase)ref).setDatePublished(datePublished );
			}else{
				logger.warn("Reference has issue date but is not of type publication base. Date was not set");
			}
		}
		
		//dateIssued
		Element elPublisher = elOriginInfo.getChild("publisher", nsMods);
		if (elPublisher != null){
			String publisher = elPublisher.getTextNormalize();
			contentList.remove(elPublisher);
			
			if (ref.getType().isPublication()){
				((IPublicationBase)ref).setPublisher(publisher);
			}else{
				logger.warn("Reference has publisher but is not of type publication base. Publisher was not set");
			}
		}
		
		removeEmptyContent(contentList);
		for (Content o: contentList){
			logger.warn(o + " (in mods:originInfo) not yet implemented for mods import");
		}
		return true;
	}


	/**
	 * @param elModsName
	 * @param ref
	 */
	//TODO
	//THIS implementation is against the mods semantics but supports the current
	//format for palmae taxonX files
	//The later has to be changed and this part has to be adapted
	private boolean makeModsName(Element elModsName, Reference<?> ref) {
		int UNPARSED = 0;
		int PARSED = 1;
		Namespace nsMods = elModsName.getNamespace();
		List<Content> contentList = elModsName.getContent();
		Team authorTeam = Team.NewInstance();
		
		//name
		List<Element> elNameParts = elModsName.getChildren("namePart", nsMods);
		int mode = UNPARSED;
		if (elNameParts.size() > 0){
			if (elNameParts.get(0).getAttributes().size() > 0){
				mode = PARSED;
			}
		}
		
		if (mode == 0){
			Element elNamePart = elNameParts.get(0); 
			if (elNamePart != null){
				String namePart = elNamePart.getTextNormalize();
				contentList.remove(elNamePart);
				authorTeam.setTitleCache(namePart, true);
			}
			if (elNameParts.size()> 1){
				logger.warn("Multiple nameparts of unexpected type");
			}
		}else{
			
			Person lastTeamMember = Person.NewInstance();
			List<Element> tmpNamePartList = new ArrayList<Element>();
			tmpNamePartList.addAll(elNameParts);
			for (Element elNamePart: tmpNamePartList){
				if (elNamePart.getAttributeValue("type").equals("family")){
					lastTeamMember = Person.NewInstance();
					authorTeam.addTeamMember(lastTeamMember);
					lastTeamMember.setLastname(elNamePart.getTextNormalize());
				}else if (elNamePart.getAttributeValue("type").equals("given")){
					lastTeamMember.setFirstname(elNamePart.getTextNormalize());
				}else{
					logger.warn("Unsupport name part type");
				}
				contentList.remove(elNamePart);
			}
		}
		ref.setAuthorTeam(authorTeam);
		
		removeEmptyContent(contentList);
		for (Content o: contentList){
			logger.warn(o + " (in mods:name) not yet implemented for mods import");
		}
		return true;
	}

	/**
	 * @param elTitleInfo
	 * @param ref
	 */
	private boolean makeTitleInfo(Element elTitleInfo, Reference<?> ref) {
		Namespace nsMods = elTitleInfo.getNamespace();
		List<Content> contentList = elTitleInfo.getContent();
		
		//title
		Element elTitle = elTitleInfo.getChild("title", nsMods);
		if (elTitle != null){
			String title = elTitle.getTextNormalize();
			contentList.remove(elTitle);
			ref.setTitle(title);
		}
		removeEmptyContent(contentList);
		for (Content o: contentList){
			logger.warn(o + " (in titleInfo) not yet implemented for mods import");
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(TaxonXImportState state){
		return ! state.getConfig().isDoMods();
	}
	
}
