package eu.etaxonomy.cdm.io.taxonx2013;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

public class TaxonXModsExtractor extends TaxonXExtractor{

    private final Map<String,UUID> personMap = new HashMap<String, UUID>();

    private final Logger logger = Logger.getLogger(getClass());


	private final String AUTHOR = "author";
    private final String EDITOR = "editor";

    /**
     * @param agentService
     */
    public TaxonXModsExtractor(TaxonXImport importer) {
        this.importer = importer;
    }

    public Reference extractMods(Node node){

    	//TODO needed? currently only filled but never read
        Map<String, String> modsMap = new HashMap<String, String>();
        NodeList children = node.getChildNodes();
        List<String> roleList = new ArrayList<String>();
        String content="";

        Reference ref = tryMakeReferenceByClassification(children);

        if (ref == null){
	        //        int reftype = askQuestion("What kind of reference is it?\n 1: Generic\n 2: Book\n 3: Article\n" +
	        //                " 4 : BookSection\n 5 : Journal\n 6 : Printseries\n 7: Thesis ");
	        int reftype=1;

	        ref = getReferenceWithType(reftype);
        }
        handleModsNames(children, ref);

        for (int i=0; i<children.getLength();i++){
        	Node modsChildNode = children.item(i);
        	String modsChildNodeName = modsChildNode.getNodeName();
            if (modsChildNodeName.equalsIgnoreCase("mods:titleinfo")){
                NodeList tmp = modsChildNode.getChildNodes();
                for (int j=0;j<tmp.getLength();j++){
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:title")) {
                        content=tmp.item(j).getTextContent().trim();
                        if (!content.isEmpty()) {
                            modsMap.put("mainTitle",content);
                            //                            ref.setTitleCache(content,true);
                            ref.setTitle(content);
                            //                            ref.generateTitle();
                        }
                    }
                }
            }else if (modsChildNodeName.equalsIgnoreCase("mods:name")){
               //handled separately
            }else if (modsChildNodeName.equalsIgnoreCase("mods:typeofresource")){
                content = modsChildNode.getTextContent().trim();
                if (!content.isEmpty()) {
                    modsMap.put("typeofresource",content);
                }
            }else if (modsChildNodeName.equalsIgnoreCase("mods:identifier")){
                content = modsChildNode.getTextContent().trim();
                if (!content.isEmpty()) {
                    modsMap.put(modsChildNode.getAttributes().getNamedItem("type").getNodeValue(),content);
                    if (modsChildNode.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("isbn")) {
                        ref.setIsbn(content);
                    }else if (modsChildNode.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("issn")) {
                        ref.setIssn(content);
                    }else if (modsChildNode.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("DOI")) {
                        try {
							ref.setDoi(DOI.fromString(content));
						} catch (IllegalArgumentException e) {
							logger.warn(content + " is not a vaild DOI");
						}
                    }else if (modsChildNode.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("GenericHash")) {
                        ref.setIssn("GenericHash: "+content);
                        try {
                            ref.setUri(new URI("http://plazi.cs.umb.edu/GgServer/search?MODS.ModsDocID="+content));
                        } catch (URISyntaxException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }else{
                    	logger.info("identifier " + modsChildNode.getAttributes().getNamedItem("type").getNodeValue() + " not yet handled.");
                    }
                }
            }else if (modsChildNodeName.equalsIgnoreCase("mods:location")){
                NodeList tmp = modsChildNode.getChildNodes();
                for (int j=0;j<tmp.getLength();j++){
                    //                    System.out.println("Child of mods:location: "+tmp.item(j).getNodeName());
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:url")) {
                        content = tmp.item(j).getTextContent().trim();
                        if (!content.isEmpty() && (content != "http://un.availab.le")) {
                            modsMap.put("url",content);
                            ref.setUri(URI.create(content));
                        }
                    }
                }
            }
            else if (modsChildNodeName.equalsIgnoreCase("mods:relatedItem")){
                addRelatedMods(modsChildNode, modsMap, ref);
            }else if (modsChildNodeName.equalsIgnoreCase("mods:classification")){
                    //already handled before
            }else if (modsChildNodeName.equalsIgnoreCase("#text") && modsChildNode.getTextContent().matches("\\s*")){
                //already handled before
            }else{
            	logger.warn("mods item not recognized yet: " + modsChildNodeName);
            }


        }
        modsMap.put("people",StringUtils.join(roleList.toArray(),SPLITTER));

