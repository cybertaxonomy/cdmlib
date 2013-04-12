// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.Scanner;

import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXExtractor {

    protected final static String SPLITTER = ",";

    protected  int askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        int index = scan.nextInt();
        return index;
    }


    /**
     * @param reftype
     * @return
     */
    protected Reference<?> getReferenceType(int reftype) {
        Reference<?> ref = null;
        switch (reftype) {
        case 1:
            ref = ReferenceFactory.newGeneric();
            break;
        case 2:
            IBook tmp= ReferenceFactory.newBook();
            ref = (Reference<?>)tmp;
            break;
        case 3:
            ref = ReferenceFactory.newArticle();
            break;
        case 4:
            IBookSection tmp2 = ReferenceFactory.newBookSection();
            ref = (Reference<?>)tmp2;
            break;
        case 5:
            ref = ReferenceFactory.newJournal();
            break;
        case 6:
            ref = ReferenceFactory.newPrintSeries();
            break;
        case 7:
            ref = ReferenceFactory.newThesis();
            break;
        default:
            break;
        }
        return ref;
    }
}
