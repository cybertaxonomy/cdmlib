package eu.etaxonomy.cdm.remote.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * @author f.revilla
 * @date 09.06.2010
 */
public class UuidList extends ArrayList<UUID> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Set<UUID> asSet() {
        HashSet<UUID> tmpSet = new HashSet<UUID>(this.size());
        tmpSet.addAll(this);
        return tmpSet;
    }

}
