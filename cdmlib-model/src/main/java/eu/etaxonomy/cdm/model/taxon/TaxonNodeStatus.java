/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * The status for the placement (taxon node) of taxon/name in classification.
 *
 * For more information on the status see #10096, #9005, #8281
 *
 * @author a.mueller
 * @since 26.05.2020
 */
public enum TaxonNodeStatus implements IEnumTerm<TaxonNodeStatus>{

    //TODO maybe we want a status "Included" too, instead of making TaxonNode.status an optional
    //     attribute (see comments in #10096 attachement)
    //0  - see #810096
//    /**
//     * The placement of the taxon is doubtful or preliminary.
//     */
//    @XmlEnumValue("Included")
//    INCLDUDED(UUID.fromString("7601cc89-8896-4b85-a83e-91700fe45a1d"), "Included (default)","Taxon included in parent taxon (default)", "INC", "", null),

    //1  - see #8281
    /**
     * The placement of the taxon is doubtful or preliminary.
     */
    @XmlEnumValue("Doubtful")
    DOUBTFUL(UUID.fromString("022ebae2-a020-4a8d-8ee1-886d98d3a4db"), "Doubtful", "Taxon incl. in parent taxon with doubts", "DOU", "?", null),

    //2
    /**
     * The taxon is not placed to the correct place (yet). Instead it is  placed here.
     */
    @XmlEnumValue("Unplaced")
    UNPLACED(UUID.fromString("92809dee-8b3f-4fd5-a915-638d7c86b351"), "Unplaced", "Taxon unplaced", "UNP", "??", null),

    //3
    /**
     * The taxon or name for any reason is excluded from the treatment this {@link TaxonNode} belongs too.
     * See sub status for more specific reasons.
     */
    @XmlEnumValue("Excluded")
    EXCLUDED(UUID.fromString("23d259b6-2d7e-4df6-8745-0e24fbe63187"), "Excluded", "Name/taxon excluded (unspecific)", "EXC", Character.toString((char)248), null),

    //4
    /**
     * Taxon or name excluded, geographically out of scope. <BR>
     * E.g. a taxon that does not occur in the region of the flora treatment.
     */
    @XmlEnumValue("Excluded_geo")
    EXCLUDED_GEO(UUID.fromString("a76c0fa8-e04d-421c-ac10-07a3c6770d45"), "Excluded geo.", "Taxon excl. (geographically out of scope)", "EXCG", Character.toString((char)248)+"g", EXCLUDED),

    //5
    /**
     * Taxon or name excluded, taxonomically out of scope. <BR>
     * E.g. name being a taxon name or synonym belonging to a taxon that is not part of the treatment.
     */
    @XmlEnumValue("Excluded_tax")
    EXCLUDED_TAX(UUID.fromString("689a5821-e59d-4ec2-ae33-2331bdb39f34"),"Excluded tax.", "Taxon/name excl. (taxonomically out of sc.)", "EXCT", Character.toString((char)248)+"t", EXCLUDED),

    //6
    /**
     * Name excluded for nomenclatural reasons. <BR>
     * E.g. an effectively published name ascribing the name to an author who merely and correctly cited an earlier name.
     */
    @XmlEnumValue("Excluded_nom")
    EXCLUDED_NOM(UUID.fromString("b4484183-6f19-4901-af96-0ab6183cebfb"), "Excluded nom.","Name excl. (for nomenclatural reasons)", "EXCN", Character.toString((char)248)+"n", EXCLUDED),

    //7
    /**
     * Name of verified uncertain application <BR>
     * E.g. an effectively published name ascribing the name to an author who merely and correctly cited an earlier name.<BR>
     * Alternative symbol: ↑ or u+2BD1 (https://unicode-table.com/en/2BD1/)
     */
    @XmlEnumValue("Uncertain_app")
    UNCERTAIN_APPLICATION(UUID.fromString("c87ea64a-f3d3-41fe-a4c5-dd6a8697fc46"), "Uncertain application", "Name of verified uncertain application", "UNA", Character.toString((char)248)+"a", EXCLUDED),

    //8
    /**
     * Name of verified uncertain application <BR>
     * E.g. an effectively published name ascribing the name to an author who merely and correctly cited an earlier name.<BR>
     * Alternative symbol: ↑ or u+2BD1 (https://unicode-table.com/en/2BD1/)
     */
    @XmlEnumValue("Unresolved")
    UNRESOLVED(UUID.fromString("ce6f2430-9662-4b78-8fc2-48b5fa9fd37e"), "Unresolved", "Unresolved name – to be further revised", "UNR", "u", null),

;

// **************** END ENUM **********************/

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(TaxonNodeStatus.class);

    private String symbol;

    private TaxonNodeStatus(UUID uuid, String label, String key, String symbol){
        this(uuid, label, label, key, symbol, null);
    }

    private TaxonNodeStatus(UUID uuid, String label, String longLabel, String key, String symbol, TaxonNodeStatus parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, longLabel, key, parent);
        this.symbol = symbol;
    }


// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<TaxonNodeStatus> delegateVoc;
    private IEnumTerm<TaxonNodeStatus> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(TaxonNodeStatus.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getLabel(){return delegateVocTerm.getLabel();}

    @Override
    public String getLabel(Language language){return delegateVocTerm.getLabel(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public TaxonNodeStatus getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<TaxonNodeStatus> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(TaxonNodeStatus ancestor) {return delegateVocTerm.isKindOf(ancestor);  }

    @Override
    public Set<TaxonNodeStatus> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static TaxonNodeStatus getByKey(String key){return delegateVoc.getByKey(key);}
    public static TaxonNodeStatus getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

//**************** symbol **********************/

    public String getSymbol(){
        return symbol;
    }
}
