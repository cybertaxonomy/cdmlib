package eu.etaxonomy.cdm.ext.ipni;

import eu.etaxonomy.cdm.ext.ipni.IIpniService.DelimitedFormat;

public class IpniServiceConfiguratorBase implements IIpniServiceConfigurator{
	
	private DelimitedFormat format = getDefaultFormat();

	
	
	public void setFormat(DelimitedFormat format) {
		this.format = format;
	}

	public DelimitedFormat getFormat() {
		if (format == null){
			format = getDefaultFormat();
		}
		return format;
	}

	private DelimitedFormat getDefaultFormat() {
		return DelimitedFormat.SHORT;
	}
}
