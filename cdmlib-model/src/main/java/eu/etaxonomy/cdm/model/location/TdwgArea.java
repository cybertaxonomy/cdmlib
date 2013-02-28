/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * Area terms according to the TDWG GeographicRegion vocabulary
 * http://rs.tdwg.org/ontology/voc/GeographicRegion.rdf
 *
 * @author a.mueller
 * @created 15.07.2008
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TdwgArea")
@XmlRootElement(name = "TdwgArea")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class TdwgArea extends NamedArea implements Cloneable{
    private static final long serialVersionUID = 4662215686356109015L;
    private static final Logger logger = Logger.getLogger(TdwgArea.class);

    public static final UUID uuidTdwgAreaVocabulary = UUID.fromString("1fb40504-d1d7-44b0-9731-374fbe6cac77");


    private static Map<String, UUID> abbrevMap = null;
    private static Map<String, UUID> labelMap = null;

    protected static Map<UUID, TdwgArea> termMap = null;



//************************** METHODS ********************************

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
     */
    @Override
    public void resetTerms(){
        termMap = null;
        labelMap = null;
        abbrevMap = null;
    }


    protected static TdwgArea getTermByUuid(UUID uuid){
        if (termMap == null){
            DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
            vocabularyStore.initialize();
        }
        return (TdwgArea)termMap.get(uuid);
    }

    /**
     * FIXME This class should really be refactored into an interface and service implementation,
     * relying on TermVocabularyDao / service (Ben)
     * @param tdwgAbbreviation
     * @return
     */
    public static NamedArea getAreaByTdwgAbbreviation(String tdwgAbbreviation){
        if (abbrevMap == null){
            initMaps();
        }
        UUID uuid = abbrevMap.get(tdwgAbbreviation);
        if (uuid == null){
            logger.info("Unknown TDWG area: " + CdmUtils.Nz(tdwgAbbreviation));
            return null;
        }
        return TdwgArea.getTermByUuid(uuid);
    }

    /**
     * FIXME This class should really be refactored into an interface and service implementation,
     * relying on TermVocabularyDao / service (Ben)
     * @param tdwgLabel
     * @return
     */
    public static NamedArea getAreaByTdwgLabel(String tdwgLabel){
        if (labelMap == null){
            initMaps();
        }
        tdwgLabel = tdwgLabel.toLowerCase();
        UUID uuid = labelMap.get(tdwgLabel);
        if (uuid == null){
            logger.info("Unknown TDWG area: " + CdmUtils.Nz(tdwgLabel));
            return null;
        }
        return TdwgArea.getTermByUuid(uuid);
    }

    public static boolean isTdwgAreaLabel(String label){
        label = (label == null? null : label.toLowerCase());
        if (labelMap.containsKey(label)){
            return true;
        }else{
            return false;
        }
    }

    public static boolean isTdwgAreaAbbreviation(String abbrev){
        if (abbrevMap.containsKey(abbrev)){
            return true;
        }else{
            return false;
        }
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.location.NamedArea#setDefaultTerms(eu.etaxonomy.cdm.model.common.TermVocabulary)
     */
//	@Override
//	protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
//		Set<NamedArea> terms = termVocabulary.getTerms();
//		for (NamedArea term : terms){
//			addTdwgArea(term);
//		}
//	}

    @Override
    protected void setDefaultTerms(TermVocabulary<NamedArea> termVocabulary) {
        termMap = new HashMap<UUID, TdwgArea>();
        for (NamedArea term : termVocabulary.getTerms()){
            termMap.put(term.getUuid(), (TdwgArea)term);  //TODO casting
            addTdwgArea(term);
        }
    }


    protected static void addTdwgArea(NamedArea area){
        if (area == null){
            logger.warn("tdwg area is null");
            return;
        }
        Language lang = Language.DEFAULT();
        Representation representation = area.getRepresentation(lang);
        String tdwgAbbrevLabel = representation.getAbbreviatedLabel();
        String tdwgLabel = representation.getLabel().toLowerCase();
        if (tdwgAbbrevLabel == null){
            logger.warn("tdwgLabel = null");
            return;
        }
        //init map
        if (abbrevMap == null){
            abbrevMap = new HashMap<String, UUID>();
        }
        if (labelMap == null){
            labelMap = new HashMap<String, UUID>();
        }
        //add to map
        abbrevMap.put(tdwgAbbrevLabel, area.getUuid());
        labelMap.put(tdwgLabel, area.getUuid());
        //add type
        area.setType(NamedAreaType.ADMINISTRATION_AREA());
        //add level
        if (tdwgAbbrevLabel.trim().length()== 1){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL1());
        }else if (tdwgAbbrevLabel.trim().length()== 2){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL2());
        }else if (tdwgAbbrevLabel.trim().length()== 3){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL3());
        }else if (tdwgAbbrevLabel.trim().length()== 6){
            area.setLevel(NamedAreaLevel.TDWG_LEVEL4());
        }else {
            logger.warn("Unknown TDWG Level " + tdwgAbbrevLabel + "! Unvalid string length (" +  tdwgAbbrevLabel.length() +")");
        }
    }

    private static void initMaps(){
        labelMap = new HashMap<String, UUID>();
        abbrevMap = new HashMap<String, UUID>();
    }


//********************* OLD ******************************/


    private static NamedArea getNamedAreaByTdwgLabel(String tdwgLabel){
        if (tdwgLabel == null){
            return null;
        }
        InputStream file;
        try {
            file = CdmUtils.getReadableResourceStream("");
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
            return null;
        }
        Element root = XmlHelp.getRoot(file, "RDF");
        Namespace nsRdf = root.getNamespace("rdf");
        XmlHelp.getFirstAttributedChild(root, "", "ID", tdwgLabel.trim());

        //Filter filter = ;
        //root.getDescendants(filter);
        return null;
    }

}
