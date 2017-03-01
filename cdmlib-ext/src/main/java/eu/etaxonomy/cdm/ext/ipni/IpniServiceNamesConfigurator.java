/**
 *
 */
package eu.etaxonomy.cdm.ext.ipni;

import org.apache.log4j.Logger;

/**
 * Configurator for IPNI name service import / mapping.
 * @author a.mueller
 */
public class IpniServiceNamesConfigurator extends IpniServiceConfiguratorBase implements IIpniServiceConfigurator {
	public static final Logger logger = Logger.getLogger(IpniServiceNamesConfigurator.class);

	//by default we do not include basionym import
	boolean doBasionyms = false;  //includes replaced synonyms
    public boolean isDoBasionyms() {return doBasionyms;}
    public void setDoBasionyms(boolean doBasionyms){this.doBasionyms = doBasionyms;}

	boolean doType = true;
    public boolean isDoType() {return doType;}
    public void setDoType(boolean doType){this.doType = doType;}

    boolean doApni = true;
    public boolean isDoApni() {return doApni;}
    public void setDoApni(boolean doApni){this.doApni = doApni;}

    boolean doIk = true;
    public boolean isDoIk() {return doIk;}
    public void setDoIk(boolean doIk){this.doIk = doIk;}

    boolean doGci = true;
    public boolean isDoGci() {return doGci;}
    public void setDoGci(boolean doGci){this.doGci = doGci;}

    boolean sortByFamily = true;
    public boolean isSortByFamily() {return sortByFamily;}
    public void setSortByFamily(boolean sortByFamily){this.sortByFamily = sortByFamily;}

    boolean includePublicationAuthors;
    public boolean isIncludePublicationAuthors() {return includePublicationAuthors;}
    public void setIncludePublicationAuthors(boolean includePublicationAuthors){this.includePublicationAuthors = includePublicationAuthors;}

    boolean includeBasionymAuthors;
    public boolean isIncludeBasionymAuthors() {return includeBasionymAuthors;}
    public void setIncludeBasionymAuthors(boolean includeBasionymAuthors){this.includeBasionymAuthors = includeBasionymAuthors;}


    /**
     * @return
     */
    public static IpniServiceNamesConfigurator NewInstance() {
        return new IpniServiceNamesConfigurator();
    }

}
