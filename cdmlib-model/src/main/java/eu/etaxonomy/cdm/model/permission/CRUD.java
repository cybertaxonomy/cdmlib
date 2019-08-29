/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.kohlbecker
 */
public enum CRUD  implements IEnumTerm<CRUD>{
    CREATE("Create", "C"),
    READ("Read", "R"),
    UPDATE("Update", "U"),
    DELETE("Delete", "D");

    private CRUD(String defaultString, String key){
        this(UUID.randomUUID(), defaultString, key, null);
    }

    private CRUD(UUID uuid, String defaultString, String key, CRUD parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<CRUD> delegateVoc;
    private IEnumTerm<CRUD> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(CRUD.class);
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
    public CRUD getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<CRUD> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(CRUD ancestor) {return delegateVocTerm.isKindOf(ancestor);  }

    @Override
    public Set<CRUD> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static CRUD getByKey(String key){return delegateVoc.getByKey(key);}
    public static CRUD getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
