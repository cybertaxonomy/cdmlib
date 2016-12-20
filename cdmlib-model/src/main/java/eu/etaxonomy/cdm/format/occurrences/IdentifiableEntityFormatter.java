/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;


/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class IdentifiableEntityFormatter extends AbstractCdmFormatter {

    public IdentifiableEntityFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
//        IdentifiableEntity identifiableEntity = (IdentifiableEntity)object;
//        List<Identifier> identifiers = identifiableEntity.getIdentifiers();
//        String identifierString = null;
//        for (Identifier identifier : identifiers) {
//            if(identifier.getType()!=null && identifier.getType().equals(DerivateLabelProvider.getSampleDesignationTerm())){
//                //first sample designation is the current
//                identifierString = identifier.toString();
//            }
//        }
//        if(identifierString!=null){
//            formatKeyMap.put(FormatKey.FIELD_NUMBER, identifierString);
//        }
    }

}
