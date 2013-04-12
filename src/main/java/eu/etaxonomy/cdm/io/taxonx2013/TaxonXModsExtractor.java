package eu.etaxonomy.cdm.io.taxonx2013;

import java.net.URI;
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

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public class TaxonXModsExtractor extends TaxonXExtractor{

    private final Map<String,UUID> personMap = new HashMap<String, UUID>();
    private final IAgentService agentService;

    Logger logger = Logger.getLogger(getClass());

    /**
     * @param agentService
     */
    public TaxonXModsExtractor(IAgentService agentService) {
        this.agentService = agentService;
    }

    public Reference<?> extractMods(Node node){
        System.out.println("extractMods");
        Map<String, String> modsMap = new HashMap<String, String>();
        NodeList children = node.getChildNodes();
        List<String> roleList = new ArrayList<String>();
        String content="";

//        int reftype = askQuestion("What kind of reference is it?\n 1: Generic\n 2: Book\n 3: Article\n" +
//                " 4 : BookSection\n 5 : Journal\n 6 : Printseries\n 7: Thesis ");
        int reftype=4;
        Reference<?> ref= getReferenceType(reftype);
        for (int i=0; i<children.getLength();i++){

            if (children.item(i).getNodeName().equalsIgnoreCase("mods:titleinfo")){
                NodeList tmp = children.item(i).getChildNodes();
                for (int j=0;j<tmp.getLength();j++){
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:title")) {
                        content=tmp.item(j).getTextContent().trim();
                        if (!content.isEmpty()) {
                            modsMap.put("mainTitle",content);
                            ref.setTitleCache(content,true);
                            ref.setTitle(content);
                            ref.generateTitle();
                            System.out.println("REFERENCE "+ref.getTitleCache());
                        }
                    }
                }
            }

            if (children.item(i).getNodeName().equalsIgnoreCase("mods:name")){
                Map<String,String> mapmap = getModsNames(children.item(i), ref);
                if (!mapmap.isEmpty()) {
                    roleList.add(mapmap.toString());
                }
            }

            if (children.item(i).getNodeName().equalsIgnoreCase("mods:typeofresource")){
                content = children.item(i).getTextContent().trim();
                if (!content.isEmpty()) {
                    modsMap.put("typeofresource",content);
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("mods:identifier")){
                content = children.item(i).getTextContent().trim();
                if (!content.isEmpty()) {
                    modsMap.put(children.item(i).getAttributes().getNamedItem("type").getNodeValue(),content);
                    if (children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("isbn")) {
                        ref.setIsbn(content);
                    }
                    if (children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("issn")) {
                        ref.setIssn(content);
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("mods:location")){
                NodeList tmp = children.item(i).getChildNodes();
                for (int j=0;j<tmp.getLength();j++){
//                    System.out.println("Child of mods:location: "+tmp.item(j).getNodeName());
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:url")) {
                        content = tmp.item(j).getTextContent().trim();
                        if (!content.isEmpty() && content != "http://un.availab.le") {
                            modsMap.put("url",content);
                            ref.setUri(URI.create(content));
                        }
                    }
                }
            }


            if (children.item(i).getNodeName().equalsIgnoreCase("mods:relatedItem")){
                addRelatedMods(children.item(i), modsMap, ref);
            }


        }
        modsMap.put("people",StringUtils.join(roleList.toArray(),SPLITTER));
        System.out.println(modsMap);
        return ref;
    }

    /**
     * @param item
     * @return
     */
    private Map<String, String> getModsNames(Node node, Reference<?> ref) {
        NamedNodeMap attributeMap = node.getAttributes();
        Map<String,String> mapmap = new HashMap<String, String>();
        List<String> roleList = new ArrayList<String>();
        boolean newPerson=false;
        boolean newTeam=false;
        String content="";
        Team authorTeam = Team.NewInstance();
        if (attributeMap.getNamedItem("type") != null && attributeMap.getNamedItem("type").getNodeValue().equalsIgnoreCase("personal")) {

            NodeList tmp = node.getChildNodes();
            for (int j=0;j<tmp.getLength();j++){
                newPerson=false;
//                System.out.println("Child of modsnametype: "+tmp.item(j).getNodeName());
                Person p = Person.NewInstance();
                if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:namePart")) {
                    content=tmp.item(j).getTextContent().trim();
                    if (!content.isEmpty()) {
                        mapmap.put("namePart",content);
                        p.setTitleCache(content, true);
                        newPerson=true;
                    }
                }
                if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:role")) {
                    NodeList tmp2 = tmp.item(j).getChildNodes();
                    for (int k=0; k< tmp2.getLength();k++){
                        if (tmp2.item(k).getNodeName().equalsIgnoreCase("mods:roleTerm")){
                            content = tmp2.item(k).getTextContent().trim();
                            System.out.println("ROLETERM!" +content);
                            if (!content.isEmpty()) {
                                roleList.add(content);
                                p.setNomenclaturalTitle(content);
                                newPerson=true;
                            }
                        }
                    }
                }
                if (!personMap.containsKey(p.getTitleCache()) && newPerson){
                    UUID uuid = agentService.saveOrUpdate(p);
                    personMap.put(p.getTitleCache(),uuid);
                }else{
                    p = (Person) agentService.find(personMap.get(p.getTitleCache()));
                }
                if (newPerson) {
                    authorTeam.addTeamMember(p);
                    newTeam=true;
                }
            }
        }
        if (!personMap.containsKey(authorTeam.getTitleCache()) && newTeam){
            UUID uuid = agentService.saveOrUpdate(authorTeam);
            personMap.put(authorTeam.getTitleCache(),uuid);
        }else{
            authorTeam =  (Team) agentService.find(personMap.get(authorTeam.getTitleCache()));
        }
        if (newTeam) {
            ref.setAuthorTeam(authorTeam);
        }
        mapmap.put("role",StringUtils.join(roleList.toArray(),SPLITTER));
        return mapmap;
    }


    /**
     * @param item
     * @param modsMap
     */
    private void addRelatedMods(Node node, Map<String, String> modsMap, Reference<?> ref) {
        NodeList tmp =node.getChildNodes();
        Map<String, String> relatedInfoMap = new HashMap<String, String>();
        List<String> roleList = new ArrayList<String>();
        String content="";

        relatedInfoMap.put("type",node.getAttributes().getNamedItem("type").getNodeValue());

        for (int j=0;j<tmp.getLength();j++){
            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:titleInfo")) {
                content=tmp.item(j).getTextContent().trim();
                if (!content.isEmpty()) {
                    relatedInfoMap.put("titleInfo",content);
                    if (node.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("host")){
                        IBook b = ReferenceFactory.newBook();
                        b.setTitleCache(content,true);
                        b.setTitle(content);
                        b.generateTitle();
                        ref.setInBook(b);
                    }
                }

            }

            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:originInfo")) {
                NodeList children = tmp.item(j).getChildNodes();
                List<String> originInfo = new ArrayList<String>();
                for (int i=0;i<children.getLength();i++){
                    content=children.item(i).getTextContent().trim();
                    if (!content.isEmpty()) {
                        originInfo.add(children.item(i).getNodeName()+":"+content);
                        if (children.item(i).getNodeName().contains("dateIssued")) {
                            ref.setDatePublished(TimePeriod.parseString(content));
                        }
                    }
                    String publisher="";
                    String publishplace="";
                    if (children.item(i).getNodeName().contains("publisher")) {
                        try{
                            publisher=children.item(i).getChildNodes().item(0).getTextContent().trim();
                            System.out.println("PUBLISHER "+publisher);
                        }catch(Exception e){System.out.println("oups "+e);}
                    }
                    if (children.item(i).getNodeName().contains("place")) {
                        try{
                            publishplace=children.item(i).getTextContent().trim();
                            System.out.println("PUBLISHED "+publishplace);
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
            }


            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:name")){
                Map<String,String> mapmap = getModsNames(tmp.item(j),ref);
                if (!mapmap.isEmpty()) {
                    roleList.add(mapmap.toString());
                }
            }
            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:part")){
                NodeList children = tmp.item(j).getChildNodes();
                List<String> partList = new ArrayList<String>();
                for (int i=0;i<children.getLength();i++){
                    Map<String,String> mapmap = new HashMap<String, String>();
                    System.out.println(children.item(i).getNodeName());
                    if (children.item(i).getNodeName().equalsIgnoreCase("mods:extent")) {
                        mapmap.put("unit", children.item(i).getAttributes().getNamedItem("unit").getNodeValue());
                        NodeList partNodes = children.item(i).getChildNodes();
                        String pstart="";
                        String pend="";
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
                        System.out.println("SET PAGES "+pstart+"-"+pend);
                        ref.setPages(pstart+"-"+pend);
                    }
                    partList.add(mapmap.toString());
                }
                modsMap.put("part",StringUtils.join(partList.toArray(),SPLITTER));
            }
        }
        relatedInfoMap.put("relatedRoles", StringUtils.join(roleList.toArray(),SPLITTER));
        modsMap.put("relatedInfo",relatedInfoMap.toString());
    }

}