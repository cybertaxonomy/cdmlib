// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import eu.etaxonomy.cdm.model.molecular.SingleRead;

/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class SingleReadFormatter extends AbstractCdmFormatter {

    public SingleReadFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
        SingleRead singleRead = (SingleRead)object;
        if(singleRead.getPrimer()!=null){
            formatKeyMap.put(FormatKey.SINGLE_READ_PRIMER, singleRead.getPrimer().getLabel());
        }
        if(singleRead.getAmplificationResult()!=null &&
                singleRead.getAmplificationResult().getAmplification()!=null){
            formatKeyMap.put(FormatKey.AMPLIFICATION_LABEL, singleRead.getAmplificationResult().getAmplification().getLabelCache());
        }
    }

}
