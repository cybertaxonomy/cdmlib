/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

/**
 * Utility class to handle {@link IntextReference}s
 *
 * @author a.mueller
 * @since 10.03.2017
 *
 */
public final class IntextReferenceHelper {

    public static IntextReference addIntextReference(IIntextReferenceTarget target, IIntextReferencable referencedEntity, String start, String inner, String end){
        IntextReference intextReference = IntextReference.NewInstance(target, referencedEntity, 0, 0);
        referencedEntity.setText(start + intextReference.toInlineString(inner) + end);
        referencedEntity.getIntextReferences().add(intextReference);
        return intextReference;
    }
    public static IntextReference addIntextReference(IIntextReferenceTarget target, IIntextReferencable referencedEntity, int start, int end){
        String text = referencedEntity.getText();
        if (start < 0 || end < 0 || start > end || end > text.length()){
            throw new IndexOutOfBoundsException("Start and end must be within bounds");
        }
        IntextReference intextReference = IntextReference.NewInstance(target, referencedEntity, 0, 0);
        referencedEntity.setText(text.substring(0, start) + intextReference.toInlineString(text.substring(start,end))
            + text.substring(end));
        referencedEntity.getIntextReferences().add(intextReference);
        return intextReference;
    }
}
