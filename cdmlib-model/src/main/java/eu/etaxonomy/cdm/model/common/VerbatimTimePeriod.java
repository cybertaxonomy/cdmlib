/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;

import eu.etaxonomy.cdm.format.common.VerbatimTimePeriodFormatter;

/**
 * @author a.mueller
 * @since 08.05.2018
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerbatimTimePeriod", propOrder = {
    "verbatimDate"
})
@XmlRootElement(name = "VerbatimTimePeriod")
@Embeddable
public class VerbatimTimePeriod extends TimePeriod {

    private static final long serialVersionUID = -6543644293635460526L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(VerbatimTimePeriod.class);

    private static VerbatimTimePeriodFormatter formatter = VerbatimTimePeriodFormatter.NewDefaultInstance();

    @XmlElement(name = "FreeText")
    private String verbatimDate;

 // ********************** FACTORY METHODS **************************/

     public static final VerbatimTimePeriod NewVerbatimInstance(){
         return new VerbatimTimePeriod();
     }

     /**
      * Factory method a date representing the current date and time
      */
     public static final VerbatimTimePeriod NewVerbatimNowInstance(){
         return NewVerbatimInstance(Calendar.getInstance());
     }

     public static final VerbatimTimePeriod NewVerbatimInstance(Partial startDate){
         return new VerbatimTimePeriod(startDate, null, null);
     }

     public static final VerbatimTimePeriod NewVerbatimInstance(Partial startDate, Partial endDate){
         return new VerbatimTimePeriod(startDate, endDate, null);
     }

     public static final VerbatimTimePeriod NewVerbatimInstance(Partial startDate, Partial endDate, String verbatimDate){
         return new VerbatimTimePeriod(startDate, endDate, verbatimDate);
     }

     public static final VerbatimTimePeriod NewVerbatimInstance(Integer year){
         return NewVerbatimInstance(year, (Integer)null);
     }

     public static final VerbatimTimePeriod NewVerbatimInstance(Integer startYear, Integer endYear){
         return new VerbatimTimePeriod(yearToPartial(startYear), yearToPartial(endYear), null);
     }

     /**
      * Factory method to create a TimePeriod from a <code>Calendar</code>. The Calendar is stored as the starting instant.
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Calendar startCalendar){
         return NewVerbatimInstance(startCalendar, null);
     }

     /**
      * Factory method to create a TimePeriod from a <code>ReadableInstant</code>(e.g. <code>DateTime</code>).
      * The <code>ReadableInstant</code> is stored as the starting instant.
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(ReadableInstant readableInstant){
         return NewVerbatimInstance(readableInstant, null);
     }

     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>Calendar</code>
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Calendar startCalendar, Calendar endCalendar){
         return new VerbatimTimePeriod(calendarToPartial(startCalendar), calendarToPartial(endCalendar), null);
     }

     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>Date</code>
      * @return VerbatimTimePeriod
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Date startDate, Date endDate){
         return NewVerbatimInstance(dateToPartial(startDate), dateToPartial(endDate));
     }

     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>ReadableInstant</code>(e.g. <code>DateTime</code>)
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(ReadableInstant startInstant, ReadableInstant endInstant){
         return new VerbatimTimePeriod(readableInstantToPartial(startInstant), readableInstantToPartial(endInstant), null);
     }

//*********************** CONSTRUCTOR *********************************/

    protected VerbatimTimePeriod() {
        super();
    }
    private VerbatimTimePeriod(Partial startDate, Partial endDate, String verbatimDate) {
        super(startDate, endDate, null);
        this.verbatimDate = verbatimDate;
    }

// ***************************** GETTER /SETTER *********************/

    public String getVerbatimDate() {
        return verbatimDate;
    }
    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

// ************************************ TRANSIENT **************************/

    @Override
    /**
     * True, if there is no start date, no end date, no freetext representation
     * and no verbatimDate.
     * @return
     */
    @Transient
    public boolean isEmpty(){
        boolean result = super.isEmpty();
        return result && isBlank(this.getVerbatimDate());
    }

//*********** EQUALS **********************************/

    //we want VerbatimTimePeriod and TimePeriod to be equals
    //if both are equal in the TimePeriod part and if
    //VerbatimTimePeriod has no verbatimDate defined

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode += (verbatimDate == null)? 0: verbatimDate.hashCode();
        return hashCode;
    }


//**************************** to String ****************************************

    /**
     * Returns the {@link #getFreeText()} value if free text is not <code>null</code>.
     * Otherwise the concatenation of <code>start</code> and <code>end</code> is returned.
     *
     * @see java.lang.Object#toString()
     */
      @Override
      public String toString(){
         return formatter.format(this);
    }

//*********** CLONE **********************************/

    @Override
    public VerbatimTimePeriod clone()  {
            VerbatimTimePeriod result = (VerbatimTimePeriod)super.clone();
            result.setVerbatimDate(this.verbatimDate);
            return result;
    }
}
