package tbai4j;

import java.io.FileInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tbai4j.cert.TbaiCert;

public class TbaiPost {
	private static final Logger logger = LoggerFactory.getLogger(TbaiPost.class);
	
	private static String HEADER_CONTENT_TYPE="application/xml;charset=UTF-8";
	private int status=-1;
	private String statusLine="";
	private String responseXml="";
	
	public int getStatus() {
		return this.status;
	}
	public String getStatusLine() {
		return this.statusLine;
	}
	public String getResponseXml() {
		return this.responseXml;
	}
		
	
		
	public  int postPkcsSignedXML(TbaiCert cert,String URL,String ficXMLSig) throws Exception {
		SSLConnectionSocketFactory sslConnectionSocketFactory=cert.getPkcsConFactory();
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
	    HttpPost req = new HttpPost(URL);
	    req.setHeader(HttpHeaders.CONTENT_TYPE,HEADER_CONTENT_TYPE);
	    HttpEntity ent = new InputStreamEntity(new FileInputStream(ficXMLSig));
	    req.setEntity(ent);
	    CloseableHttpResponse response = httpclient.execute(req);
	    HttpEntity entity = response.getEntity();
	    this.status=response.getStatusLine().getStatusCode();
	    this.statusLine=response.getStatusLine().toString();
	    
	    logger.info("POST Response status: {} URL: {} XMLSig: {} ",this.statusLine,URL, ficXMLSig);
	   
	    this.responseXml = EntityUtils.toString(entity);
	    EntityUtils.consume(entity);
	    
	    return this.status;
	}
	

}
