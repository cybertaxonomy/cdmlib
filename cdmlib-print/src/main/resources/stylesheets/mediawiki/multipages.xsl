<!DOCTYPE xsl:stylesheet [<!ENTITY hyphen "&#45;" > <!ENTITY ndash "&#x2013;" >]>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:functx="http://www.functx.com"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:import href="src/main/resources/stylesheets/mediawiki/functx-1.0-doc-2007-01.xsl" />
	<!--xsl:import href="functx-1.0-doc-2007-01.xsl" /-->
	<!-- xsl:output method="xml" indent="no"/> -->
	<xsl:strip-space elements="text" />

	<!-- Authors: Sybille & Lorna -->
	<!-- Date: March/April 2013 -->

	<!--++++++++++ global vars (and templates used as such) +++++++++++++++++++++++++++ -->

	<!-- mediawiki prefix -->
	<xsl:param name="prefix"></xsl:param>
	<xsl:variable name="page-prefix">
		<!-- <xsl:text>Internal:</xsl:text> -->
		<xsl:value-of select="$prefix"></xsl:value-of>
	</xsl:variable>
	
	<!-- cdm url of source -->
	<xsl:param name="cdm-url"></xsl:param>
	<xsl:variable name="cdm-credit-text">
		<xsl:value-of select="concat('This page was generated automatically from content in ', $cdm-url)"></xsl:value-of>
	</xsl:variable>

	<!-- create a timestamp for the whole going -->
	<xsl:variable name="timestamp">
		<xsl:value-of
			select="concat(year-from-date(current-date()),'-',month-from-date(current-date()),'-',day-from-date(current-date()),'T',hours-from-time(current-time()),':',minutes-from-time(current-time()),':00Z')" />
	</xsl:variable>

	<!-- create the username who changed/created the pages -->
	<xsl:param name="username"></xsl:param>
	<xsl:variable name="u_name">
		<xsl:value-of select="$username"></xsl:value-of>
	</xsl:variable>

	<xsl:template name="wiki-newline">
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template name="wiki-newline-comment">
		<xsl:comment>
			<xsl:text>&#xa; </xsl:text>
		</xsl:comment>
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- this is the start template it creates the mediawiki tag surounding 
		and calls a template to create a page for every taxon node TODO: and a category -->
	<xsl:template match="root">
		<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.7/"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.7.xsd"
			version="0.7" xml:lang="en">
			<siteinfo>
				<sitename>TestWiki</sitename>
				<base>http://biowikifarm.net/test/Main_Page</base>
				<generator>MediaWiki 1.20.3</generator>
				<case>first-letter</case>
				<namespaces>
					<namespace key="-2" case="first-letter">Media</namespace>
					<namespace key="-1" case="first-letter">Special</namespace>
					<namespace key="0" case="first-letter" />
					<namespace key="1" case="first-letter">Talk</namespace>
					<namespace key="2" case="first-letter">User</namespace>
					<namespace key="3" case="first-letter">User talk</namespace>
					<namespace key="4" case="first-letter">TestWiki</namespace>
					<namespace key="5" case="first-letter">TestWiki talk</namespace>
					<namespace key="6" case="first-letter">File</namespace>
					<namespace key="7" case="first-letter">File talk</namespace>
					<namespace key="8" case="first-letter">MediaWiki</namespace>
					<namespace key="9" case="first-letter">MediaWiki talk</namespace>
					<namespace key="10" case="first-letter">Template</namespace>
					<namespace key="11" case="first-letter">Template talk</namespace>
					<namespace key="12" case="first-letter">Help</namespace>
					<namespace key="13" case="first-letter">Help talk</namespace>
					<namespace key="14" case="first-letter">Category</namespace>
					<namespace key="15" case="first-letter">Category talk</namespace>
					<namespace key="102" case="first-letter">Property</namespace>
					<namespace key="103" case="first-letter">Property talk</namespace>
					<namespace key="106" case="first-letter">Form</namespace>
					<namespace key="107" case="first-letter">Form talk</namespace>
					<namespace key="108" case="first-letter">Concept</namespace>
					<namespace key="109" case="first-letter">Concept talk</namespace>
					<namespace key="170" case="first-letter">Filter</namespace>
					<namespace key="171" case="first-letter">Filter talk</namespace>
					<namespace key="198" case="first-letter">Internal</namespace>
					<namespace key="199" case="first-letter">Internal talk</namespace>
					<namespace key="200" case="first-letter">Portal</namespace>
					<namespace key="201" case="first-letter">Portal talk</namespace>
					<namespace key="202" case="first-letter">Bibliography</namespace>
					<namespace key="203" case="first-letter">Bibliography talk</namespace>
					<namespace key="204" case="first-letter">Draft</namespace>
					<namespace key="205" case="first-letter">Draft talk</namespace>
					<namespace key="206" case="first-letter">Submission</namespace>
					<namespace key="207" case="first-letter">Submission talk</namespace>
					<namespace key="208" case="first-letter">Reviewed</namespace>
					<namespace key="209" case="first-letter">Reviewed talk</namespace>
					<namespace key="420" case="first-letter">Layer</namespace>
					<namespace key="421" case="first-letter">Layer talk</namespace>
				</namespaces>
			</siteinfo>
			<xsl:apply-templates select="//TaxonNode" />
		</mediawiki>
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- this creates a page and a category for every taxon node. -->
	<!-- TODO create a template for page creating and for categorie creating -->
	<xsl:template match="TaxonNode" name="TaxonNode">
		<!-- as we will need the title more than once, we create a variable -->
		<xsl:variable name="title">
			<xsl:call-template name="title">
				<xsl:with-param name="taxon" select="Taxon" />
			</xsl:call-template>
		</xsl:variable>

		<!-- we also initialize the parent taxon variable if there is a higher 
			taxon assigned: -->

		<xsl:variable name="parent-title">
			<xsl:if test="exists(../..) and name(../..)='TaxonNode'">

				<xsl:call-template name="title">
					<xsl:with-param name="taxon" select="../../Taxon" />
				</xsl:call-template>

			</xsl:if>
			<!-- else if no higher taxon could be found -->
			<xsl:if test="not(exists(../..)) or not(name(../..)='TaxonNode')">

				<xsl:text>{{EDIT No Parent available}}</xsl:text>

			</xsl:if>
		</xsl:variable>

		<!-- create category -->
		<xsl:text>
            <!-- this creates a newline before the <page> 
            if it was not there, the page could not be imported by the mediawiki-->
        </xsl:text>

		<page>
			<title>
				<xsl:text>Category:</xsl:text>
				<xsl:value-of select="$title" />
			</title>
			<revision>
				<!-- TODO: create seconds without positions after decimal point! -->
				<timestamp>
					<xsl:value-of select="$timestamp" />
				</timestamp>
				<contributor>
					<username>
						<xsl:value-of select="$u_name" />
					</username>
				</contributor>

				<text>
					<xsl:attribute name="xml:space">preserve</xsl:attribute>
					<!-- redirekt to corresponding page -->
					<xsl:value-of select="concat('#REDIRECT [[',$page-prefix,$title,']]')" />
					<!-- add parent categorie if exists -->
					<xsl:if test="exists(../..) and name(../..)='TaxonNode'">
						<xsl:call-template name="wiki-newline" />
						<xsl:value-of select="concat('[[Category:',$parent-title,']]')" />
					</xsl:if>
				</text>

			</revision>
		</page>

		<!-- create taxon page -->
		<page>
			<title>
				<xsl:value-of select="concat($page-prefix,$title)" />
			</title>
			<revision>
				<!-- MAYDO: create seconds without positions after decimal point! -->
				<timestamp>
					<xsl:value-of select="$timestamp" />
				</timestamp>
				<contributor>
					<username>
						<xsl:value-of select="$username" />
					</username>
				</contributor>

				<text>
					<xsl:attribute name="xml:space">preserve</xsl:attribute>
					<!-- add table of contents -->
					<xsl:call-template name="TOC" />
					<!-- add taxo tree -->
					<xsl:value-of
						select="concat('{{EDIT_Taxotree| parentTaxon=',$page-prefix,$parent-title,'}}')" />
					<xsl:call-template name="wiki-newline" />
					<xsl:call-template name="wiki-newline" />

					<!-- add contents of taxon page -->
					<xsl:call-template name="credit" />
					<xsl:call-template name="wiki-newline" />
					<xsl:apply-templates select="Taxon" />
					<xsl:call-template name="display-references" />
					<xsl:call-template name="wiki-newline" />
					<xsl:call-template name="wiki-newline" />
					<!-- put page to corresponding tax category -->
					<xsl:value-of select="concat('[[Category:',$title, ']]')" />

				</text>
			</revision>
		</page>

		<!--<xsl:apply-templates select="//childNodes/TaxonNode/Taxon"/> -->
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- this template provides the correct name of a taxon parameter: taxon 
		the taxon we want to get the name from -->
	<xsl:template name="title">
		<xsl:param name="taxon" />
		<xsl:choose>
			<!-- family -->
			<xsl:when
				test="$taxon/name/rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
				<xsl:call-template name="get-family-or-genus-title">
					<xsl:with-param name="genusOrUninomial" select="$taxon/name/genusOrUninomial" />
				</xsl:call-template>
			</xsl:when>
			<!-- genus -->
			<xsl:when
				test="$taxon/name/rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
				<xsl:call-template name="get-family-or-genus-title">
					<xsl:with-param name="genusOrUninomial" select="$taxon/name/genusOrUninomial" />
				</xsl:call-template>
			</xsl:when>
			<!--TODO -->
			<!-- subgenus -->
			<xsl:when
				test="$taxon/name/rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
				<xsl:apply-templates select="$taxon/name/rank/representation_L10n" />
				<xsl:text> </xsl:text>
				<xsl:apply-templates select="$taxon/name/genusOrUninomial" />
			</xsl:when>
			<!-- species -->
			<xsl:when
				test="$taxon/name/rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
				<xsl:call-template name="get-species-title">
					<xsl:with-param name="taxon" select="$taxon" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- for debugging -->
				Unformatted title for rank uuid:
				<xsl:value-of select="$taxon/name/rank/uuid" />
				:
				<xsl:value-of select="$taxon/name/titleCache">
				</xsl:value-of>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- these templates create the name of the taxa wheater their kind is -->

	<xsl:template name="get-family-or-genus-title">
		<xsl:param name="genusOrUninomial" />
		<xsl:value-of select="$genusOrUninomial" />
	</xsl:template>

	<xsl:template name="get-species-title">
		<xsl:param name="taxon" />
		<xsl:value-of
			select="concat($taxon/name/genusOrUninomial, ' ', $taxon/name/specificEpithet)" />
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- we run this for the content of the page -->
	<xsl:template match="Taxon" name="Taxon">
		
		<xsl:apply-templates select="synonymy" />
		<xsl:apply-templates select="key" />
		<xsl:apply-templates select="descriptions" />
		<!-- A change to Gallery: uncomment following line: -->
		<!-- <xsl:call-template name="gallery"/> -->
		<xsl:call-template name="featureGallery" />
		<!-- TODO to change to Figure Gallery: call figure gallery in the right 
			place what else? -->
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- these templates provide the citations -->

	<xsl:template name="display-references">
		<xsl:text>{{EDIT_Reference_Section}}</xsl:text>
	</xsl:template>


	<xsl:template name="citations">
		<xsl:param name="name-uuid" />
		<xsl:param name="descriptionelements" />

		<!--<ref name="{$name-uuid}"> -->
		<!-- iterate through all the description elements for the citation feature -->
		<xsl:for-each select="$descriptionelements/descriptionelement">

			<xsl:sort select="sources/e[1]/citation/datePublished/start" />

			<xsl:variable name="citation-uuid" select="sources/e[1]/citation/uuid" />

			<xsl:for-each select="sources/e">
				<xsl:if test="nameUsedInSource/uuid=$name-uuid">

					<!-- call reference template with sources/e/citation -->
					<xsl:call-template name="reference">
						<xsl:with-param name="reference-node" select="citation" />
					</xsl:call-template>
					<!-- use the citation-uuid as a unique name for the reference -->

				</xsl:if>
			</xsl:for-each>
			<!--</ref> -->
		</xsl:for-each>

	</xsl:template>

	<!-- an example of mediawiki formtting of references: Mohrig, W.; Menzel, 
		F. 1992: Neue Arten europäischer Trauermücken (Diptera, Sciaridae). An International 
		Journal of Dipterological Research, 3(1-2), 1–16. Authors Date: title. inReferences 
		title in italics, Volume in bold(series), page range -->
	<xsl:template name="reference">
		<xsl:param name="reference-node" />
		<!--TODO Do we need to sort the list of references in WikiMedia or will 
			they just appear in numbered order as they occur in the names -->
		<!--xsl:sort select="sources/e[1]/citation/datePublished/start"/ -->

		<!-- first of all check if any of the data we show exist: TODO remove this 
			test, if we modified CDM services, to export proper data -->
		<xsl:if
			test="exists($reference-node/authorship/teamMembers/e[1]/lastname) 
            or exists($reference-node/authorship/lastname) or exists($reference-node/datePublished/start)
            or exists($reference-node/title)">

			<!-- use the citation-uuid as a unique name for the reference -->
			<xsl:variable name="citation-uuid" select="$reference-node/uuid" />

			<!-- use the citation-uuid as a unique name for the reference -->
			<xsl:value-of
				select="concat('{{EDIT_Reference|name=',$citation-uuid,'|content=')" />

			<xsl:choose>
				<xsl:when test="exists($reference-node/authorship/teamMembers)">
					<xsl:text>{{aut|</xsl:text>
					<xsl:for-each select="$reference-node/authorship/teamMembers/e">
						<xsl:value-of select="lastname" />
						<xsl:choose>
							<xsl:when test="position() != last()">
								<xsl:text> &amp; </xsl:text>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
					<xsl:text>}} </xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="exists($reference-node/authorship/lastname)">
							<xsl:text>{{aut|</xsl:text>
							<xsl:value-of select="$reference-node/authorship/lastname" />
							<xsl:text>}} </xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$reference-node/titleCache"></xsl:value-of>
						</xsl:otherwise>
					</xsl:choose>

				</xsl:otherwise>

			</xsl:choose>

			<!-- DEBUGGING display uuid so can check references against those in Tax 
				Editor -->
			<!--<xsl:if test="$reference-node/datePublished/start != ''"> <xsl:value-of 
				select="$reference-node/datePublished/start"/> <xsl:text>: </xsl:text> </xsl:if> -->
			<xsl:apply-templates select="$reference-node/title" />
			<!-- do we need any other info from inReference node - see flore-afrique-centrale.xsl 
				xsl -->
			<xsl:variable name="wiki-markup">
				''
			</xsl:variable>

			<xsl:if test="$reference-node/inReference/node()">
				<xsl:value-of
					select="concat($wiki-markup, $reference-node/inReference/title, $wiki-markup)" />
			</xsl:if>
			<xsl:apply-templates select="$reference-node/volume" />
			<xsl:apply-templates select="$reference-node/series" />
			<xsl:apply-templates select="$reference-node/pages" />
			<xsl:apply-templates select="$reference-node/placePublished" />
			<xsl:apply-templates select="$reference-node/publisher" />

			<!--xsl:choose> <xsl:when test="$reference-node/inReference/node()"> <xsl:value-of 
				select="$reference-node/title"/> <xsl:text>. </xsl:text> <xsl:value-of select="$reference-node/inReference/title"/> 
				</xsl:when> <xsl:otherwise> <xsl:value-of select="$reference-node/title"/> 
				</xsl:otherwise> </xsl:choose> <xsl:text>.</xsl:text> <xsl:value-of select="$reference-node/pages"/> 
				<xsl:text>.</xsl:text> <xsl:if test="$reference-node/type = 'Book' or $reference-node/type 
				= 'BookSection'"> <xsl:if test="$reference-node/placePublished != '' or $reference-node/publisher 
				!= ''"> <xsl:text> </xsl:text> <xsl:value-of select="$reference-node/placePublished"/> 
				<xsl:text>, </xsl:text> <xsl:value-of select="$reference-node/publisher"/> 
				<xsl:text>.</xsl:text> </xsl:if> </xsl:if -->
			<xsl:text>}}</xsl:text>
		</xsl:if>
		<!--</ref> -->
		<xsl:call-template name="wiki-newline-comment" />
	</xsl:template>

	<!-- wild card match for different children of nomenclaturalReference and 
		citaion nodes -->
	<xsl:template match="*">

		<xsl:choose>
			<xsl:when test="name(.) = 'pages'">
				<xsl:value-of select="replace(.,'&hyphen;','&ndash;')" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="." />
			</xsl:otherwise>
		</xsl:choose>

		<!--<xsl:text>FOO </xsl:text> -->
		<xsl:if
			test="name(.) = 'title' or name(.) = 'publisher' or name(.) = 'pages'">
			<!-- . if publisher or title - comma if placePublished -->
			<xsl:text>. </xsl:text>
		</xsl:if>

		<xsl:if test="name(.) = 'volume'">
			<xsl:text>: </xsl:text>
		</xsl:if>
		<xsl:if
			test="(../type = 'Book' or ../type = 'BookSection') and name(.) = 'placePublished' and . != ''">
			<xsl:text>, </xsl:text>
		</xsl:if>
	</xsl:template>
	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- these templates provide the polytomous key For example: {{Lead | 1=4| 
		2= jeunes couverts de cils simples et glanduleux mais jamais de cils denticulés 
		ou branchus| result = Erica trimera (Flora of XX) | result text = ''Erica 
		trimera''}} -->
	<xsl:template match="key" name="key">

		<xsl:param name="uuidFamily">
			210a8214-4e69-401a-8e47-c7940d990bdd
		</xsl:param>
		<xsl:param name="uuidGenus">
			1b11c34c-48a8-4efa-98d5-84f7f66ef43a
		</xsl:param>
		<xsl:param name="uuidSubgenus">
			78786e16-2a70-48af-a608-494023b91904
		</xsl:param>

		<xsl:if test="HashMap/records/e">

			<xsl:call-template name="chapter">
				<xsl:with-param name="title">
					Key
				</xsl:with-param>
			</xsl:call-template>

			<xsl:variable name="key-name" select="HashMap/titleCache" />
			<xsl:value-of
				select="concat('{{Key Start | id =',$key-name,'|title=',$key-name,'|edited by=L.Morris}}')" />


			<!--xsl:if test="ArrayList/e" -->
			<xsl:for-each select="HashMap/records/e">

				<xsl:variable name="node-number" select="nodeNumber" />
				<xsl:variable name="child-statement" select="childStatement" />

				<!-- TaxonLinkDto or PolytomousKeyNodeLinkDto -->
				<!--xsl:value-of select="concat('{{Decision | id =', $node-number, '| 
					lead 1 = ', $child-statement)"/ -->
				<xsl:value-of select="concat('{{Lead | 1 =', $node-number)" />

				<xsl:if test="edgeNumber = 2">
					<xsl:text>*</xsl:text>
				</xsl:if>

				<xsl:value-of select="concat('| 2 =', $child-statement)" />


				<xsl:choose>
					<xsl:when test="links/e[1]/class = 'PolytomousKeyNodeLinkDto'">
						<xsl:variable name="link-node-number" select="links/e[1]/nodeNumber" />
						<xsl:value-of select="concat('| 3 =', $link-node-number)" />
					</xsl:when>
					<xsl:when test="links/e[1]/class = 'TaxonLinkDto'">

						<xsl:text>| result  = '' </xsl:text>
						<xsl:variable name="taxonUuid" select="links/e[1]/uuid" />
						<xsl:variable name="genus"
							select="//Taxon/uuid[.=$taxonUuid]/../name/genusOrUninomial" />
						<xsl:choose>
							<xsl:when test="taxonUuid = $uuidSubgenus">
								<xsl:variable name="repr"
									select="//Taxon/uuid[.='71cd0e8d-47eb-4c66-829a-e21c705ee660']/../name/rank/representation_L10n" />
								<xsl:value-of select="concat($genus, '. ', $repr)" />
							</xsl:when>
							<xsl:when test="taxonUuid = $uuidGenus">
								<xsl:apply-templates select="$genus" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="specificEpithet"
									select="//Taxon/uuid[.=$taxonUuid]/../name/specificEpithet" />
								<!-- abbreviate the genus for species names -->
								<xsl:value-of select="concat($genus, ' ', $specificEpithet)" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>''</xsl:text>
					</xsl:when>
					<xsl:otherwise />
				</xsl:choose>

				<xsl:text>}}</xsl:text>

			</xsl:for-each>

			<xsl:text>{{Key End}}</xsl:text>
			<xsl:call-template name="wiki-newline"></xsl:call-template>
		</xsl:if>

	</xsl:template>

	<!-- this template provides the Credit to the source database -->
	
	<xsl:template name="credit">
		<xsl:call-template name="chapter">
			<xsl:with-param name="title">
				Credits
			</xsl:with-param>
		</xsl:call-template>
			<xsl:apply-templates select="$cdm-credit-text"/><br></br>
	</xsl:template>

	<!-- these templates provide the synonomy -->

	<xsl:template match="synonymy" name="synonymy">
		<xsl:call-template name="chapter">
			<xsl:with-param name="title">
				Synonomy
			</xsl:with-param>
		</xsl:call-template>
		<!--<xsl:text>{{EDIT_Taxon|</xsl:text> -->
		<xsl:apply-templates select="../name">
			<xsl:with-param name="type">
				Taxon
			</xsl:with-param>
		</xsl:apply-templates>
		<!--<xsl:text>}}</xsl:text> -->
		<xsl:call-template name="wiki-newline" />
		<!-- getting nomenclatural refs and citaions for this name -->
		<xsl:call-template name="reference">
			<xsl:with-param name="reference-node" select="../name/nomenclaturalReference" />
		</xsl:call-template>
		<xsl:call-template name="citations">
			<xsl:with-param name="descriptionelements"
				select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements" />
			<xsl:with-param name="name-uuid" select="../name/uuid" />
		</xsl:call-template>
		<xsl:apply-templates select="homotypicSynonymsByHomotypicGroup" />
		<xsl:apply-templates select="heterotypicSynonymyGroups" />
		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />
		<!--xsl:call-template name="citations"> <xsl:with-param name="descriptionelements" 
			select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/> 
			<xsl:with-param name="name-uuid" select="../name/uuid"/> </xsl:call-template -->
	</xsl:template>

	<!--............................................. -->

	<xsl:template match="homotypicSynonymsByHomotypicGroup">
		<xsl:for-each select="e">
			<xsl:text>{{EDIT Homotypic Synonym|1|</xsl:text>
			<xsl:apply-templates select="name" />
			<xsl:text>}}</xsl:text>
			<xsl:call-template name="wiki-newline-comment" />
			<!--homotypicSynonymsByHomotypicGroup/e/name/nomenclaturalReference -->
			<!--xsl:apply-templates select="name/nomenclaturalReference"/ -->
			<xsl:for-each select="name/homotypicalGroup/typifiedNames/e">
				<xsl:call-template name="reference">
					<xsl:with-param name="reference-node" select="nomenclaturalReference" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:call-template name="reference">
				<xsl:with-param name="reference-node" select="name/nomenclaturalReference" />


			</xsl:call-template>

			<!--LORNA Pass the description elements for the citation 99b2842f-9aa7-42fa-bd5f-7285311e0101 -->
			<xsl:call-template name="citations">
				<xsl:with-param name="descriptionelements"
					select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements" />
				<xsl:with-param name="name-uuid" select="name/uuid" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<!--............................................. -->

	<xsl:template match="heterotypicSynonymyGroups">
		<xsl:for-each select="e">
			<!-- take the first one to printout as the head of the homotypic group -->
			<xsl:variable name="first-element" select="e[1]" />
			<xsl:text>{{EDIT Heterotypic Synonym|1|</xsl:text>
			<xsl:apply-templates select="$first-element/name" />
			<xsl:text>}}</xsl:text>

			<xsl:call-template name="wiki-newline-comment" />
			<xsl:call-template name="reference">
				<xsl:with-param name="reference-node"
					select="$first-element/name/nomenclaturalReference" />
			</xsl:call-template>

			<xsl:for-each select="name/homotypicalGroup/typifiedNames/e">
				<xsl:call-template name="reference">
					<xsl:with-param name="reference-node"
						select="$first-element/nomenclaturalReference" />
				</xsl:call-template>
			</xsl:for-each>

			<!-- take the first one to printout as the head of the homotypic group -->
			<!--do foreach for the rest -->
			<xsl:for-each select="e[position() &gt; 1]">
				<!--xsl:for-each select="e" -->
				<xsl:text>{{EDIT Homotypic Synonym|2|</xsl:text>
				<xsl:apply-templates select="name" />
				<xsl:text>}}</xsl:text>
				<xsl:call-template name="wiki-newline-comment" />
				<xsl:call-template name="reference">
					<xsl:with-param name="reference-node" select="name/nomenclaturalReference" />
				</xsl:call-template>

				<xsl:for-each select="name/homotypicalGroup/typifiedNames/e">
					<xsl:call-template name="reference">
						<xsl:with-param name="reference-node" select="nomenclaturalReference" />
					</xsl:call-template>
				</xsl:for-each>

				<!--LORNA Pass the description elements for the citation 99b2842f-9aa7-42fa-bd5f-7285311e0101 -->
				<xsl:call-template name="citations">
					<xsl:with-param name="descriptionelements"
						select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements" />
					<xsl:with-param name="name-uuid" select="name/uuid" />
				</xsl:call-template>
			</xsl:for-each>
		</xsl:for-each>
		<!--xsl:apply-templates select="e[1]/name[1]/homotypicalGroup[1]/typifiedNames[1]/e/taxonBases[1]/e/descriptions[1]/e/elements[1]/e[1]/media[1]/e/representations[1]/e/parts[1]/e/uri"></xsl:apply-templates -->
	</xsl:template>

	<!--............................................. -->

	<xsl:template match="name" name="name">
		<xsl:param name="type" />
		<xsl:apply-templates select="taggedName">
			<xsl:with-param name="type" select="$type" />
		</xsl:apply-templates>
		<!--xsl:apply-templates select="nomenclaturalReference"/ -->
	</xsl:template>

	<!--............................................. -->

	<xsl:template match="taggedName">
		<xsl:param name="type" />
		<xsl:choose>
			<xsl:when test="$type!=''">
				<xsl:value-of select="concat('{{EDIT_',normalize-space($type),'|')" />
				<xsl:for-each select="e">
					<xsl:choose>
						<xsl:when test="type='name'">
							<xsl:apply-templates select="text" />
							<xsl:text> </xsl:text>
						</xsl:when>
						<xsl:when test="type='authors'">
							<xsl:text>}}</xsl:text>
							<xsl:value-of
								select="concat('{{EDIT_',normalize-space($type),'_Author|')" />
							<xsl:apply-templates select="text" />
							<xsl:text>}}</xsl:text>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="e">
					<xsl:apply-templates select="text" />
					<xsl:text> </xsl:text>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>




		<!-- TODO mybe some types don't contain font tags, we don't have to call 
			the tmeplate -->
		<!-- we could just use <xsl:value-of select="text"/> -->


		<!--<xsl:text>&#xA;</xsl:text> -->
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!-- description features -->

	<!-- TODO: for first and second level: check if there is a page that describes 
		the feature and use Parameter "Link=pagename" with templates Tax_Feature 
		or Second_Level_Feature -->

	<xsl:template match="descriptions" name="descriptions">
		<xsl:call-template name="wiki-comment">
			<xsl:with-param name="wiki-comment">
				FEATURES
			</xsl:with-param>
		</xsl:call-template>
		<xsl:for-each select="features/feature">
			<xsl:choose>
				<xsl:when test="count(feature)!=0">
					<xsl:call-template name="secondLevelDescriptionElements" />
				</xsl:when>
				<xsl:otherwise>
					<!-- everything but Citation -->
					<xsl:if test="uuid!='99b2842f-9aa7-42fa-bd5f-7285311e0101'">
						<xsl:call-template name="descriptionElements" />
					</xsl:if>
					<!--xsl:apply-templates select="media/e/representations/e/parts/e/uri"/ -->
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>

	</xsl:template>

	<!--............................................. -->

	<xsl:template name="secondLevelDescriptionElements">

		<xsl:value-of
			select="concat('{{EDIT_Highlevel_Feature|',representation_L10n,'}}')" />
		<xsl:call-template name="wiki-newline" />
		<xsl:for-each select="feature">
			<!-- TODO assign TermBase description to parameter description- after 
				exporting it from CDM -->
			<xsl:if test="uuid!='6dfb4e78-c67e-410c-8989-c1fb1295abf6'">
				<xsl:value-of
					select="concat('{{EDIT_Nested_Feature|name=',representation_L10n,'|description=',representation_L10n,'|elements=')" />
				<xsl:call-template name="wiki-newline" />
				<!-- TODO create Elements -->
				<xsl:for-each select="descriptionelements/descriptionelement">
					<xsl:value-of
						select="concat('{{EDIT_Nested_Feature_Element|',multilanguageText_L10n/text, '}}')" />
					
					<xsl:choose>
						<xsl:when test="position() != last()">
							<xsl:text>{{EDIT_Delimiter}}</xsl:text>
						</xsl:when>
					</xsl:choose>
					<xsl:if test="position() != last()">
						<xsl:call-template name="wiki-newline" />
					</xsl:if>
				</xsl:for-each>
				<xsl:text>}}</xsl:text>
				<xsl:call-template name="wiki-newline" />
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="feature">
			<xsl:if test="uuid='6dfb4e78-c67e-410c-8989-c1fb1295abf6'">
				<xsl:call-template name="featureGallery" />
			</xsl:if>
		</xsl:for-each>
		<xsl:call-template name="wiki-newline" />

	</xsl:template>
	<!--............................................. -->


	<xsl:template name="descriptionElements">
		<xsl:choose>
			<xsl:when test="supportsCommonTaxonName='true'">
				<!-- must be Vernacular Name feature -->
				<xsl:call-template name="commonTaxonName" />
			</xsl:when>
			<xsl:otherwise>
				<!-- for example Habitat, Material Examined -->
				<xsl:call-template name="textData" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--............................................. -->

	<xsl:template name="commonTaxonName">

		<xsl:value-of
			select="concat('{{EDIT_Feature|name=',representation_L10n,'|elements=')" />
		<xsl:call-template name="wiki-newline" />
		<xsl:for-each select="descriptionelements/descriptionelement">
			<xsl:text />
			<xsl:value-of
				select="concat('{{EDIT_Common_Name|name=',name,'|language=',language/representation_L10n,'}}')" />
			<xsl:choose>
				<xsl:when test="position() != last()">
					<xsl:text>{{EDIT_Delimiter}}</xsl:text>
					<xsl:call-template name="wiki-newline" />
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
		<xsl:text>}}</xsl:text>
		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />
	</xsl:template>

	<!--............................................. -->

	<xsl:template name="textData">
		
		<xsl:value-of
			select="concat('{{EDIT_Feature|name=',representation_L10n, '|elements=')" />
		<xsl:call-template name="wiki-newline" />
		<xsl:text>{{EDIT_Feature_Text|</xsl:text>
		<!-- <xsl:choose> <xsl:when test="uuid!='9fc9d10c-ba50-49ee-b174-ce83fc3f80c6'"> -->
		<!-- feature is not "Distribution" -->
		<xsl:apply-templates
			select="descriptionelements/descriptionelement[1]/multilanguageText_L10n/text" />
		<xsl:call-template name="reference">
			<xsl:with-param name="reference-node" select="descriptionelements/descriptionelement[1]/sources[1]/e[1]/citation[1]" />
		</xsl:call-template>
		(
		<xsl:apply-templates
			select="descriptionelements/descriptionelement[1]/sources[1]/e[1]/citation[1]/titleCache[1]" />
		)
		
		<!-- </xsl:when> </xsl:choose> -->
		<xsl:text>}}}}</xsl:text>
		<!-- feature is "Figures -->
		<!-- A Gallery: uncomment following line -->
		<!--<xsl:apply-templates select="descriptionelements/descriptionelement[1]/multilanguageText_L10n/text"/> -->
		<!-- A Gallery: comment following line -->
		<!--xsl:call-template name="gallery"/ -->

		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />
		<!-- LORNA TRY IMAGE HERE -->
		<!--xsl:apply-templates select="descriptionelements/descriptionelement[1]/media/e/representations/e/parts/e/uri"/ -->


	</xsl:template>

	<!--............................................. -->


	<xsl:template match="text">
		<xsl:call-template name="replace-tags-others">
			<xsl:with-param name="text-string" select="." />
		</xsl:call-template>
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- image galleries -->

	<xsl:template name="gallery">
		<!-- A change to Figure Gallery: delete/comment following line -->
		<xsl:text>{{EDIT_Gallery|files=</xsl:text>
		<!-- A change to Figure Gallery: uncomment following line -->
		<!--<xsl:text>{{EDIT_Figure_Gallery|files=</xsl:text> -->
		<xsl:call-template name="wiki-newline" />
		<xsl:apply-templates select="../media/e/representations/e/parts/e/uri" />

		<xsl:text>}}</xsl:text>
		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />
	</xsl:template>


	<xsl:template name="featureGallery">

		<xsl:value-of
			select="concat('{{EDIT_Nested_Feature|name=',representation_L10n,'|description=',representation_L10n,'|elements=}}')" />
		<xsl:text>{{EDIT_Figure_Gallery|files=</xsl:text>
		<xsl:text />
		<xsl:call-template name="wiki-newline" />
		<xsl:apply-templates select=".//media/e/representations/e/parts/e/uri" />

		<xsl:text>}}</xsl:text>
		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />

		<xsl:call-template name="gallery_file" />

	</xsl:template>


	<xsl:template name="gallery_file" />

	<xsl:template match="media/e/representations/e/parts/e/uri">

		<xsl:value-of
			select="concat('{{EDIT_Gallery_File|filename=',functx:substring-after-last(.,'/'), '|description=')" />
		<!--go back up to the description element and get the text for the Figure 
			legend -->
		<xsl:apply-templates select="../../../../../../../multilanguageText_L10n/text" />
		<xsl:apply-templates select="../../../../../../../name/titleCache" />
		<xsl:text>}}</xsl:text>
		<xsl:if test="position() != last()">
			<xsl:call-template name="wiki-newline" />
		</xsl:if>
	</xsl:template>




	<!--+++++++++++++++++++++++++++++L A Y O U T ++++++++++++++++++++++++++++++++++++++ -->


	<!-- change here to change the look of the mediawiki output -->
	<!-- please use mediawiki templates -->
	<!-- think also of template changes in the mediawiki -->

	<xsl:template name="TOC">
		{{EDIT_TOC}}
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<xsl:template name="chapter">
		<xsl:param name="title" />
		<xsl:value-of select="concat('{{EDIT_Section|title=',$title,'}}')" />
		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<xsl:template name="subchapter">
		<xsl:param name="title" />
		<xsl:value-of select="concat('{{EDIT_Subsection|title=',$title,'}}')" />
		<xsl:call-template name="wiki-newline" />
		<xsl:call-template name="wiki-newline" />
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
	<!-- puts in a wiki comment -->
	<xsl:template name="wiki-comment">
		<xsl:param name="wiki-comment" />
		<xsl:comment>
			<xsl:value-of select="concat($wiki-comment,'&#xa;')" />
		</xsl:comment>
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!--this template layouts and displays the name of a taxon depending on 
		it's "kind" MAYDO: create more useful layouts instead of title sizes -->
	<xsl:template name="display-taxon-name">
		<xsl:param name="taxon" />
		<!-- get the name of the taxon -->
		<xsl:variable name="name">
			<xsl:call-template name="title">
				<xsl:with-param name="taxon" select="$taxon" />
			</xsl:call-template>
		</xsl:variable>
		<!-- format the name of the taxon according to it's kind -->
		<xsl:choose>
			<!-- family -->
			<xsl:when
				test="$taxon/name/rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
				<xsl:value-of select="concat('{{EDIT_Family_Name|', $name, '}}')" />
			</xsl:when>
			<!-- genus -->
			<xsl:when
				test="$taxon/name/rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
				<xsl:value-of select="concat('{{EDIT_Genus Name|', $name, '}}')" />
			</xsl:when>
			<!--TODO -->
			<!-- subgenus -->
			<xsl:when
				test="$taxon/name/rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
				<xsl:value-of select="concat('{{EDIT_Subgenus Name|', $name, '}}')" />
			</xsl:when>
			<!-- species -->
			<xsl:when
				test="$taxon/name/rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
				<xsl:value-of select="concat('{{EDIT_Species Name|', $name, '}}')" />
			</xsl:when>
			<xsl:otherwise>
				<!-- for debugging -->
				Unformatted title for rank uuid:
				<xsl:value-of select="$taxon/name/rank/uuid" />
				:
				<xsl:value-of select="$taxon/name/titleCache" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

	<!--we replace "escaped" html <i> and <b> with mediawiki markup -->
	<!--............................................. -->

	<xsl:template name="replace-tags-others">
		<xsl:param name="text-string" />
		<!-- first replace bold tags and put the result in text-string2 -->
		<xsl:variable name="text-string2">
			<xsl:call-template name="add-markup">
				<xsl:with-param name="str" select="$text-string" />
				<xsl:with-param name="wiki-markup">
					'''
				</xsl:with-param>
				<xsl:with-param name="tag-name">
					b
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>

		<!-- second replace italic tags on text-string2 -->
		<xsl:variable name="text-string3">
			<xsl:call-template name="add-markup">
				<xsl:with-param name="str" select="$text-string2" />
				<xsl:with-param name="wiki-markup">
					''
				</xsl:with-param>
				<xsl:with-param name="tag-name">
					i
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
	

	<!-- third wrap = with <nowiki> tag -->
	<xsl:call-template name="wrap-a-sequence-with-tags">
	 <xsl:with-param name="str" select="$text-string3" />
		<xsl:with-param name="pattern">=</xsl:with-param>
		<xsl:with-param name="tag-name">nowiki</xsl:with-param>
	</xsl:call-template>
	</xsl:template>
	<!--............................................. -->

	<xsl:template name="add-markup">
		<xsl:param name="str" />
		<xsl:param name="wiki-markup" />
		<xsl:param name="tag-name" />

		<xsl:variable name="opening-tag">
			<xsl:value-of select="concat('&lt;', $tag-name, '&gt;')">
			</xsl:value-of>
		</xsl:variable>
		<xsl:variable name="closing-tag">
			<xsl:value-of select="concat('&lt;/', $tag-name, '&gt;')">
			</xsl:value-of>
		</xsl:variable>

		<xsl:choose>
			<!-- if the tag occures in the string -->
			<xsl:when
				test="contains($str, $opening-tag) and contains($str, $closing-tag)">

				<!-- separate string before, inside and after the tag -->
				<xsl:variable name="before-tag"
					select="substring-before($str, $opening-tag)" />
				<xsl:variable name="inside-tag"
					select="substring-before(substring-after($str,$opening-tag),$closing-tag)" />
				<xsl:variable name="after-tag"
					select="substring-after($str, $closing-tag)" />

				<!-- built the new string by putting in the mediawiki template -->
				<xsl:value-of
					select="concat($before-tag, $wiki-markup, $inside-tag, $wiki-markup)" />
				<!-- in the part after the closing tag could be more tag, so we do a 
					recursive call -->
				<xsl:call-template name="add-markup">
					<xsl:with-param name="str" select="$after-tag" />
					<xsl:with-param name="wiki-markup" select="$wiki-markup" />
					<xsl:with-param name="tag-name" select="$tag-name" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$str" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--............................................. -->
	
	<xsl:template name="wrap-a-sequence-with-tags">
		
		<!-- the string we operate on -->
		<xsl:param name="str" />
		<!-- the sequence we want to put tags arround e.g. "=" -->
		<xsl:param name="pattern"/>
		<!-- the tag name e.g. nowiki -->
		<xsl:param name="tag-name" />
		<!--xsl:value-of>###in wrap</xsl:value-of-->
		<xsl:variable name="new-pattern">
			<xsl:value-of select="concat('&lt;',$tag-name,'&gt;',$pattern,'&lt;/',$tag-name,'&gt;')">				
			</xsl:value-of>
		</xsl:variable>
		
		<xsl:value-of select="replace($str,$pattern, $new-pattern)"></xsl:value-of>
	</xsl:template>

	<!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->




</xsl:stylesheet>
