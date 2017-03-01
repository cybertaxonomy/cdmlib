package eu.etaxonomy.cdm.ext.ipni;

import eu.etaxonomy.cdm.ext.ipni.IIpniService.DelimitedFormat;

public interface IIpniServiceConfigurator {

	/**
	 * The IPNI delimited data format to use. See {@link http://www.ipni.org/ipni/delimited_help.html} for
	 * further information on the delimited data format.
	 * <BR/><BR/>
	 * NOTE: Not all services support all formats.
	 */
	public DelimitedFormat getFormat();

	/**
	 * @see #getFormat()
	 * @param format
	 */
	public void setFormat(DelimitedFormat format);

}
