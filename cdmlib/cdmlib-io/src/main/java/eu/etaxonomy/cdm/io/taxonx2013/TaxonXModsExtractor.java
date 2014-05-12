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

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

public class TaxonXModsExtractor extends TaxonXExtractor{

    private final Map<String,UUID> personMap = new HashMap<String, UUID>();

    Logger logger = Logger.getLogger(getClass());

    /**
     * @param agentService
     */
    public TaxonXModsExtractor(TaxonXImport importer) {
        this.importer = importer;
    }

    public Reference<?> extractMods(Node node){
        //        System.out.println("extractMods");
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
                            //                            ref.setTitleCache(content,true);
                            ref.setTitle(content);
                            //                            ref.generateTitle();
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
                    if (children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("GenericHash")) {
                        ref.setIssn("GenericHash: "+content);
                        try {
                            ref.setUri(new URI("http://plazi.cs.umb.edu/GgServer/search?MODS.ModsDocID="+content));
                        } catch (URISyntaxException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("mods:location")){
                NodeList tmp = children.item(i).getChildNodes();
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


            if (children.item(i).getNodeName().equalsIgnoreCase("mods:relatedItem")){
                addRelatedMods(children.item(i), modsMap, ref);
            }


        }
        modsMap.put("people",StringUtils.join(roleList.toArray(),SPLITTER));

        List<Reference> references = importer.getReferenceService().list(Reference.class, 0, 0, null, null);
        for(Reference<?> refe:references){
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

    private final String AUTHOR = "author";
    private final String EDITOR = "editor";
    /**
     * @param item
     * @return
     */
    private Map<String, String> getModsNames(Node node, Reference<?> ref) {
        NamedNodeMap attributeMap = node.getAttributes();
        Map<String,String> mapmap = new HashMap<String, String>();
        List<String> roleList = new ArrayList<String>();
        boolean newRole=false;
        String content="";
        String role =null;
        List<Person> persons = new ArrayList<Person>();
        List<String> editors= new ArrayList<String>();

        if ((attributeMap.getNamedItem("type") != null) && attributeMap.getNamedItem("type").getNodeValue().equalsIgnoreCase("personal")) {

            NodeList tmp = node.getChildNodes();
            for (int j=0;j<tmp.getLength();j++){

                //                System.out.println("Child of modsnametype: "+tmp.item(j).getNodeName());
                Person p=null;
                if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:namePart")) {
                    content=tmp.item(j).getTextContent().trim();
                    if (!content.isEmpty()) {
                        mapmap.put("namePart",content);
                        p = Person.NewInstance();
                        p.setTitleCache(content, true);
                    }
                }
                if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:role")) {
                    NodeList tmp2 = tmp.item(j).getChildNodes();
                    for (int k=0; k< tmp2.getLength();k++){
                        if (tmp2.item(k).getNodeName().equalsIgnoreCase("mods:roleTerm")){
                            content = tmp2.item(k).getTextContent().trim();
                            //                            System.out.println("ROLETERM!" +content);
                            if (!content.isEmpty()) {
                                roleList.add(content);
                                //                                p.setNomenclaturalTitle(content);
                                if (content.equalsIgnoreCase(EDITOR)) {
                                    role=EDITOR;
                                }
                                if (content.equalsIgnoreCase(AUTHOR)) {
                                    role=AUTHOR;
                                }
                                newRole=true;
                            }
                        }
                    }
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
                    if ((p!=null) && role.equals(EDITOR)) {
                        editors.add(p.getTitleCache());
                    }
                }
            }
        }
        if (persons.size()>0){
            if (persons.size()==1){
                ref.setAuthorTeam(persons.get(0));
            }
            else{
                Team authorTeam = Team.NewInstance();
                for (Person pers:persons){
                    authorTeam.addTeamMember(pers);
                }

                if (!personMap.containsKey(authorTeam.getTitleCache()) && (authorTeam.getTeamMembers().size()>0)){
                    UUID uuid = importer.getAgentService().saveOrUpdate(authorTeam);
                    personMap.put(authorTeam.getTitleCache(),uuid);
                }else{
                    if(authorTeam.getTeamMembers().size()>1) {
                        authorTeam =  (Team) importer.getAgentService().find(personMap.get(authorTeam.getTitleCache()));
                    }
                }

                ref.setAuthorTeam(authorTeam);
            }
        }
        if (editors.size()>0) {
            ref.setEditor(StringUtils.join(editors,", "));
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

        for (int j=0;j<tmp.getLength();j++){
            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:titleInfo")) {
                content=tmp.item(j).getTextContent().trim();
                if (!content.isEmpty()) {
                    relatedInfoMap.put("titleInfo",content);
                    if (node.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("host")){
                        List<Reference> references = importer.getReferenceService().list(Reference.class, 0, 0, null, null);
                        boolean refFound = false;
                        IBook book = null;
                        for (Reference<?> refe:references){
                            if(refe.getTitleCache().equalsIgnoreCase(content)){
                                refFound=true;
                                book= refe;
                            }
                        }
                        if (!refFound){
                            book = ReferenceFactory.newBook();
                            //                            book.setTitleCache(content,true);
                            book.setTitle(content);
                            //                            book.generateTitle();
                        }
                        if ((ref.getInBook() == null) || !ref.getInBook().equals(book)) {
                            ref.setInBook(book);
                            //                            ref.generateTitle();
                        }
                    }
                }
            }

            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:originInfo")) {
                children = tmp.item(j).getChildNodes();
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
            }


            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:name")){
                mapmap = getModsNames(tmp.item(j),ref);
                if (!mapmap.isEmpty()) {
                    roleList.add(mapmap.toString());
                }
            }
            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:part")){
                children = tmp.item(j).getChildNodes();
                partList = new ArrayList<String>();
                for (int i=0;i<children.getLength();i++){
                    mapmap = new HashMap<String, String>();
                    //                    System.out.println(children.item(i).getNodeName());

                    if (children.item(i).getNodeName().equalsIgnoreCase("mods:date")){
                        content = children.item(i).getTextContent().trim();
                        if (!content.isEmpty()){
                            date = TimePeriodParser.parseString(content);
                            ref.setDatePublished(date);
                        }
                    }
                    if (children.item(i).getNodeName().equalsIgnoreCase("mods:detail") &&
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
                    }
                    if (children.item(i).getNodeName().equalsIgnoreCase("mods:extent")) {
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