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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.mueller
 * @since 26.05.2020
 */
public enum TaxonNodeStatus implements IEnumTerm<TaxonNodeStatus>{

    //0  - see #8281
    /**
     * The placement of the taxon is doubtful or preliminary.
     */
    @XmlEnumValue("Doubtful")
    DOUBTFUL(UUID.fromString("022ebae2-a020-4a8d-8ee1-886d98d3a4db"), "Doubtful", "DOU", "?", null),

    //1
    /**
     * The taxon is not placed to the correct place (yet). Instead it is  placed here.
     */
    @XmlEnumValue("Unplaced")
    UNPLACED(UUID.fromString("92809dee-8b3f-4fd5-a915-638d7c86b351"), "Unplaced", "UNP", "??", null),

    //2
    /**
     * The taxon for some reason is excluded from the treatment this {@link TaxonNode} belongs too.
     */
    @XmlEnumValue("Excluded")
    EXCLUDED(UUID.fromString("23d259b6-2d7e-4df6-8745-0e24fbe63187"), "Excluded", "EXC", Character.toString((char)248), null)
    ;

// **************** END ENUM **********************/

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonNodeStatus.class);

    private String symbol;

    private TaxonNodeStatus(UUID uuid, String defaultString, String key, String symbol){
        this(uuid, defaultString, key, symbol, null);
    }

    private TaxonNodeStatus(UUID uuid, String defaultString, String key, String symbol, TaxonNodeStatus parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
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
