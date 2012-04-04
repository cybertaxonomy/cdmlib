<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- pass this in as a parameter? -->

    <xsl:variable name="url_webservice">../taxon/findTaxaAndNames</xsl:variable>
    <xsl:variable name="url_start">"../taxon/</xsl:variable>
    <xsl:variable name="url_end">/extensions.json"</xsl:variable>


    <xsl:template match="/DefaultPagerImpl">
        <HTML>

            <HEAD>
                <script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"/>
                <link type="text/css" rel="stylesheet" media="all" href="../vibrant.css"/>
            </HEAD>
            <BODY>


                <script type="text/javascript" language="javascript">               

        $(document).ready(function() {

        $('.source-url').each(function(index) {
            
            var fullurl = '/vibrant_index/taxon/' + $(this).attr('ref') + '/extensions.json';
            //$(this).html(fullurl);
            var source_url = 'lorna testing';
            $.ajax({
                url: fullurl,
                dataType: "jsonp",
                async: false, // only for debugging, let's you see more error messages
                cache: false, // browser will not cache
                success: function(data) {
                    
                    var dataRecords = data.records;
                    $(this).html(data.pageSize);
            
                    $.each(data.records, function(i,record){
                    
                    var value = record.value;
                    var ch = 'http';
                    if ( value.indexOf(ch) === 0 ) {

                        source_url = value;
                        }
                    });
                    },
                error: function(XMLHttpRequest, statusText, errorThrown){
                                     $("#content").html("ERROR: " + statusText + " - " + errorThrown);
                             }
                                                        
            });


            $(this).html('&lt;a href="' + source_url + '"&gt;' + source_url + '&lt;/a&gt;');
            

            });           
            });       
            </script>



                <a href="../vibrant_names.html" title="Home">
                    <img src="../acquia_prosper_logo.png" alt="Home"/>
                </a>
                <br/>
                <table cellpadding="1" width="100%">
                    <tr>
                        <td width="5%"/>
                        <td valign="top">

                            <h2>ViBRANT Common Data Model names search</h2>
                            <br/>
                            <BR/>
                            <H4>Source(s) using this name: </H4>
                            <br/>


                            <xsl:for-each select="records/e">
                                <table width="35%" border="1" cellspacing="4" cellpadding="0"
                                    bordercolor="#666666" style="border-collapse: collapse">

                                    <tr>
                                        <TABLE border="0">

                                            <TR>
                                                <TD>
                                                  <strong>Name:</strong>
                                                </TD>
                                                <TD>
                                                  <xsl:apply-templates select="titleCache"/>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD>
                                                  <strong>Status: </strong>
                                                </TD>
                                                <TD>
                                                  <xsl:variable name="taxonclass">
                                                  <xsl:value-of select="class"/>
                                                  </xsl:variable>
                                                  <xsl:choose>
                                                  <xsl:when test="$taxonclass = 'Taxon'">
                                                  <font color="green">
                                                  <strong>
                                                  <xsl:apply-templates select="class"/>
                                                  </strong>
                                                  </font>
                                                  </xsl:when>
                                                  <xsl:otherwise>
                                                  <font color="purple">
                                                  <strong>
                                                  <xsl:apply-templates select="class"/>
                                                  </strong>
                                                  </font>
                                                  </xsl:otherwise>
                                                  </xsl:choose>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD>
                                                  <strong>Source: </strong>
                                                </TD>
                                                <TD>
                                                  <xsl:apply-templates select="sec/titleCache"/>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD>
                                                  <strong>UUID: </strong>
                                                </TD>
                                                <TD>
                                                  <xsl:apply-templates select="uuid"/>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD> </TD>
                                                <xsl:variable name="uuid">
                                                  <xsl:value-of select="uuid"/>
                                                </xsl:variable>

                                                <TD class="source-url" ref="{$uuid}">

                                                  <a>
                                                  <xsl:attribute name="href">
                                                  <div id="content"> </div>
                                                  </xsl:attribute>
                                                  <div id="content"> </div>
                                                  </a>
                                                </TD>
                                            </TR>
                                        </TABLE>
                                        <TABLE> </TABLE>
                                    </tr>
                                </table>
                                <br/>
                            </xsl:for-each>
                        </td>
                    </tr>
                </table>
            </BODY>
        </HTML>
    </xsl:template>



    <!--xsl:template match=".">
        <xsl:value-of select="text()"/>
    </xsl:template-->

</xsl:stylesheet>
