package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

public class Abcd206XMLFieldGetter {

    private static final Logger logger = Logger.getLogger(Abcd206Import.class);
    private final Abcd206DataHolder dataHolder;
    private final String prefix;
    private String path = "";
    boolean DEBUG =false;

    public Abcd206XMLFieldGetter(Abcd206DataHolder dataholder, String prefix){
        this.dataHolder = dataholder;
        this.prefix = prefix;
    }


    /**
     * getType :
     * @param result
     */
    protected void getType(Node result) {
        if (DEBUG) {
            logger.info("GetType");
        }
        NodeList results, types, ntds, ntd;
        String type;
        results = result.getChildNodes();
        boolean typeFound = false;
        try {
            for (int k = 0; k < results.getLength(); k++) {
                typeFound=false;
                if (results.item(k).getNodeName()
                        .equals(prefix + "SpecimenUnit")) {
                    types = results.item(k).getChildNodes();
                    for (int l = 0; l < types.getLength(); l++) {
                        if (types.item(l).getNodeName().equals(prefix+ "NomenclaturalTypeDesignations")) {
                            ntds = types.item(l).getChildNodes();
                            for (int m = 0; m < ntds.getLength(); m++) {
                                if (ntds.item(m).getNodeName().equals(prefix+ "NomenclaturalTypeDesignation")) {
                                    ntd = ntds.item(m).getChildNodes();
                                    for (int n = 0; n < ntd.getLength(); n++) {
                                        if (ntd.item(n).getNodeName().equals(prefix + "TypeStatus"))
                                        {
                                            type = ntd.item(n).getTextContent();
                                            if(DEBUG) {
                                                logger.info("ADD "+type);
                                            }
                                            dataHolder.statusList.add(getSpecimenTypeDesignationStatusByKey(type));
                                            typeFound=true;
                                            path = ntd.item(l).getNodeName();
                                            getHierarchie(ntd.item(l));
                                            dataHolder.knownABCDelements.add(path);
                                            path = "";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!typeFound) {
                    dataHolder.statusList.add(null);
                }
            }
        } catch (NullPointerException e) {
            dataHolder.statusList = new ArrayList<SpecimenTypeDesignationStatus>();
        }
    }

    /**
     * getScientificNames : get the list of scientific names (preferred and non preferred)
     * @param group
     */
    protected void getScientificNames(NodeList group) {
        NodeList identifications, results;
        String tmpName = null;
        boolean nameFound = false;

        for (int j = 0; j < group.getLength(); j++) {
            if (group.item(j).getNodeName().equals(prefix + "Identification")) {
                identifications = group.item(j).getChildNodes();
                for (int m = 0; m < identifications.getLength(); m++) {
                    if (identifications.item(m).getNodeName()
                            .equals(prefix + "Result")) {
                        results = identifications.item(m).getChildNodes();
                        for (int k = 0; k < results.getLength(); k++) {
                            if (results.item(k).getNodeName().equals(prefix + "TaxonIdentified")) {
                                tmpName = this.getScientificName(results.item(k));
                                // logger.info("TMP NAME " + tmpName);dataHolder.identificationList.add(tmpName);nameFound = true;
                            }
                        }
                    } else if (identifications.item(m).getNodeName().equals(prefix + "PreferredFlag")) {
                        if (dataHolder.nomenclatureCode != null&& dataHolder.nomenclatureCode != "") {
                            // logger.info("TMP NAME P" + tmpName);

                            dataHolder.identificationList.add(tmpName+ "_preferred_"+ identifications.item(m).getTextContent()+ "_code_" + dataHolder.nomenclatureCode);
                        } else {
                            dataHolder.identificationList.add(tmpName+ "_preferred_"+ identifications.item(m).getTextContent());
                        }
                        path = identifications.item(m).getNodeName();
                        // getHierarchie(identifications.item(m));
                        dataHolder.knownABCDelements.add(path);
                        path = "";
                        try {
                            dataHolder.identificationList.remove(tmpName);
                        } catch (Exception e) {
                            logger.info("ohooooooooooo:" + e);
                        }
                    } else if (identifications.item(m).getNodeName().equals(prefix + "References")) {
                        this.getReferences(identifications.item(m));
                    }
                }
            }
        }
        boolean hasPref = false;
        for (int j = 0; j < group.getLength(); j++) {
            if (group.item(j).getNodeName().equals(prefix + "Identification")) {
                dataHolder.nomenclatureCode = "";
                identifications = group.item(j).getChildNodes();
                for (int m = 0; m < identifications.getLength(); m++) {
                    if (identifications.item(m).getNodeName().equals(prefix + "Result")) {
                        results = identifications.item(m).getChildNodes();
                        for (int k = 0; k < results.getLength(); k++) {
                            if (results.item(k).getNodeName().equals(prefix + "TaxonIdentified")) {
                                tmpName = this.getScientificName(results.item(k));
                            }
                        }
                    }
                    if (identifications.item(m).getNodeName()
                            .equals(prefix + "PreferredFlag")) {
                        hasPref = true;
                    }
                }
                if (!hasPref && tmpName != null) {
                    if (dataHolder.nomenclatureCode != null
                            && dataHolder.nomenclatureCode != "") {
                        dataHolder.identificationList.add(tmpName+ "_preferred_" + "0" + "_code_"+ dataHolder.nomenclatureCode);
                    } else {
                        dataHolder.identificationList.add(tmpName+ "_preferred_" + "0");
                    }
                    try {
                        dataHolder.identificationList.remove(tmpName);
                    } catch (Exception e) {
                        logger.info("ohooooooooooo:" + e);
                    }
                }
            }
        }
    }

    /**
     * getScientificName : get the list of scientific names
     * @param result
     * @return
     */
    private String getScientificName(Node result) {
        // logger.info("IN getScientificName " + dataHolder.nomenclatureCode);
        NodeList taxonsIdentified, scnames, atomised;
        String tmpName = "";
        dataHolder.atomisedStr = "";
        taxonsIdentified = result.getChildNodes();
        for (int l = 0; l < taxonsIdentified.getLength(); l++) {

            if (taxonsIdentified.item(l).getNodeName().equals(prefix + "ScientificName")) {
                scnames = taxonsIdentified.item(l).getChildNodes();
                for (int n = 0; n < scnames.getLength(); n++) {

                    if (scnames.item(n).getNodeName().equals(prefix + "FullScientificNameString")) {
                        path = scnames.item(n).getNodeName();
                        tmpName = scnames.item(n).getTextContent();
                        getHierarchie(scnames.item(n));
                        dataHolder.knownABCDelements.add(path);
                        path = "";
                    }
                    if (scnames.item(n).getNodeName().equals(prefix + "NameAtomised")) {
                        try {
                            if (scnames.item(n).hasChildNodes()) {String tmp = scnames.item(n).getChildNodes().item(1).getNodeName();if (tmp.indexOf(prefix) != -1&& prefix.length() > 0) {
                                dataHolder.nomenclatureCode = tmp.split(prefix)[1];
                            } else {dataHolder.nomenclatureCode = scnames.item(n).getChildNodes().item(1).getNodeName();}
                            }
                        } catch (Exception e) {
                            if(DEBUG) {
                                logger.warn("PB nomenclaturecode");
                            }
                            dataHolder.nomenclatureCode = "";
                        }
                        atomised = scnames.item(n).getChildNodes().item(1).getChildNodes();
                        dataHolder.atomisedIdentificationList.add(this.getAtomisedNames(dataHolder.nomenclatureCode,atomised));
                    }
                }
            }
        }
        return tmpName;
    }

    /**
     * getRerences : get the reference and citations
     * @param result
     * @return
     */
    protected boolean getReferences(Node result) {
        // logger.info("GET REFERENCE");
        NodeList results, reference;
        results = result.getChildNodes();
        boolean referencefound = false;
        for (int k = 0; k < results.getLength(); k++) {
            if (results.item(k).getNodeName().equals(prefix + "SourceReference")) {
                reference = results.item(k).getChildNodes();
                for (int l = 0; l < reference.getLength(); l++) {
                    if (reference.item(l).getNodeName().equals(prefix + "TitleCitation")) {
                        path = reference.item(l).getNodeName();
                        dataHolder.referenceList.add(reference.item(l).getTextContent());
                        getHierarchie(reference.item(l));
                        dataHolder.knownABCDelements.add(path);
                        path = "";
                        referencefound = true;
                    }
                }
            }
        }
        return referencefound;
    }

    /**
     * Traverses the tree for compareABCDtoCDM
     *
     * @param node
     * @param dataHolder
     */
    protected void traverse(Node node) {
        // Extract node info:
        String test = node.getTextContent();

        // Print and continue traversing.
        if (test != null && test != "#text" && node.getNodeName() != "#text"&& test.split("\n").length == 1 && test.length() > 0) {
            path = node.getNodeName();
            getHierarchie(node);
            dataHolder.allABCDelements.put(path, test);
            path = "";
        }
        // Now traverse the rest of the tree in depth-first order.
        if (node.hasChildNodes()) {
            // Get the children in a list.
            NodeList nl = node.getChildNodes();
            // How many of them?
            int size = nl.getLength();
            for (int i = 0; i < size; i++) {
                // Recursively traverse each of the children.
                traverse(nl.item(i));
            }
        }
    }



    /*
     * (non-Javadoc)
     *
     * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#
     * getSpecimenTypeDesignationStatusByKey(java.lang.String)
     */
    private SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(
            String key) {
        if (key == null) {
            return null;
        } else if (key.matches("(?i)(T|Type)")) {
            return SpecimenTypeDesignationStatus.TYPE();
        } else if (key.matches("(?i)(HT|Holotype)")) {
            return SpecimenTypeDesignationStatus.HOLOTYPE();
        } else if (key.matches("(?i)(LT|Lectotype)")) {
            return SpecimenTypeDesignationStatus.LECTOTYPE();
        } else if (key.matches("(?i)(NT|Neotype)")) {
            return SpecimenTypeDesignationStatus.NEOTYPE();
        } else if (key.matches("(?i)(ST|Syntype)")) {
            return SpecimenTypeDesignationStatus.SYNTYPE();
        } else if (key.matches("(?i)(ET|Epitype)")) {
            return SpecimenTypeDesignationStatus.EPITYPE();
        } else if (key.matches("(?i)(IT|Isotype)")) {
            return SpecimenTypeDesignationStatus.ISOTYPE();
        } else if (key.matches("(?i)(ILT|Isolectotype)")) {
            return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
        } else if (key.matches("(?i)(INT|Isoneotype)")) {
            return SpecimenTypeDesignationStatus.ISONEOTYPE();
        } else if (key.matches("(?i)(IET|Isoepitype)")) {
            return SpecimenTypeDesignationStatus.ISOEPITYPE();
        } else if (key.matches("(?i)(PT|Paratype)")) {
            return SpecimenTypeDesignationStatus.PARATYPE();
        } else if (key.matches("(?i)(PLT|Paralectotype)")) {
            return SpecimenTypeDesignationStatus.PARALECTOTYPE();
        } else if (key.matches("(?i)(PNT|Paraneotype)")) {
            return SpecimenTypeDesignationStatus.PARANEOTYPE();
        } else if (key.matches("(?i)(unsp.|Unspecified)")) {
            return SpecimenTypeDesignationStatus.UNSPECIFIC();
        } else if (key.matches("(?i)(2LT|Second Step Lectotype)")) {
            return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
        } else if (key.matches("(?i)(2NT|Second Step Neotype)")) {
            return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
        } else if (key.matches("(?i)(OM|Original Material)")) {
            return SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL();
        } else if (key.matches("(?i)(IcT|Iconotype)")) {
            return SpecimenTypeDesignationStatus.ICONOTYPE();
        } else if (key.matches("(?i)(PT|Phototype)")) {
            return SpecimenTypeDesignationStatus.PHOTOTYPE();
        } else if (key.matches("(?i)(IST|Isosyntype)")) {
            return SpecimenTypeDesignationStatus.ISOSYNTYPE();
        } else {
            return null;
        }
    }

    /**
     * getHierarchie : get the whole path
     * @param node
     */
    private void getHierarchie(Node node) {
        // logger.info("getHierarchie");
        while (node != null && node.getNodeName() != prefix + "DataSets" && node.getParentNode() != null) {
            // logger.info("nodeparent "+node.getParentNode().getNodeName());
            path = node.getParentNode().getNodeName() + "/" + path;
            node = node.getParentNode();
        }
        // logger.info("path gethierarchie: "+path);
    }


    /**
     * getIDs : get the source institution id, source id...
     * @param root
     */
    protected void getIDs(Element root) {
        NodeList group;
        try {
            group = root.getElementsByTagName(prefix + "SourceInstitutionID");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.institutionCode = group.item(0).getTextContent();
        } catch (NullPointerException e) {
            dataHolder.institutionCode = "";
        }
        try {
            group = root.getElementsByTagName(prefix + "SourceID");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.collectionCode = group.item(0).getTextContent();
        } catch (NullPointerException e) {
            dataHolder.collectionCode = "";
        }
        try {
            group = root.getElementsByTagName(prefix + "UnitID");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.unitID = group.item(0).getTextContent();
        } catch (NullPointerException e) {
            dataHolder.unitID = "";
        }
    }

    /**
     * getRecordBasis : extract the recordBasis out of the ABCD document
     * @param root
     */
    protected void getRecordBasis(Element root) {
        NodeList group;
        try {
            group = root.getElementsByTagName(prefix + "RecordBasis");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.recordBasis = group.item(0).getTextContent();
        } catch (NullPointerException e) {
            dataHolder.recordBasis = "";
        }
    }

    /**
     * getNumbers : getAccessionNumber, collector number ...
     * @param root
     */
    protected void getNumbers(Element root) {
        NodeList group;
        try {
            group = root.getElementsByTagName(prefix + "AccessionNumber");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.accessionNumber = group.item(0).getTextContent();
        } catch (NullPointerException e) {
            dataHolder.accessionNumber = "";
        }
        try {
            group = root.getElementsByTagName(prefix + "CollectorsFieldNumber");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.fieldNumber = group.item(0).getTextContent();
        } catch (NullPointerException e) {
            dataHolder.fieldNumber = "";
        }

        //try {
        //group = root.getElementsByTagName(prefix + "AccessionNumber");
        //path = group.item(0).getNodeName();
        //getHierarchie(group.item(0));
        //dataHolder.knownABCDelements.add(path);
        //path = "";
        //dataHolder.accessionNumber = group.item(0).getTextContent();
        //} catch (NullPointerException e) {
        //dataHolder.accessionNumber = "";
        //}
    }

    /**
     * getGeolocation : get locality
     * @param root
     */
    protected void getGeolocation(Element root) {
        NodeList group, childs;
        try {
            group = root.getElementsByTagName(prefix + "LocalityText");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.locality = group.item(0).getTextContent();
            if (group.item(0).hasAttributes()) {
                if (group.item(0).getAttributes().getNamedItem("lang") != null) {
                    dataHolder.languageIso = group.item(0).getAttributes().getNamedItem("lang").getTextContent();
                }
            }
        } catch (NullPointerException e) {
            dataHolder.locality = "";
        }
        try {
            group = root.getElementsByTagName(prefix + "LongitudeDecimal");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.longitude = Double.valueOf(group.item(0)
                    .getTextContent());
        } catch (NullPointerException e) {
            dataHolder.longitude = null;
        }
        try {
            group = root.getElementsByTagName(prefix + "LatitudeDecimal");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.latitude = Double
                    .valueOf(group.item(0).getTextContent());
        } catch (NullPointerException e) {
            dataHolder.latitude = null;
        }
        try {
            group = root.getElementsByTagName(prefix + "Country");
            childs = group.item(0).getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                if (childs.item(i).getNodeName().contains("Name")) {
                    path = childs.item(i).getNodeName();
                    getHierarchie(childs.item(i));
                    dataHolder.knownABCDelements.add(path);
                    path = "";
                    dataHolder.country = childs.item(i).getTextContent();
                }
            }
        } catch (NullPointerException e) {
            dataHolder.country = "";
        }
        try {
            group = root.getElementsByTagName(prefix + "Country");
            childs = group.item(0).getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                if (childs.item(i).getNodeName().contains("ISO3166Code")) {
                    path = childs.item(i).getNodeName();
                    getHierarchie(childs.item(i));
                    dataHolder.knownABCDelements.add(path);
                    path = "";
                    dataHolder.isocountry = childs.item(i).getTextContent();
                }
            }
        } catch (NullPointerException e) {
            dataHolder.isocountry = "";
        }
        try {
            group = root.getElementsByTagName(prefix + "Altitude");
            for (int i = 0; i < group.getLength(); i++) {
                childs = group.item(i).getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    if (childs.item(j).getNodeName().equals(prefix + "MeasurementOrFactText")) {
                        path = childs.item(j).getNodeName();
                        getHierarchie(childs.item(j));
                        dataHolder.knownABCDelements.add(path);
                        path = "";
                        dataHolder.altitude = Integer.valueOf(childs.item(j).getTextContent());
                    }
                }
            }
        } catch (NullPointerException e) {
            dataHolder.altitude = -9999;
        }

        try {
            group = root.getElementsByTagName(prefix + "Depth");
            path = group.item(0).getNodeName();
            getHierarchie(group.item(0));
            dataHolder.knownABCDelements.add(path);
            path = "";
            dataHolder.depth = Integer.valueOf(group.item(0).getTextContent());
        } catch (NullPointerException e) {
            dataHolder.depth = -9999;
        }

        try {
            group = root.getElementsByTagName(prefix + "NamedArea");
            dataHolder.namedAreaList = new ArrayList<String>();
            for (int i = 0; i < group.getLength(); i++) {
                childs = group.item(i).getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    if (childs.item(j).getNodeName() .equals(prefix + "AreaName")) {
                        path = childs.item(j).getNodeName();
                        getHierarchie(childs.item(j));
                        dataHolder.knownABCDelements.add(path);
                        path = "";
                        dataHolder.namedAreaList.add(childs.item(j).getTextContent());
                    }
                }
            }
        } catch (NullPointerException e) {
            dataHolder.namedAreaList = new ArrayList<String>();
        }
    }

    /**
     * getMultimedia: get the FileURI objects
     * @param root
     */
    protected void getMultimedia(Element root) {
        NodeList group, multimedias, multimedia;
        try {
            group = root.getElementsByTagName(prefix + "MultiMediaObjects");
            for (int i = 0; i < group.getLength(); i++) {
                multimedias = group.item(i).getChildNodes();
                for (int j = 0; j < multimedias.getLength(); j++) {
                    if (multimedias.item(j).getNodeName().equals(prefix + "MultiMediaObject")) {
                        multimedia = multimedias.item(j).getChildNodes();
                        for (int k = 0; k < multimedia.getLength(); k++) {
                            if (multimedia.item(k).getNodeName().equals(prefix + "FileURI")) {
                                dataHolder.multimediaObjects.add(multimedia.item(k).getTextContent());
                                path = multimedia.item(k).getNodeName();getHierarchie(multimedia.item(k));
                                dataHolder.knownABCDelements.add(path);path = "";
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            logger.info(e);
        }
    }

    /**
     * getGatheringPeople : get GatheringAgent with fullname
     * @param root
     */
    protected void getGatheringPeople(Element root) {
        NodeList group, childs, person;
        try {
            group = root.getElementsByTagName(prefix + "GatheringAgent");
            dataHolder.gatheringAgentList = new ArrayList<String>();
            for (int i = 0; i < group.getLength(); i++) {
                childs = group.item(i).getChildNodes();
                for (int j = 0; j < childs.getLength(); j++) {
                    if (childs.item(j).getNodeName().equals(prefix + "Person")) {
                        person = childs.item(j).getChildNodes();
                        for (int k = 0; k < person.getLength(); k++) {
                            if (person.item(k).getNodeName().equals(prefix + "FullName")) {
                                path = person.item(k).getNodeName();
                                getHierarchie(person.item(k));
                                dataHolder.knownABCDelements.add(path);
                                path = "";
                                if (!person.item(k).getTextContent().trim().equalsIgnoreCase("none")) {
                                    dataHolder.gatheringAgentList.add(person.item(k).getTextContent());
                                }
                            }
                        }
                    }

                }
            }
        } catch (NullPointerException e) {
            dataHolder.gatheringAgentList = new ArrayList<String>();
        }
        if (dataHolder.gatheringAgentList.size() > 1){
            dataHolder.gatheringTeamList.addAll(dataHolder.gatheringAgentList);
            dataHolder.gatheringAgentList = new ArrayList<String>();
        }
    }

    /*PARSING METHODS*/

    private HashMap<String, String> getAtomisedNames(String code,NodeList atomised) {
        logger.info("code getatomised " + code);
        if (code.indexOf("Botanical") != -1) {
            return this.getAtomisedBotanical(atomised);
        }
        if (code.indexOf("Bacterial") != -1) {
            return this.getAtomisedBacterial(atomised);
        }
        if (code.indexOf("Viral") != -1) {
            return this.getAtomisedViral(atomised);
        }
        if (code.indexOf("Zoological") != -1) {
            return this.getAtomisedZoological(atomised);
        }
        return new HashMap<String, String>();
    }

    private HashMap<String, String> getAtomisedZoological(NodeList atomised) {
        logger.info("getAtomisedZoo");
        HashMap<String, String> atomisedMap = new HashMap<String, String>();

        for (int i = 0; i < atomised.getLength(); i++) {
            if (atomised.item(i).getNodeName().equals(prefix + "GenusOrMonomial")) {
                atomisedMap.put("Genus", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "Subgenus")) {
                atomisedMap.put("Subgenus", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "SpeciesEpithet")) {
                atomisedMap.put("SpeciesEpithet", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "SubspeciesEpithet")) {
                atomisedMap.put("SubspeciesEpithet", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "AuthorTeamOriginalAndYear")) {
                atomisedMap.put("AuthorTeamOriginalAndYear", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "AuthorTeamParenthesisAndYear")) {
                atomisedMap.put("AuthorTeamParenthesisAndYear", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "CombinationAuthorTeamAndYear")) {
                atomisedMap.put("CombinationAuthorTeamAndYear", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "Breed")) {
                atomisedMap.put("Breed", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "NamedIndividual")) {
                atomisedMap.put("NamedIndividual", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
        }
        return atomisedMap;
    }

    private HashMap<String, String> getAtomisedViral(NodeList atomised) {
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        for (int i = 0; i < atomised.getLength(); i++) {
            if (atomised.item(i).getNodeName().equals(prefix + "GenusOrMonomial")) {
                atomisedMap.put("Genus", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "ViralSpeciesDesignation")) {
                atomisedMap.put("ViralSpeciesDesignation", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "Acronym")) {
                atomisedMap.put("Acronym", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
        }
        return atomisedMap;
    }

    private HashMap<String, String> getAtomisedBotanical(NodeList atomised) {
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        for (int i = 0; i < atomised.getLength(); i++) {
            if (atomised.item(i).getNodeName().equals(prefix + "GenusOrMonomial")) {
                atomisedMap.put("Genus", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "FirstEpithet")) {
                atomisedMap.put("FirstEpithet", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName()
                    .equals(prefix + "InfraspecificEpithet")) {
                atomisedMap.put("InfraSpeEpithet", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "Rank")) {
                atomisedMap.put("Rank", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "HybridFlag")) {
                atomisedMap.put("HybridFlag", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName()
                    .equals(prefix + "AuthorTeamParenthesis")) {
                atomisedMap.put("AuthorTeamParenthesis", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "AuthorTeam")) {
                atomisedMap.put("AuthorTeam", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "CultivarGroupName")) {
                atomisedMap.put("CultivarGroupName", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "CultivarName")) {
                atomisedMap.put("CultivarName", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "TradeDesignationNames")) {
                atomisedMap.put("Trade", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
        }
        return atomisedMap;
    }

    private HashMap<String, String> getAtomisedBacterial(NodeList atomised) {
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        for (int i = 0; i < atomised.getLength(); i++) {
            if (atomised.item(i).getNodeName().equals(prefix + "GenusOrMonomial")) {
                atomisedMap.put("Genus", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "Subgenus")) {
                atomisedMap.put("SubGenus", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "SubgenusAuthorAndYear")) {
                atomisedMap.put("SubgenusAuthorAndYear", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "SpeciesEpithet")) {
                atomisedMap.put("SpeciesEpithet", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "SubspeciesEpithet")) {
                atomisedMap.put("SubspeciesEpithet", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "ParentheticalAuthorTeamAndYear")) {
                atomisedMap.put("ParentheticalAuthorTeamAndYear", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "AuthorTeamAndYear")) {
                atomisedMap.put("AuthorTeamAndYear", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
            if (atomised.item(i).getNodeName().equals(prefix + "NameApprobation")) {
                atomisedMap.put("NameApprobation", atomised.item(i).getTextContent());
                path = atomised.item(i).getNodeName();
                getHierarchie(atomised.item(i));
                dataHolder.knownABCDelements.add(path);
                path = "";
            }
        }
        return atomisedMap;
    }

}
