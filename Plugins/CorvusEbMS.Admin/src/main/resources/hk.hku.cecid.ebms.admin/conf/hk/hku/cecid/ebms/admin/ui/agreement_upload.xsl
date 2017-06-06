<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>
<xsl:template match="/partnership">


<table border="0" cellpadding="2" cellspacing="2" width="100%">
  <tr>
    <th colspan="2">Collaboration-Protocol Profile and Agreement Upload</th>
  </tr>

  <form enctype='multipart/form-data' name="agreementUploadForm" method="post" action="agreement_upload" onSubmit="return confirm('Are you sure to upload the agreement?');">
  <tr>
    <th colspan="2"/>
  </tr>
  <tr>
    <td width="20%">Party Name</td>
    <td width="80%">
    <input type="text">
        <xsl:attribute name="name">party_name</xsl:attribute>
        <xsl:attribute name="value"></xsl:attribute>
    </input>
    </td>
  </tr>
      <tr>
          <td width="20%">Transport Endpoint Override</td>
          <td width="80%">
              <input type="text">
                  <xsl:attribute name="name">transport_endpoint</xsl:attribute>
                  <xsl:attribute name="value"></xsl:attribute>
              </input>
          </td>
      </tr>
  <tr>
    <td width="20%">File Upload</td>
    <td width="80%">
    <input type="file">
        <xsl:attribute name="name">cpa</xsl:attribute>
        <xsl:attribute name="value"></xsl:attribute>
    </input>
    </td>
  </tr>
  <tr>
      <td width="20%">Certificate For Verification</td>
      <td width="80%">
          <input type="file">
              <xsl:attribute name="name">verify_cert</xsl:attribute>
              <xsl:attribute name="value"></xsl:attribute>
          </input>
      </td>
  </tr>
  <tr>
      <td width="20%">Certificate For Encryption</td>
      <td width="80%">
          <input type="file">
              <xsl:attribute name="name">encrypt_cert</xsl:attribute>
              <xsl:attribute name="value"></xsl:attribute>
          </input>
      </td>
  </tr>
  <tr>
    <td width="20%"></td>
    <td width="80%">
    <input type="Submit" name="action" value="Import"/>
    </td>
  </tr>
  </form>

<!-- The blank field - end -->
  
</table>

<xsl:if test="count(partnership)>0">
<br/>
<table border="0" cellpadding="2" cellspacing="2" width="100%">
  <tr>
    <th colspan="2">Added <xsl:value-of select="count(partnership[./agreement_added='true'])" /> Partnership(s)</th>
  </tr>
  <!-- Loop the existing delivery channels - start -->
  <xsl:for-each select="partnership">
      <xsl:if test="./agreement_added = 'false'">
          <tr>
              <th colspan="2"/>
          </tr>
          <tr>
              <td width="20%"><b>Partnership ID</b></td>
              <td width="80%"><b><xsl:value-of select="./partnership_id" /></b> already exists</td>
          </tr>
      </xsl:if>
      <xsl:if test="./agreement_added = 'true'">
      <tr>
        <th colspan="2"/>
      </tr>
      <tr>
        <td width="20%"><b>Partnership ID</b></td>
        <td width="80%"><b><xsl:value-of select="./partnership_id" /></b></td>
      </tr>
      <tr>
        <td width="20%"><b>CPA ID</b></td>
        <td width="80%"><b><xsl:value-of select="./cpa_id" /></b></td>
      </tr>
      <tr>
        <td width="20%"><b>Service</b></td>
        <td width="80%"><b><xsl:value-of select="./service" /></b></td>
      </tr>
      <tr>
        <td width="20%"><b>Action</b></td>
        <td width="80%"><b><xsl:value-of select="./action_id" /></b></td>
      </tr>
      <tr>
        <td width="20%">Transport Protocol</td>
        <td width="80%"><xsl:value-of select="./transport_protocol" /></td>
      </tr>
        <tr>
        <td width="20%">Transport Endpoint</td>
        <td width="80%"><xsl:value-of select="./transport_endpoint" /></td>
      </tr>
      <tr>
        <td width="20%">Sync Reply Mode</td>
        <td width="80%"><xsl:value-of select="./sync_reply_mode" /></td>
      </tr>
      <tr>
        <td width="20%">Acknowledgement Requested</td>
        <td width="80%"><xsl:value-of select="./ack_requested" /></td>
      </tr>
      <tr>
        <td width="20%">Acknowledgement Signed Requested</td>
        <td width="80%"><xsl:value-of select="./ack_sign_requested" /></td>
      </tr>
      <tr>
        <td width="20%">Duplicate Elimination</td>
        <td width="80%"><xsl:value-of select="./dup_elimination" /></td>
      </tr>
      <tr>
        <td width="20%">Actor</td>
        <td width="80%"><xsl:value-of select="./actor" /></td>
      </tr>
      <tr>
        <td width="20%">Maximum Retries</td>
        <td width="80%"><xsl:value-of select="./retries" /></td>
      </tr>
      <tr>
        <td width="20%">Retry Interval (ms)</td>
        <td width="80%"><xsl:value-of select="./retry_interval" /></td>
      </tr>
      <tr>
        <td width="20%">Persist Duration</td>
        <td width="80%"><xsl:value-of select="./persist_duration" /></td>
      </tr>
      <tr>
        <td width="20%">Message Order</td>
        <td width="80%"><xsl:value-of select="./message_order" /></td>
      </tr>
      <tr>
        <td width="20%">Sign Requested</td>
        <td width="80%"><xsl:value-of select="./sign_requested" /></td>
      </tr>
      <xsl:if test="''!=./ds_algorithm">
          <tr>
            <td width="20%">Digital Signature Algorithm</td>
            <td width="80%"><xsl:value-of select="./ds_algorithm" /></td>
          </tr>
      </xsl:if>
      <xsl:if test="''!=./md_algorithm">
          <tr>
            <td width="20%">Message Digest Algorithm</td>
            <td width="80%"><xsl:value-of select="./md_algorithm" /></td>
          </tr>
      </xsl:if>
      <tr>
        <td width="20%">Encrypt Requested</td>
        <td width="80%"><xsl:value-of select="./encrypt_requested" /></td>
      </tr>
      <xsl:if test="''!=./encrypt_algorithm">
          <tr>
            <td width="20%">Encrypt Algorithm</td>
            <td width="80%"><xsl:value-of select="./encrypt_algorithm" /></td>
          </tr>
      </xsl:if>
      <tr>
        <td width="20%">Disabled</td>
        <td width="80%"><xsl:value-of select="./disabled" /></td>
      </tr>
      </xsl:if>
  
  </xsl:for-each>
  
<!-- Loop the existing delivery channels - end -->
</table>
<br/>
</xsl:if>

</xsl:template>
</xsl:stylesheet>