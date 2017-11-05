/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.common.media;

import org.apache.log4j.Logger;

/**
 *
 * @author n.hoffmann
 * @created 13.11.2008
 */
public abstract class MediaInfo {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MediaInfo.class);
	private String formatName;
	private String mimeType;
	private long length;
	private String suffix;


	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getMimeType() {
		return mimeType;
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}
	public String getFormatName() {
		return formatName;
	}

	public void setLength(long length) {
		this.length = length;
	}
	public long getLength() {
		return length;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getSuffix() {
		return suffix;
	}

}
