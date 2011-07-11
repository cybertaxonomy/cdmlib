/**
 * 
 */
package eu.etaxonomy.cdm.ext.ipni;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public class IpniServicePublicationConfigurator extends IpniServiceConfiguratorBase  implements IIpniServiceConfigurator{
	public static final Logger logger = Logger.getLogger(IpniServicePublicationConfigurator.class);
	
	/**
	 * If true the abbreviation is used as title instead of the title 
	 */
	private boolean useAbbreviationAsTitle = false;

	
	
	public void setUseAbbreviationAsTitle(boolean useAbbreviationAsTitle) {
		this.useAbbreviationAsTitle = useAbbreviationAsTitle;
	}

	public boolean isUseAbbreviationAsTitle() {
		return useAbbreviationAsTitle;
	} 
	
}
