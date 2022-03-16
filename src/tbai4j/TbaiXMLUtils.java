package tbai4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import xades4j.utils.DOMHelper;

public class TbaiXMLUtils {
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	static {
		System.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");
		NamespaceContext ds = new NamespaceContext() {
		    public String getNamespaceURI(String prefix) {
		    	if("ds".equals(prefix)) {
		        return "http://www.w3.org/2000/09/xmldsig#"; 
		    	}
		    	return null;
		        
		    }
		    public Iterator<String>  getPrefixes(String val) {
		        return null;
		    }
		    public String getPrefix(String uri) {
		        return null;
		    }
		};
		xPath.setNamespaceContext(ds);
	}
	
	public static void outputDocument(Document doc, String fileName) throws Exception
    {       
        FileOutputStream out = new FileOutputStream(new File(fileName));
        try {
        	TransformerFactory tff = TransformerFactory.newInstance();
        	Transformer tf=tff.newTransformer();
        	tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
        	tf.transform(new DOMSource(doc), new StreamResult(out));
        } finally {
            out.close();
        }

    }
	
	public static Document parseDocument(InputStream is) throws Exception{
		DocumentBuilder db;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);
		// Apache Santuario now uses Document.getElementById; use this convention for tests.
		Element elem = doc.getDocumentElement();
		DOMHelper.useIdAsXmlId(elem);
		return doc;
	}
	
	public static Document getDocument(String fileName) throws Exception{
		FileInputStream fis=null;
        try {
        	fis = new FileInputStream(fileName);
        	DocumentBuilder db;
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		dbf.setNamespaceAware(true);
    		db = dbf.newDocumentBuilder();
    		Document doc = db.parse(fis);
    		// Apache Santuario now uses Document.getElementById; use this convention for tests.
    		Element elem = doc.getDocumentElement();
    		DOMHelper.useIdAsXmlId(elem);
    		return doc;
        } finally {
        	if(null!=fis)
        		fis.close();
        }
    }
	
	public static String getNodeTextContent(Object doc, String tag) throws XPathExpressionException{
		XPathExpression expr = xPath.compile(tag);
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		String ret=nodes.item(0).getTextContent(); 
		return ret;
	}
	
	public static void convertToUTF8(String filein, String fileout) throws IOException {
		FileInputStream fis = new FileInputStream(filein);
        InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
        Reader in = new BufferedReader(isr);
        FileOutputStream fos = new FileOutputStream(fileout);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        Writer out = new BufferedWriter(osw);

        int ch;
        while ((ch = in.read()) > -1) {
            out.write(ch);
        }

        out.close();
        in.close();
	}
	

}
