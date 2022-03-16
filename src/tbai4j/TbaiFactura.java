package tbai4j;

import org.w3c.dom.Document;

import tbai4j.cert.TbaiCertImplPkcs11;
import tbai4j.cert.TbaiCertImplPkcs12;
import xades4j.production.TbaiXadesEpesSign;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TbaiFactura {

	private static final Logger logger = LoggerFactory.getLogger(TbaiFactura.class);
	
	
		
	public static void main(String[] args) {
	
		try {			
			
			Properties props = new Properties();			
			props.load( TbaiFactura.class.getClassLoader().getResourceAsStream("tbai4j.properties"));
			
			String XML_SIG_EXTENSION=props.getProperty("XML_SIG_EXTENSION",".xsig");
			String PKCS12_FILE=props.getProperty("PKCS12_FILE");
			String PKCS12_PASSWORD=props.getProperty("PKCS12_PASSWORD");
			String TBAI_POST_URL=props.getProperty("TBAI_POST_URL");
			String TBAI_POLICY_URL=props.getProperty("TBAI_POLICY_URL");
			String TBAI_POLICY_FILE=props.getProperty("TBAI_POLICY_FILE");
			String TBAI_QR_URL=props.getProperty("TBAI_QR_URL");
			
			if(null==args || args.length<=0) {
				logger.error("Falta parámetro fichero xml a firmar");
				System.exit(1);
			}
			String TBAI_FACT_XML_FILE=args[0];
			
			File file = new File(TBAI_FACT_XML_FILE);
		    if (!file.exists()) {
		    	logger.error("Fichero inexistente: {}",TBAI_FACT_XML_FILE);
		    	System.exit(1);
		    }
			
			
			String tabai_path = file.getParent();
			String tbai_fact_file=file.getName();
			int punto=tbai_fact_file.lastIndexOf('.');
			if(punto>-1) {
				tbai_fact_file=tbai_fact_file.substring(0,punto);
			}
			
			String TBAI_FACT_XML_FILE_SIG=tabai_path+File.separator+tbai_fact_file+XML_SIG_EXTENSION;
			String TBAI_QR_IMAGE_FILE=tabai_path+File.separator+tbai_fact_file+".png";
			String TBAI_ID_FILE=tabai_path+File.separator+tbai_fact_file+".txt";									
			
			//Certificado
			TbaiCertImplPkcs12 tbaicert12=new TbaiCertImplPkcs12();
			tbaicert12.setPath(PKCS12_FILE);
			tbaicert12.setPassword(PKCS12_PASSWORD);			
			
			//TbaiCertImplPkcs11 tbaicert11=new TbaiCertImplPkcs11();
			//tbaicert11.setPkcs11Lib(props.getProperty("PKCS11_LIB_PATH"));
			//tbaicert11.setAlias(props.getProperty("PKCS11_ALIAS"));
			//tbaicert11.setPassword(props.getProperty("PKCS11_PASSWORD"));
			
			//Política de firma de la diputación de Álava
			TbaiSignPolicy tbaipolicy=new TbaiSignPolicy();
			tbaipolicy.setPolicyFile(TBAI_POLICY_FILE);
			tbaipolicy.setPolicyUrl(TBAI_POLICY_URL);
			
			//Firmar documento y guardar					
			Document docsig=TbaiXadesEpesSign.sign(TBAI_FACT_XML_FILE,tbaipolicy, tbaicert12);
			TbaiXMLUtils.outputDocument(docsig, TBAI_FACT_XML_FILE_SIG);

			//Cargar el documento firmado y generar el id y qr
			TbaiID tbaiid=new TbaiID(docsig);
			tbaiid.setUrl(TBAI_QR_URL);
			logger.info("NIF: {} Serie: {}, NumFact: {}, Fecha: {} Importe: {}",
					tbaiid.getNif(),
					tbaiid.getSerie(),
					tbaiid.getNumfact(),
					tbaiid.getFecha(),
					tbaiid.getImporte());
			logger.info("Firma: {}",tbaiid.getFirma());
			
			Properties tbai_props=new Properties();
			
			
			//tbaiid.writeIDtoFile(TBAI_ID_FILE);
			String id=tbaiid.getTbaiID();	
			String qrtext=tbaiid.getUrlQR(id);
			
			
			logger.info("TBAI ID: {}",id);
			logger.info("QR Text: {}",qrtext);
			
			TbaiQR.generateQRcode(qrtext, TBAI_QR_IMAGE_FILE);		
			System.out.println("OK: "+TBAI_FACT_XML_FILE_SIG);
			
			
			tbai_props.setProperty("tbaiid",id);
			tbai_props.setProperty("tbaiurl",qrtext);
			tbai_props.setProperty("firma", tbaiid.getFirma());
			tbai_props.setProperty("pathpng",TBAI_QR_IMAGE_FILE);
			tbai_props.setProperty("pathxml",TBAI_FACT_XML_FILE);
			tbai_props.setProperty("pathxmlsig",TBAI_FACT_XML_FILE_SIG);
			
			
			//Enviar documento firmado.
			TbaiPost tbaipost=new TbaiPost();
			tbaipost.postPkcsSignedXML(tbaicert12,TBAI_POST_URL,TBAI_FACT_XML_FILE_SIG);
			
			logger.info("POST Response: {}",tbaipost.getResponseXml());
			
			tbai_props.setProperty("status",""+tbaipost.getStatus());
			tbai_props.setProperty("responseXml",tbaipost.getResponseXml());
			
			tbaiid.writetoFile(TBAI_ID_FILE, tbai_props);
			
		} catch (Exception e) {
			logger.error("Error en factura",e);
			System.out.println("ERROR!!!");
			e.printStackTrace();
		}
		
		
	}

}
