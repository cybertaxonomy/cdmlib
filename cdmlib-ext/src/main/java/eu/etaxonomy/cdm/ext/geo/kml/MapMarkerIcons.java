/**
 * Copyright (C) 2020 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.ext.geo.kml;

import java.net.MalformedURLException;
import java.net.URL;

import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;

/**
 *
 * MOre icons at http://kml4earth.appspot.com/icons.html
 *
 * @author Andreas Kohlbecker
 * @since Apr 21, 2020
 */
public enum MapMarkerIcons {

	blu_blank("https://maps.google.com/mapfiles/kml/paddle/blu-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	blu_circle("https://maps.google.com/mapfiles/kml/paddle/blu-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	blu_diamond("https://maps.google.com/mapfiles/kml/paddle/blu-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	blu_square("https://maps.google.com/mapfiles/kml/paddle/blu-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	blu_stars("https://maps.google.com/mapfiles/kml/paddle/blu-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	grn_blank("https://maps.google.com/mapfiles/kml/paddle/grn-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	grn_circle("https://maps.google.com/mapfiles/kml/paddle/grn-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	grn_diamond("https://maps.google.com/mapfiles/kml/paddle/grn-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	grn_square("https://maps.google.com/mapfiles/kml/paddle/grn-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	grn_stars("https://maps.google.com/mapfiles/kml/paddle/grn-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	ltblu_blank("https://maps.google.com/mapfiles/kml/paddle/ltblu-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ltblu_circle("https://maps.google.com/mapfiles/kml/paddle/ltblu-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ltblu_diamond("https://maps.google.com/mapfiles/kml/paddle/ltblu-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ltblu_square("https://maps.google.com/mapfiles/kml/paddle/ltblu-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ltblu_stars("https://maps.google.com/mapfiles/kml/paddle/ltblu-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	pink_blank("https://maps.google.com/mapfiles/kml/paddle/pink-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	pink_circle("https://maps.google.com/mapfiles/kml/paddle/pink-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	pink_diamond("https://maps.google.com/mapfiles/kml/paddle/pink-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	pink_square("https://maps.google.com/mapfiles/kml/paddle/pink-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	pink_stars("http://maps.google.com/mapfiles/kml/paddle/pink-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	purple_blank("http://maps.google.com/mapfiles/kml/paddle/purple-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	purple_circle("http://maps.google.com/mapfiles/kml/paddle/purple-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	purple_diamond("http://maps.google.com/mapfiles/kml/paddle/purple-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	purple_square("http://maps.google.com/mapfiles/kml/paddle/purple-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	purple_stars("http://maps.google.com/mapfiles/kml/paddle/purple-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	red_blank("https://maps.google.com/mapfiles/kml/paddle/red-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	red_circle("https://maps.google.com/mapfiles/kml/paddle/red-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	red_diamond("https://maps.google.com/mapfiles/kml/paddle/red-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	red_square("https://maps.google.com/mapfiles/kml/paddle/red-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	red_stars("https://maps.google.com/mapfiles/kml/paddle/red-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	ylw_blank("http://maps.google.com/mapfiles/kml/paddle/ylw-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ylw_circle("http://maps.google.com/mapfiles/kml/paddle/ylw-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ylw_diamond("http://maps.google.com/mapfiles/kml/paddle/ylw-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ylw_square("http://maps.google.com/mapfiles/kml/paddle/ylw-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	ylw_stars("http://maps.google.com/mapfiles/kml/paddle/ylw-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	orange_blank("http://maps.google.com/mapfiles/kml/paddle/orange-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	orange_circle("http://maps.google.com/mapfiles/kml/paddle/orange-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	orange_diamond("http://maps.google.com/mapfiles/kml/paddle/orange-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	orange_square("http://maps.google.com/mapfiles/kml/paddle/orange-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	orange_stars("http://maps.google.com/mapfiles/kml/paddle/orange-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	wht_blank("http://maps.google.com/mapfiles/kml/paddle/wht-blank.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	wht_circle("http://maps.google.com/mapfiles/kml/paddle/wht-circle.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	wht_diamond("http://maps.google.com/mapfiles/kml/paddle/wht-diamond.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	wht_square("http://maps.google.com/mapfiles/kml/paddle/wht-sqare.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),
	wht_stars("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png", KmlFactory.createVec2().withX(0.5).withY(0).withXunits(Units.FRACTION).withYunits(Units.FRACTION)),

	sunny("http://maps.google.com/mapfiles/kml/shapes/sunny.png", null);

	URL url;
	Vec2 hotspot;

	MapMarkerIcons(String ulr, Vec2 hotspot) {
		try {
			this.url = new URL(ulr);
			if(hotspot != null) {
				this.hotspot = hotspot;
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return url.toString();
	}

	public Vec2 hotSpot() {
		return hotspot;
	}

	public IconStyle asIconStyle() {
		IconStyle iconStyle = KmlFactory.createIconStyle().withIcon(
				KmlFactory.createIcon().withHref(this.toString())
				);
		iconStyle.setScale(1);
		iconStyle.setHotSpot(hotspot);
		return iconStyle;
	}

}
