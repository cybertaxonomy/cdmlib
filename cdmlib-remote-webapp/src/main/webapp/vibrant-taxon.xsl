<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- pass this in as a parameter? -->
    <xsl:variable name="url_portal"> http://160.45.63.151:8080/cichorieae/ </xsl:variable>
    <xsl:variable name="url_webservice">http://localhost:8080/taxon/findTaxaAndNames</xsl:variable>

    <xsl:template match="/DefaultPagerImpl">
        <HTML>
            <HEAD>
                <link type="text/css" rel="stylesheet" media="all" href="/vibrant.css"/>
            </HEAD>
            <BODY>
                <a href="/vibrant_names.html" title="Home">
                    <img src="http://localhost:8080/acquia_prosper_logo.png" alt="Home"/>
                </a>
                <br/>
                <table cellpadding="1" width="100%">
                    <tr>
                        <td width="5%"/>
                        <td valign="top">
                            
                            <h2>ViBRANT Common Data Model names search</h2>
                            <br></br>
                            <BR></BR>
                            <H3>Taxon:</H3>
                            <br></br>

                            <TABLE border="0" frame="1">
                                <xsl:for-each select="records/e">
                                    <xsl:variable name="url">
                                        <xsl:value-of select="$url_portal"/>name/<xsl:value-of
                                            select="uuid"/>
                                    </xsl:variable>

                                    <TR>
                                        <TD>Title and reference: </TD>
                                        <TD>
                                            <xsl:apply-templates select="titleCache"/>
                                        </TD>
                                    </TR>
                                    <TR>
                                        <TD>UUID: </TD>
                                        <TD>
                                            <xsl:apply-templates select="uuid"/>
                                        </TD>
                                    </TR>
                                </xsl:for-each>
                            </TABLE>

                        </td>
                    </tr>
                </table>
            </BODY>
        </HTML>
    </xsl:template>

    <xsl:template match="titleCache">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$url_portal"/>name/<xsl:value-of
                    select="../uuid"/></xsl:attribute>
            <xsl:value-of select="."/>
        </a>
    </xsl:template>


    <!--xsl:template match=".">
        <xsl:value-of select="text()"/>
    </xsl:template-->

</xsl:stylesheet>
