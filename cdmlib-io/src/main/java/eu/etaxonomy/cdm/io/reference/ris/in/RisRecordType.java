/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 \* @since 12.05.2017
 *
 */
public enum RisRecordType {
    AGGR("Aggregated Database" , ReferenceType.Database ),
    ANCIENT("Ancient Text"  ),
    ART("Art Work"  ),
    BILL("Bill"  ),
    BLOG("Blog"  ),
    BOOK("Whole book" , ReferenceType.Book ),
    CASE("Case"  ),
    CHAP("Book chapter" , ReferenceType.BookSection ),
    CHART("Chart"  ),
    CLSWK("Classical Work"  ),
    COMP("Computer program"  ),
    CONF("Conference proceeding" , ReferenceType.InProceedings ),
    CPAPER("Conference paper"  ),
    CTLG("Catalog"  ),
    DATA("Data file"  ),
    DBASE("Online Database" , ReferenceType.Database ),
    DICT("Dictionary"  ),
    EBOOK("Electronic Book"  ),
    ECHAP("Electronic Book Section"  ),
    EDBOOK("Edited Book" , ReferenceType.Book ),
    EJOUR("Electronic Article"  ),
    ELEC("Web Page" , ReferenceType.WebPage ),
    ENCYC("Encyclopedia"  ),
    EQUA("Equation"  ),
    FIGURE("Figure"  ),
    GEN("Generic" , ReferenceType.Generic ),
    GOVDOC("Government Document"  ),
    GRANT("Grant"  ),
    HEAR("Hearing"  ),
    ICOMM("Internet Communication"  ),
    INPR("In Press"  ),
    JFULL("Journal (full)" , ReferenceType.Journal ),
    JOUR("Journal" , ReferenceType.Article ),
    LEGAL("Legal Rule or Regulation"  ),
    MANSCPT("Manuscript"  ),
    MAP("Map" , ReferenceType.Map ),
    MGZN("Magazine article" , ReferenceType.Article ),
    MPCT("Motion picture"  ),
    MULTI("Online Multimedia"  ),
    MUSIC("Music score"  ),
    NEWS("Newspaper"  ),
    PAMP("Pamphlet"  ),
    PAT("Patent"  ),
    PCOMM("Personal communication" , ReferenceType.PersonalCommunication ),
    RPRT("Report" , ReferenceType.Report ),
    SER("Serial publication" , ReferenceType.PrintSeries ),
    SLIDE("Slide"  ),
    SOUND("Sound recording"  ),
    STAND("Standard"  ),
    STAT("Statute"  ),
    THES("Thesis/Dissertation" , ReferenceType.Thesis ),
    UNPB("Unpublished work"  ),
    VIDEO("Video recording"  ),


    ;

    private String description;
    private ReferenceType refType;

    private RisRecordType(String description){
        this.description = description;
    }

    private RisRecordType(String description, ReferenceType refType){
        this.description = description;
        this.refType = refType;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return
     */
    public ReferenceType getCdmReferenceType() {
        return refType;
    }
}
