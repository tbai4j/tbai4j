package xades4j.production;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tbai4j.cert.TbaiCert;
import tbai4j.TbaiSignPolicy;
import tbai4j.TbaiXMLUtils;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SignaturePolicyInfoProvider;


public class TbaiXadesEpesSign {
	
	private static final Logger logger = LoggerFactory.getLogger(TbaiXadesEpesSign.class);
	
	public static Document sign(String ficXML,TbaiSignPolicy policy,TbaiCert cert) throws Exception {
		logger.trace("Firma documento: {}",ficXML);
		Document doc = TbaiXMLUtils.getDocument(ficXML); 
		return sign(doc,policy, cert);
	}
		
	public static Document sign(Document doc,TbaiSignPolicy policy,TbaiCert cert) throws Exception {
		
		logger.trace("Firmando...");
		
		KeyingDataProvider keyingDataProvider = cert.getKeyingDataProvider();
		
		Element elemToSign = doc.getDocumentElement();
		      
		SignaturePolicyInfoProvider policyInfoProvider = policy.getSignaturePolicyInfoProvider();
		
		SignerEPES signer = (SignerEPES) new XadesEpesSigningProfile(keyingDataProvider, policyInfoProvider).newSigner();
		new Enveloped(signer).sign(elemToSign);
		logger.info("XML Tbai firmado");
		
		return doc;
        
	}
	
}
