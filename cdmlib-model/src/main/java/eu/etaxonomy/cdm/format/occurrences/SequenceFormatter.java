/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import eu.etaxonomy.cdm.model.molecular.Sequence;

/**
 * @author pplitzner
 * @since Nov 30, 2015
 *
 */
public class SequenceFormatter extends AbstractCdmFormatter {

    public SequenceFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
        Sequence sequence = (Sequence)object;
        if(sequence.getDnaMarker()!=null){
            formatKeyMap.put(FormatKey.SEQUENCE_DNA_MARKER, sequence.getDnaMarker().getLabel());
        }
    }

}
