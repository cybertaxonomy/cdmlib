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
import java.util.StringTokenizer;


/**
 * @author k.luther
 * @date 06.07.2011
 *
 */
public class Operation {

    private Operation(){

    }

    final static public EnumSet<CRUD> CREATE = EnumSet.of(CRUD.CREATE);

    final static public EnumSet<CRUD> READ = EnumSet.of(CRUD.READ);

    final static public EnumSet<CRUD> UPDATE = EnumSet.of(CRUD.UPDATE);

    final static public EnumSet<CRUD> DELETE = EnumSet.of(CRUD.DELETE);

    final static public EnumSet<CRUD> ALL = EnumSet.allOf(CRUD.class);

    final static public EnumSet<CRUD> ADMIN = ALL; // FIXME remove?

    final static public EnumSet<CRUD> NONE = EnumSet.noneOf(CRUD.class);

    public static EnumSet<CRUD> fromString(String string){
        if(string.equals("ALL")){
            return ALL;
        }
        if(string.equals("ADMIN")){
            return ADMIN;
        }
        if(string.equals("NONE")){
            return NONE;
        }
		StringTokenizer st = new StringTokenizer(string,",");		 
		EnumSet<CRUD> op = EnumSet.noneOf(CRUD.class);

		while (st.hasMoreElements()) {

			String opStr = (String) st.nextElement();			
			op.add(CRUD.valueOf(opStr.trim()));
		}

		return op;
    }

    public static boolean isOperation(Object o){
        try {
        return o instanceof EnumSet<?> && ALL.containsAll((EnumSet<?>)o);
        } catch (Throwable e){
            return false;
        }
    }

}


