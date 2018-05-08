/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;


/**
 * The external link type is used to define the type of an {@link ExternalLink external link}.<BR>
 *
 * @author a.mueller
 * @created 09.06.2017
 */
@XmlEnum
public enum ExternalLinkType implements IEnumTerm<ExternalLinkType>{

    //0
    /**
     * Unknown link type is the type to be used if no information is available about the type.
     */
    @XmlEnumValue("Unknown")
    Unknown(UUID.fromString("270c0fa7-8c11-4a66-a0d7-deb6592e7f40"), "Unknown Link Type","UNK", null),

    //1
    /**
     * Link type to represent a web site.
     */
    @XmlEnumValue("Language")
    WebSite(UUID.fromString("40f128af-b953-4943-af91-9b2f6b030546"), "WebSite", "WS", null),

    //2
    /**
     * Link type to represent a file.
     */
    @XmlEnumValue("File")
    File(UUID.fromString("6e7ee084-d8a1-4813-b45a-218414c8795b"), "File", "FI", null),

    ;

// **************** END ENUM **********************/

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ExternalLinkType.class);

    private ExternalLinkType(UUID uuid, String defaultString, String key){
        this(uuid, defaultString, key, null);
    }

    private ExternalLinkType(UUID uuid, String defaultString, String key, ExternalLinkType parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }


// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<ExternalLinkType> delegateVoc;
    private IEnumTerm<ExternalLinkType> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(ExternalLinkType.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

    @Override
    public String getMessage(eu.etaxonomy.cdm.model.common.Language language){return delegateVocTerm.getMessage(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public ExternalLinkType getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<ExternalLinkType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(ExternalLinkType ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

    @Override
    public Set<ExternalLinkType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static ExternalLinkType getByKey(String key){return delegateVoc.getByKey(key);}
    public static ExternalLinkType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}



}
