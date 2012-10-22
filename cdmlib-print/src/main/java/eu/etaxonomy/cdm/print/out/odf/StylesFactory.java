// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print.out.odf;

import org.apache.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.doc.style.OdfStyle;
import org.odftoolkit.odfdom.doc.style.OdfStyleParagraphProperties;
import org.odftoolkit.odfdom.doc.style.OdfStyleTabStop;
import org.odftoolkit.odfdom.doc.style.OdfStyleTabStops;
import org.odftoolkit.odfdom.doc.style.OdfStyleTextProperties;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;

/**
 * @author n.hoffmann
 * @created Apr 29, 2010
 * @version 1.0
 */
public class StylesFactory implements IStyleNames{
	private static final Logger logger = Logger.getLogger(StylesFactory.class);
	
	OdfOfficeStyles officeStyles;
	OdfDefaultStyle defaultStyle;
	OdfStyleParagraphProperties paragraphProperties;

	OdfStyleTabStops tabStops;
	OdfStyleTabStop tabStop;


	public StylesFactory(OdfTextDocument outputDocument) {
		this.officeStyles = outputDocument.getOrCreateDocumentStyles();
		init();
	}
	
	private void init() {
		createDefaultStyles();
		
		createHeadings();
		
	}

	public void createDefaultStyles(){
		// Set default font size to 10 point
		defaultStyle = officeStyles.getDefaultStyle(
			OdfStyleFamily.Paragraph);
		setFontSize(defaultStyle, "10pt");
	}
	
	private void createHeadings(){
		OdfStyle style;
		
		style = officeStyles.newStyle(HEADING_ACCEPTED_TAXON,
				OdfStyleFamily.Paragraph);
		style.setStyleDisplayNameAttribute("Accepted Taxon Heading");
		style.setProperty(OdfStyleParagraphProperties.MarginTop, "0.25cm");
		style.setProperty(OdfStyleParagraphProperties.MarginBottom, "0.25cm");
		setFontWeight(style, "bold");
		setFontSize(style, "20pt");
		
		style = officeStyles.newStyle(HEADING_FEATURE,
				OdfStyleFamily.Paragraph);
		style.setStyleDisplayNameAttribute("Feature Heading");
		style.setProperty(OdfStyleParagraphProperties.MarginTop, "0.25cm");
		style.setProperty(OdfStyleParagraphProperties.MarginBottom, "0.25cm");
		setFontWeight(style, "bold");
		setFontSize(style, "14pt");
	}
	
//	@Deprecated // testing
//	void addOfficeStyles() {
//		OdfDefaultStyle defaultStyle;
//		OdfStyle style;
//		OdfStyleParagraphProperties pProperties;
//
//		OdfStyleTabStops tabStops;
//		OdfStyleTabStop tabStop;
//
//		// Set default font size to 10 point
//		defaultStyle = stylesOfficeStyles
//				.getDefaultStyle(OdfStyleFamily.Paragraph);
//		setFontSize(defaultStyle, "10pt");
//
//		// movie heading: 14pt, bold
//		style = stylesOfficeStyles.newStyle("Movie_20_Heading",
//				OdfStyleFamily.Paragraph);
//		style.setStyleDisplayNameAttribute("Movie Heading");
//		style.setProperty(OdfStyleParagraphProperties.MarginTop, "0.25cm");
//		style.setProperty(OdfStyleParagraphProperties.MarginBottom, "0.25cm");
//		setFontWeight(style, "bold");
//		setFontSize(style, "14pt");
//
//		// span for stars: 12pt, bold
//		style = stylesOfficeStyles
//				.newStyle("Star_20_Span", OdfStyleFamily.Text);
//		style.setStyleDisplayNameAttribute("Star Span");
//		setFontWeight(style, "bold");
//		setFontSize(style, "12pt");
//
//		// Cast heading is 12pt italic
//		style = stylesOfficeStyles.newStyle("Cast_20_Heading",
//				OdfStyleFamily.Paragraph);
//		style.setStyleDisplayNameAttribute("Cast Heading");
//		style.setProperty(OdfStyleParagraphProperties.MarginTop, "0.5cm");
//		style.setProperty(OdfStyleParagraphProperties.MarginBottom, "0.25cm");
//		setFontStyle(style, "italic");
//		setFontSize(style, "12pt");
//
//		// Paragraph with tab stop at 7.5cm with a
//		// leader of "." This is used for the
//		// cast list.
//		style = stylesOfficeStyles.newStyle("Cast_20_Para",
//				OdfStyleFamily.Paragraph);
//		style.setStyleDisplayNameAttribute("Cast Para");
//		style.setStyleFamilyAttribute(OdfStyleFamily.Paragraph.toString());
//
//		// build hierarchy from "inside out"
//		tabStop = new OdfStyleTabStop(stylesDom);
//		tabStop.setStylePositionAttribute("7.5cm");
//		tabStop.setStyleLeaderStyleAttribute("dotted");
//		tabStop.setStyleLeaderTextAttribute(".");
//		tabStop.setStyleTypeAttribute("right");
//
//		tabStops = new OdfStyleTabStops(stylesDom);
//		tabStops.appendChild(tabStop);
//
//		pProperties = new OdfStyleParagraphProperties(stylesDom);
//		pProperties.appendChild(tabStops);
//
//		style.appendChild(pProperties);
//
//		// style for the movie synopsis
//		style = stylesOfficeStyles.newStyle("Synopsis_20_Para",
//				OdfStyleFamily.Paragraph);
//		style.setStyleDisplayNameAttribute("Synopsis Para");
//		style.setProperty(OdfStyleParagraphProperties.Border,
//				"0.035cm solid #000000");
//		style.setProperty(OdfStyleParagraphProperties.Padding, "0.25cm");
//		style.setProperty(OdfStyleParagraphProperties.MarginLeft, "1cm");
//		style.setProperty(OdfStyleParagraphProperties.MarginRight, "1cm");
//		style.setProperty(OdfStyleParagraphProperties.TextIndent, "0.25cm");
//	}
	

	private void setFontWeight(OdfStyleBase style, String value) {
		style.setProperty(OdfStyleTextProperties.FontWeight, value);
		style.setProperty(OdfStyleTextProperties.FontWeightAsian, value);
		style.setProperty(OdfStyleTextProperties.FontWeightComplex, value);
	}

	private void setFontStyle(OdfStyleBase style, String value) {
		style.setProperty(OdfStyleTextProperties.FontStyle, value);
		style.setProperty(OdfStyleTextProperties.FontStyleAsian, value);
		style.setProperty(OdfStyleTextProperties.FontStyleComplex, value);
	}

	private void setFontSize(OdfStyleBase style, String value) {
		style.setProperty(OdfStyleTextProperties.FontSize, value);
		style.setProperty(OdfStyleTextProperties.FontSizeAsian, value);
		style.setProperty(OdfStyleTextProperties.FontSizeComplex, value);
	}
}
