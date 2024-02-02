/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author f.revilla
 * @since 09.06.2010
 */
public class UuidList extends ArrayList<UUID> {

    private static final long serialVersionUID = -7844234502924643928L;

    public Set<UUID> asSet() {
        HashSet<UUID> tmpSet = new HashSet<>(this.size());
        tmpSet.addAll(this);
        return tmpSet;
    }

    @Override
    public String toString() {
        String result = "";
        for (UUID uuid : this){
            if(uuid != null){
                result +=  (result.isEmpty() ? "": ",") + uuid.toString();
            } else {
                result += (result.isEmpty() ? "": ",") + "NULL";
            }
        }
        return "[" + result + "]";
    }
}