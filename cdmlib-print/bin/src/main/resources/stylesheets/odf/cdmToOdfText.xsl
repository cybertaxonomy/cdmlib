<?xml version="1.0" encoding="UTF-8"?>
<!--
	Translates the information written in cdm webservice taxon output into
	ODF text.
-->
<!--xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0" 
	xmlns:dc="http://purl.org/dc/elements/1.1/" 
	xmlns:dom="http://www.w3.org/2001/xml-events" 
	xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0" 
	xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" 
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0" 
	xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0" 
	xmlns:math="http://www.w3.org/1998/Math/MathML" 
	xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0" 
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0" 
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" 
	xmlns:ooo="http://openoffice.org/2004/office" 
	xmlns:oooc="http://openoffice.org/2004/calc" 
	xmlns:ooow="http://openoffice.org/2004/writer" 
	xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0" 
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0" 
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0" 
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" 
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" 
	xmlns:xforms="http://www.w3.org/2002/xforms" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	exclude-result-prefixes="text"-->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0">
	
	
	<!-- imports -->
	<!--xsl:import href="xxx.xsl" /-->
	
	
	<xsl:preserve-space elements="reference"/>
	
	<!-- start xsl output -->
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="no"/>
	<xsl:param name="versionParam" select="'1.0'"/>
	
	<!-- root element: root -->
	<xsl:template match="root">
		<root>
			<xsl:for-each select="//taxon">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</root>
	</xsl:template>
	
	
	
	<!--xsl:template match="TaxonNode">
		<xsl:apply-templates select="taxon"/>
		</xsl:template-->
	
	<xsl:template match="taxon">
		<xsl:apply-templates select="name"/>
		<xsl:apply-templates select="descriptions"/>
	</xsl:template>
	
	<xsl:template match="name" name="TaxonName">
		<text:h text:style-name="heading_accepted_taxon">
			<xsl:value-of select="titleCache"/>
		</text:h>
	</xsl:template>
	
	
	<xsl:template match="descriptions">
		<xsl:for-each select="TaxonDescription">
			<xsl:apply-templates select="."/>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="TaxonDescription">
		
		<xsl:for-each select="elements">
			<xsl:for-each select="e">
				<!--xsl:variable name="lastFeature"><xsl:value-of select="feature/uuid"/></xsl:variable-->
				--------------<!-- for some reason feture does not get processed without these -->
				
				<!--xsl:value-of select="$lastFeature"/>
				<xsl:value-of select="feature/uuid"/>
				<xsl:if test="feature/uuid!=$lastFeature"-->
				<xsl:apply-templates select="feature" />
				<!--/xsl:if-->
				--------------
				<xsl:apply-templates select="multilanguageText_L10n" />
				
				
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="feature">
		<text:h  text:style-name="heading_feature">
			<xsl:value-of select="representation_L10n"/>
		</text:h>
	</xsl:template>
	
	<xsl:template match="multilanguageText_L10n">
		<text:p><xsl:value-of select="text"/></text:p>
	</xsl:template>
	
	<xsl:template match="text()|@*">
		<!-- all content without a specific template should be omitted -->
	</xsl:template>
</xsl:stylesheet>
