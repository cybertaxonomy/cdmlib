<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:http="http://expath.org/ns/http-client"
    version="1.0">


    <xsl:template match="/Statistics">

        <HTML>
            <HEAD>
                <script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"/>
                <link type="text/css" rel="stylesheet" media="all"
                    href="../css/vibrant.css"/>
            </HEAD>
            <BODY>
                <script type="text/javascript" language="javascript"/>
                <a href="/vibrant_index/search/vibrant_names.html" title="Home">
                    <img src="../css/acquia_prosper_logo.png" alt="Home"/>
                </a>

                <table width="100%">
                    <tr>
                        <td width="20%"/>
                        <td valign="top">

                            <h2>ViBRANT index database statistics</h2>
                            <br/>
                            <BR/>
                            <H3>Summary statistics: </H3>
                            <br/>

                            <table width="50%" cellspacing="2" cellpadding="2" 
                                style="border-collapse: collapse">
                                <xsl:for-each select="countMap/*">
                                    <tr>
                                        <td bgcolor="#FFCCCC" width="70%">
                                            <strong>
                                                <xsl:value-of select="name(.)"/>:
                                            </strong>
                                        </td>

                                        <td bgcolor="#FFE6E6">
                                            <xsl:apply-templates select="."/>
                                        </td>
                                    </tr>
                                </xsl:for-each>

                            </table>
                        </td>
                    </tr>
                </table>
            </BODY>
        </HTML>

    </xsl:template>



</xsl:stylesheet>
