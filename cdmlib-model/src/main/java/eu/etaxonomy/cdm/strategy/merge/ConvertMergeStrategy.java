/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.merge;

import java.lang.reflect.Field;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * @author k.luther
 * @date 27.08.2015
 *
 */
public class ConvertMergeStrategy extends DefaultMergeStrategy {

   private static final long serialVersionUID = -1046274562281576696L;

   private boolean deleteSecondObject = false;
    /**
     * @param mergeClazz
     */
    protected ConvertMergeStrategy(Class<? extends CdmBase> mergeClazz) {
        super(mergeClazz);

    }

    public static ConvertMergeStrategy NewInstance(Class<? extends CdmBase> mergeClazz){
        return new ConvertMergeStrategy(mergeClazz);
    }

    public boolean isDeleteSecondObject() {
        return deleteSecondObject;
    }

    public void setDeleteSecondObject(boolean deleteSecondObject) {
        this.deleteSecondObject = deleteSecondObject;
    }

    @Override
    protected <T extends IMergable> void mergeCollectionFieldNoFirst(T mergeFirst, T mergeSecond, Field field, MergeMode mergeMode, Set<ICdmBase> deleteSet, Set<ICdmBase> clonedObjects) throws Exception{
        super.mergeCollectionFieldNoFirst(mergeFirst, mergeSecond, field, mergeMode, deleteSet, clonedObjects);
    }

}
