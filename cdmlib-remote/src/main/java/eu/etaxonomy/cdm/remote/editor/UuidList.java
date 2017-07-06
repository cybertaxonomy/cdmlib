package eu.etaxonomy.cdm.remote.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.CdmUtils;


/**
 * @author f.revilla
 * @date 09.06.2010
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
        String result = null;
        for (UUID uuid : this){
            result = CdmUtils.concat(",", uuid.toString());
        }
        return (result == null)? super.toString():result;
    }



}
