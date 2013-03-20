package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TaxonXXMLFieldGetter {

    private static final Logger logger = Logger.getLogger(TaxonXImport.class);
    private final TaxonXDataHolder dataHolder;
    private final String prefix;
    private final String path = "";
    private final Document doc;

    private final static String SPLITTER = ",";

    public TaxonXXMLFieldGetter(TaxonXDataHolder dataholder, String prefix,Document document){
        this.dataHolder = dataholder;
        this.prefix = prefix;
        this.doc = document;
    }


    public void parseFile(){
        //taxonx
        Node root = doc.getFirstChild();

        //taxonHeader, taxonBody
        NodeList nodes = root.getChildNodes();
        for (int i=0; i< nodes.getLength();i++) {
            System.out.println(nodes.item(i).getNodeName());
            if (nodes.item(i).getNodeName().equalsIgnoreCase("tax:taxonxheader")){
                NodeList nodes2 = nodes.item(i).getChildNodes();
                for (int j=0; j< nodes2.getLength();j++){
                    System.out.println("nodes2 : "+nodes2.item(j).getNodeName());
                    if (nodes2.item(j).getNodeName().equalsIgnoreCase("mods:mods")){
                        extractMods(nodes2.item(j));
                    }
                }
            }

        }
    }

    public Map<String,String> extractMods(Node node){
        Map<String, String> modsMap = new HashMap<String, String>();
        NodeList childs = node.getChildNodes();
        List<String> roleList = new ArrayList<String>();
        String content="";

        for (int i=0; i<childs.getLength();i++){
            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:titleinfo")){
                NodeList tmp = childs.item(i).getChildNodes();
                for (int j=0;j<tmp.getLength();j++){
                    System.out.println("Child of titleinfo: "+tmp.item(j).getNodeName());
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:title")) {
                        content=tmp.item(j).getTextContent().trim();
                        if (!content.isEmpty()) {
                            modsMap.put("mainTitle",content);
                        }
                    }
                }
            }

            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:name")){
                Map<String,String> mapmap = getModsNames(childs.item(i));
                    if (!mapmap.isEmpty()) {
                        roleList.add(mapmap.toString());
                    }
            }

            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:typeofresource")){
                content = childs.item(i).getTextContent().trim();
                if (!content.isEmpty()) {
                    modsMap.put("typeofresource",content);
                }
            }
            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:identifier")){
                content = childs.item(i).getTextContent().trim();
                if (!content.isEmpty()) {
                    modsMap.put(childs.item(i).getAttributes().getNamedItem("type").getNodeValue(),content);
                }
            }
            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:location")){
                NodeList tmp = childs.item(i).getChildNodes();
                for (int j=0;j<tmp.getLength();j++){
                    System.out.println("Child of mods:location: "+tmp.item(j).getNodeName());
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:url")) {
                        content = tmp.item(j).getTextContent().trim();
                        if (!content.isEmpty() && content != "http://un.availab.le") {
                            modsMap.put("url",content);
                        }
                    }
                }
            }

            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:relatedItem")){
               addRelatedMods(childs.item(i), modsMap);
            }

            if (childs.item(i).getNodeName().equalsIgnoreCase("mods:part")){
                NodeList tmp = childs.item(i).getChildNodes();
                List<String> partList = new ArrayList<String>();
                for (int j=0;j<tmp.getLength();j++){
                    Map<String,String> mapmap = new HashMap<String, String>();
                    if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:extent")) {
                        mapmap.put("type", tmp.item(j).getAttributes().getNamedItem("type").getNodeValue());
                        NodeList partNodes = tmp.item(j).getChildNodes();
                        for (int k=0; k<partNodes.getLength();k++){
                            if (partNodes.item(k).getAttributes().getNamedItem("start") !=null) {
                                content = partNodes.item(k).getTextContent().trim();
                                if (!content.isEmpty()) {
                                    mapmap.put("start",content);
                                }
                            }
                            if (partNodes.item(k).getAttributes().getNamedItem("end") !=null) {
                                content = partNodes.item(k).getTextContent().trim();
                                if (!content.isEmpty()) {
                                    mapmap.put("end",content);
                                }
                            }
                        }
                    }
                    partList.add(mapmap.toString());
                }
                modsMap.put("part",StringUtils.join(partList.toArray(),SPLITTER));
            }

        }
        modsMap.put("people",StringUtils.join(roleList.toArray(),SPLITTER));
        System.out.println(modsMap);
        return modsMap;
}
    /**
     * @param item
     * @return
     */
    private Map<String, String> getModsNames(Node node) {
        NamedNodeMap attributeMap = node.getAttributes();
        Map<String,String> mapmap = new HashMap<String, String>();
        List<String> roleList = new ArrayList<String>();

        String content="";
        if (attributeMap.getNamedItem("type") != null && attributeMap.getNamedItem("type").getNodeValue().equalsIgnoreCase("personal")) {
            NodeList tmp = node.getChildNodes();
            for (int j=0;j<tmp.getLength();j++){
                System.out.println("Child of modsnametype: "+tmp.item(j).getNodeName());
                if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:namePart")) {
                    content=tmp.item(j).getTextContent().trim();
                    if (!content.isEmpty()) {
                        mapmap.put("namePart",content);
                    }

                }
                if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:role")) {
                    NodeList tmp2 = tmp.item(j).getChildNodes();
                    for (int k=0; k< tmp2.getLength();k++){
                        if (tmp2.item(k).getNodeName().equalsIgnoreCase("mods:roleTerm")){

                            content = tmp2.item(k).getTextContent().trim();
                            System.out.println("ROLETERM!" +content);
                            if (content.isEmpty()) {
                                roleList.add(content);
                            }

                        }
                    }
                }
            }
        }
        mapmap.put("role",StringUtils.join(roleList.toArray(),SPLITTER));
        return mapmap;

    }


    /**
     * @param item
     * @param modsMap
     */
    private void addRelatedMods(Node node, Map<String, String> modsMap) {
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
                }

            }

            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:originInfo")) {
               NodeList childs = tmp.item(j).getChildNodes();
               List<String> originInfo = new ArrayList<String>();
               for (int i=0;i<childs.getLength();i++){
                   content=childs.item(i).getTextContent().trim();
                   if (!content.isEmpty()) {
                    originInfo.add(childs.item(i).getNodeName()+":"+content);
                }
               }
               relatedInfoMap.put("originInfo", StringUtils.join(originInfo.toArray(),SPLITTER));
            }


            if (tmp.item(j).getNodeName().equalsIgnoreCase("mods:name")){
                Map<String,String> mapmap = getModsNames(tmp.item(j));
                    if (!mapmap.isEmpty()) {
                        roleList.add(mapmap.toString());
                    }
            }
        }
        relatedInfoMap.put("relatedRoles", StringUtils.join(roleList.toArray(),SPLITTER));
        modsMap.put("relatedInfo",relatedInfoMap.toString());
    }
}