        List<Reference> references = importer.getReferenceService().list(Reference.class, 0, 0, null, null);
        for(Reference refe:references){
            if (refe.getCitation().equalsIgnoreCase(ref.getCitation())) {
                ref=refe;
            }
        }
        //        System.out.println(modsMap);
        //
        //        System.out.println("REFERENCE "+ref.getCitation());
        //        System.out.println("REFERENCE "+ref.getTitle());
        //        System.out.println("REFERENCE "+ref.getTitleCache());
        return ref;
    }

    private void handleModsNames(NodeList children, Reference ref) {

        List<String> roleList = new ArrayList<String>();

        List<Person> persons = new ArrayList<Person>();
        List<String> editors= new ArrayList<String>();


    	//handle all mods:name
        for ( int i = 0; i<children.getLength(); i++){
    		if (children.item(i).getNodeName().equalsIgnoreCase("mods:name")){
    			NamedNodeMap attributeMap = children.item(i).getAttributes();
    			if ((attributeMap.getNamedItem("type") != null) && attributeMap.getNamedItem("type").getNodeValue().equalsIgnoreCase("personal")) {
    				handleNameTypePersonal(children.item(i), roleList, persons, editors);
    			} else if (attributeMap.getNamedItem("type") == null){
    				logger.warn("mods:name attribute 'type' is missing. Name not handled");
    			}else {
    				logger.warn("mods:name 'type' " + attributeMap.getNamedItem("type").getNodeValue() + " not yet supported");
    			}
    		}
    	}
        //evaluate authors and editors
       	if (persons.size()>0){
       		if (ref == null){
       			logger.warn("mods:name exists but reference is null");
       		}else if (persons.size()==1){
                ref.setAuthorship(persons.get(0));
            }
            else{
                Team authorship = Team.NewInstance();
                for (Person pers:persons){
                    authorship.addTeamMember(pers);
                }

                if (!personMap.containsKey(authorship.getTitleCache()) && (authorship.getTeamMembers().size()>0)){
                    UUID uuid = importer.getAgentService().saveOrUpdate(authorship);
                    personMap.put(authorship.getTitleCache(),uuid);
                }else{
                    if(authorship.getTeamMembers().size()>1) {
                    	UUID uuid = personMap.get(authorship.getTitleCache());
                        authorship =  (Team) importer.getAgentService().find(uuid);
                    }
                }

                ref.setAuthorship(authorship);
            }
            if (editors.size()>0) {
                ref.setEditor(StringUtils.join(editors,", "));
            }
        }
	}

	/**
     * Extracts the reference with correct type from mods:classification.
     * Incomplete implementation. Will be filled whenever new cases show up
     * @param children list of all children of mods:mods
     * @return
     */
    //http://www.loc.gov/standards/mods/userguide/classification.html
    private Reference tryMakeReferenceByClassification(NodeList children) {
        for (int i=0; i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("mods:classification")){
            	Node classificationNode = children.item(i);
            	String text = classificationNode.getTextContent();
            	if ("journal article".equals(text)){
            		return ReferenceFactory.newArticle();
            	}else if ("book".equals(text)){
                	return ReferenceFactory.newBook();
            	}else{
            		if (StringUtils.isNotBlank(text)){
            			logger.warn("mods:classification could not be recognized: " + text);
            		}else{
            			logger.warn("mods:classification has not text. ");
            		}
            	}
            }
        }
		return null;
	}


    private void handleNameTypePersonal(Node node, List<String> roleList, List<Person> persons, List<String> editors) {
    	boolean newRole=false;
        String content="";
        String role =null;

        List<String> nameParts = new ArrayList<String>();

    	NodeList tmp = node.getChildNodes();
        for (int j=0;j<tmp.getLength();j++){

            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:namePart")) {
                content=tmp.item(j).getTextContent().trim();
                if (! content.isEmpty()) {
                	nameParts.add(content);
                }
            } else if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:role")) {
                NodeList roleChildren = tmp.item(j).getChildNodes();
                for (int k=0; k< roleChildren.getLength();k++){
                    if (roleChildren.item(k).getNodeName().equalsIgnoreCase("mods:roleTerm")){
                        content = roleChildren.item(k).getTextContent().trim();
                        if (!content.isEmpty()) {
                            roleList.add(content);
                            //                                p.setNomenclaturalTitle(content);
                            if (content.equalsIgnoreCase(EDITOR)) {
                                role=EDITOR;
                            }
                            else if (content.equalsIgnoreCase(AUTHOR)) {
                                role=AUTHOR;
                            }
                            newRole=true;
                        }
                    }
                }
            }

        }

        Person p=null;
        if (! nameParts.isEmpty()){
            p = Person.NewInstance();
            p.setTitleCache(StringUtils.join(nameParts.toArray(), " "), true);
        }

        if (newRole){
            if ((p!=null) && role.equals(AUTHOR)) {
                UUID uuid = null;
                if (!personMap.containsKey(p.getTitleCache())){
                    uuid = importer.getAgentService().saveOrUpdate(p);
                    p = (Person) importer.getAgentService().find(uuid);
                    personMap.put(p.getTitleCache(),uuid);
                }else{
                    uuid = personMap.get(p.getTitleCache());
                    p = (Person) importer.getAgentService().find(uuid);
                }
                //                        logger.info("ADD PERSON "+p);
                persons.add(p);
            }
            else if ((p!=null) && role.equals(EDITOR)) {
                editors.add(p.getTitleCache());
            }
        }
	}

	/**
     * @param item
     * @param modsMap
     */
    private void addRelatedMods(Node node, Map<String, String> modsMap, Reference ref) {
        NodeList tmp =node.getChildNodes();
        NodeList partNodes = null;
        NodeList children = null;

        List<String> originInfo = null;
        List<String> partList = null;

        TimePeriod date;

        String publisher="";
        String publishplace="";
        String pstart="";
        String pend="";

        Map<String,String> mapmap=null;

        Map<String, String> relatedInfoMap = new HashMap<String, String>();
        List<String> roleList = new ArrayList<String>();
        String content="";

        relatedInfoMap.put("type",node.getAttributes().getNamedItem("type").getNodeValue());


        Reference inRef = null;
        for (int j=0;j<tmp.getLength();j++){
        	Node childNode = tmp.item(j);
        	String childNodeName = childNode.getNodeName();
	        if (childNodeName.equalsIgnoreCase("#text")  && childNode.getTextContent().matches("\\s*")){
	        	//do nothing
	        } else if (childNodeName.equalsIgnoreCase("mods:titleInfo")) {
                content=childNode.getTextContent().trim();
                if (!content.isEmpty()) {
                    relatedInfoMap.put("titleInfo",content);
                    if (node.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("host")){
                        List<Reference> references = importer.getReferenceService().list(Reference.class, 0, 0, null, null);
                        boolean refFound = false;
                        for (Reference tmpRef:references){
                            if(tmpRef.getTitleCache().equalsIgnoreCase(content)){
                                refFound = true;
                                inRef= tmpRef;
                            }
                        }
                        if (!refFound){
                            inRef = getBestInreference(ref);
                            if (inRef == null){
                            	inRef = ReferenceFactory.newGeneric();
                            }

                            //book.setTitleCache(content,true);
                            inRef.setTitle(content);
                        }
                        if ((ref.getInReference() == null) || !ref.getInReference().equals(inRef)) {
                            ref.setInReference(inRef);
                        }else{
                        	//TODO
                        }
                    }
                }
            } else if (childNodeName.equalsIgnoreCase("mods:originInfo")) {
                children = childNode.getChildNodes();
                originInfo = new ArrayList<String>();
                for (int i=0;i<children.getLength();i++){
                    content=children.item(i).getTextContent().trim();
                    if (!content.isEmpty()) {
                        originInfo.add(children.item(i).getNodeName()+":"+content);
                        if (children.item(i).getNodeName().contains("dateIssued")) {
                            ref.setDatePublished(TimePeriodParser.parseString(content));
                        }
                    }
                    publisher="";
                    publishplace="";
                    if (children.item(i).getNodeName().contains("publisher")) {
                        try{
                            publisher=children.item(i).getChildNodes().item(0).getTextContent().trim();
                            //                            System.out.println("PUBLISHER "+publisher);
                        }catch(Exception e){System.out.println("oups "+e);}
                    }
                    if (children.item(i).getNodeName().contains("place")) {
                        try{
                            publishplace=children.item(i).getTextContent().trim();
                            //                            System.out.println("PUBLISHED "+publishplace);
                        }catch(Exception e){System.out.println("oups "+e);}
                    }
                    if (publishplace.isEmpty() && !publisher.isEmpty()) {
                        ref.setPublisher(publisher);
                    }
                    if (!publishplace.isEmpty() && !publisher.isEmpty()) {
                        ref.setPublisher(publisher, publishplace);
                    }
                }
                relatedInfoMap.put("originInfo", StringUtils.join(originInfo.toArray(),SPLITTER));
            } else if (childNodeName.equalsIgnoreCase("mods:name")){
            	//handled later
            } else if (childNodeName.equalsIgnoreCase("mods:part")){
                children = childNode.getChildNodes();
                partList = new ArrayList<String>();
                for (int i=0;i<children.getLength();i++){
                    mapmap = new HashMap<String, String>();
                    //                    System.out.println(children.item(i).getNodeName());

                    if (children.item(i).getNodeName().equalsIgnoreCase("#text")  && children.item(i).getTextContent().matches("\\s*")){
        	        	//do nothing
        	        } else if (children.item(i).getNodeName().equalsIgnoreCase("mods:date")){
                        content = children.item(i).getTextContent().trim();
                        if (!content.isEmpty()){
                            date = TimePeriodParser.parseString(content);
                            //TODO need to check if date belongs to ref or inref
                            ref.setDatePublished(date);
                        }
                    } else if (children.item(i).getNodeName().equalsIgnoreCase("mods:detail") &&
                            children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("volume")){
                        partNodes = children.item(i).getChildNodes();
                        for (int k=0; k<partNodes.getLength();k++){
                            if (partNodes.item(k).getNodeName().equalsIgnoreCase("mods:number")) {
                                content = partNodes.item(k).getTextContent().trim();
                                if (!content.isEmpty()) {
                                    ref.setVolume(content);
                                }
                            }
                        }
                    } else if (children.item(i).getNodeName().equalsIgnoreCase("mods:extent")) {
                        mapmap.put("unit", children.item(i).getAttributes().getNamedItem("unit").getNodeValue());
                        partNodes = children.item(i).getChildNodes();
                        pstart="";
                        pend="";
                        for (int k=0; k<partNodes.getLength();k++){
                            if (partNodes.item(k).getNodeName().equalsIgnoreCase("mods:start")) {
                                content = partNodes.item(k).getTextContent().trim();
                                if (!content.isEmpty()) {
                                    mapmap.put("start",content);
                                    pstart=content;
                                }
                            }
                            if (partNodes.item(k).getNodeName().equalsIgnoreCase("mods:end")) {
                                content = partNodes.item(k).getTextContent().trim();
                                if (!content.isEmpty()) {
                                    mapmap.put("end",content);
                                    pend=content;
                                }
                            }
                        }
                        //                        System.out.println("SET PAGES "+pstart+"-"+pend);
                        ref.setPages(pstart+"-"+pend);
                    }else{
                    	logger.warn("mods:part not yet supported: " + children.item(i).getNodeName());
                    }
                    partList.add(mapmap.toString());
                }
                modsMap.put("part",StringUtils.join(partList.toArray(),SPLITTER));
            }else{
            	logger.warn("relatedItem child not yet supported: " + childNodeName);
            }
        }


        handleModsNames(children, inRef);

        relatedInfoMap.put("relatedRoles", StringUtils.join(roleList.toArray(),SPLITTER));
        modsMap.put("relatedInfo",relatedInfoMap.toString());
    }


	/**
	 * Returns empty reference which best fits to the given ref as in-reference.
	 * TODO move to {@link ReferenceType} or {@link ReferenceFactory}
	 * @param ref
	 * @return
	 */
	private Reference getBestInreference(Reference ref) {
		if (ref.getType().equals(ReferenceType.Article)){
			return ReferenceFactory.newJournal();
		}else if (ref.getType().equals(ReferenceType.BookSection)){
			return ReferenceFactory.newBook();
		}else{
			//TODO support more types
			logger.warn("In-Reference type not yet supported for :" + ref.getType());

		}
		return null;
	}

}