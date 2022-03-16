package xades4j.production;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;

import org.apache.xml.security.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tbai4j.TbaiFactura;
import tbai4j.TbaiSignPolicy;
import tbai4j.TbaiXMLUtils;
import xades4j.properties.ObjectIdentifier;
import xades4j.providers.CertificateValidationProvider;
import xades4j.providers.SignaturePolicyDocumentProvider;
import xades4j.providers.impl.PKIXCertificateValidationProvider;
import xades4j.utils.FileSystemDirectoryCertStore;
import xades4j.verification.SignatureSpecificVerificationOptions;
import xades4j.verification.XAdESForm;
import xades4j.verification.XAdESVerificationResult;
import xades4j.verification.XadesVerificationProfile;

public class TbaiXadesEpesVerify {

	private static final Logger logger = LoggerFactory.getLogger(TbaiXadesEpesVerify.class);
	
	 private static String DEF_JAVA_KEYSTORE_PATH="C:\\java\\jdk-11.0.2\\lib\\security";
	 private static String DEF_JAVA_KEYSTORE_FILE="C:\\java\\jdk-11.0.2\\lib\\security\\cacerts_prueba";	
	 private static String DEF_JAVA_KEYSTORE_PASS="changeit";
	 
	 private static String JAVA_KEYSTORE_PATH;
	 private static String JAVA_KEYSTORE_FILE;	
	 private static String JAVA_KEYSTORE_PASS;
	 
	private static Element getSigElement(Document doc) throws Exception
    {
        return (Element)doc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATURE).item(0);
    }
	private static XAdESVerificationResult verifySignature(
            String sigFileName,
            XadesVerificationProfile p,
            SignatureSpecificVerificationOptions options) throws Exception
    {
        Element signatureNode = getSigElement(TbaiXMLUtils.getDocument(sigFileName));
        return p.newVerifier().verify(signatureNode, options);
    }
	
	
	private static KeyStore createAndLoadJKSKeyStore(String path, String pwd) throws Exception {
  
        FileInputStream fis = new FileInputStream(path);
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(fis, pwd.toCharArray());
        fis.close();
        return ks;
    }
		 
	public static XAdESVerificationResult verifySignature(String sigFileName,TbaiSignPolicy policy) throws Exception
    {
		
		 FileSystemDirectoryCertStore certStore = new FileSystemDirectoryCertStore(JAVA_KEYSTORE_PATH);
         KeyStore ks = createAndLoadJKSKeyStore(JAVA_KEYSTORE_FILE, JAVA_KEYSTORE_PASS);
        
         CertificateValidationProvider certValidationProvider = PKIXCertificateValidationProvider.builder(ks)
                 .checkRevocation(false)
                 .intermediateCertStores(certStore.getStore())
                 .build();
         
        XadesVerificationProfile verificationProfile=new XadesVerificationProfile(certValidationProvider);
        
        //Validar el documento en local con el pdf de la politítica
        SignaturePolicyDocumentProvider policyDocumentFinder;
        policyDocumentFinder = new SignaturePolicyDocumentProvider()
        {
            @Override
            public InputStream getSignaturePolicyDocumentStream(ObjectIdentifier sigPolicyId) {            	
            	try {
						return new FileInputStream(policy.getPolicyFile());
				} catch (FileNotFoundException e) {
					logger.error("getSignaturePolicyDocumentStream",e);
				}
				return null;				 
            }
        };
        
        verificationProfile.withPolicyDocumentProvider(policyDocumentFinder);
        XAdESVerificationResult result= verifySignature(sigFileName,verificationProfile, null);
        XAdESForm form=result.getSignatureForm();
        logger.info("verifySignature OK. {}: {}",sigFileName,form.toString());
        return result;
    }
	
	 public static void main(String[] args) {
    	 try {  
    		 if(null==args || args.length<=0){
    			 logger.error("Falta parámetro xml firmado");
    			 System.exit(1);;
    		 }
    		String TBAI_XML_SIG_PATH=args[0];    			 
    		Properties props = new Properties();			
 			props.load( TbaiFactura.class.getClassLoader().getResourceAsStream("tbai4j.properties"));
    		
			JAVA_KEYSTORE_PATH=props.getProperty("JAVA_KEYSTORE_PATH",DEF_JAVA_KEYSTORE_PATH);
			JAVA_KEYSTORE_FILE=props.getProperty("JAVA_KEYSTORE_FILE",DEF_JAVA_KEYSTORE_FILE);	
			JAVA_KEYSTORE_PASS=props.getProperty("JAVA_KEYSTORE_PASS",DEF_JAVA_KEYSTORE_PASS);
			
    		TbaiSignPolicy tbaipolicy=new TbaiSignPolicy();
    		tbaipolicy.setPolicyFile(props.getProperty("TBAI_POLICY_FILE"));
			tbaipolicy.setPolicyUrl(props.getProperty("TBAI_POLICY_URL"));
    		XAdESVerificationResult result = TbaiXadesEpesVerify.verifySignature(TBAI_XML_SIG_PATH,tbaipolicy);
    		XAdESForm form=result.getSignatureForm();
    
			logger.info("Firma OK. {}",form.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error validadndo firma",e);
			//e.printStackTrace();
		}
    }

}
