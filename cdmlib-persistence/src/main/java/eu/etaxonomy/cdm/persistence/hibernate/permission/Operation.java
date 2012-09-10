/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.EnumSet;
import java.util.Iterator;

import com.sun.xml.internal.ws.api.pipe.NextAction;


/**
 * @author k.luther
 * @date 06.07.2011
 *
 */
public enum Operation {
    CREATE, READ, UPDATE, DELETE;

    final static public EnumSet<Operation> ALL = EnumSet.allOf(Operation.class);

    final static public EnumSet<Operation> ADMIN = ALL; // FIXME remove

    final static public EnumSet<Operation> NONE = EnumSet.noneOf(Operation.class);

    public static EnumSet<Operation> fromString(String string){
        if(string.equals("ALL")){
            return ALL;
        }
        if(string.equals("ADMIN")){
            return ADMIN;
        }
        if(string.equals("NONE")){
            return NONE;
        }
        return EnumSet.of(Operation.valueOf(string));
    }

    public EnumSet<Operation> asEnumSet(){
        return EnumSet.of(this);
    }

    public static String namesOf(EnumSet<Operation> operation){
        StringBuilder names = new StringBuilder();

        for(Iterator<Operation> it = operation.iterator(); it.hasNext();){
            if(names.length() > 0){
                names.append(", ");
            }
            names.append(it.next().name());
        }
        return names.toString();
    }
}


