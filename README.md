# tbai4j
Ejemplo java de firma y envío de facturas en formato  Xades Epes Enveloped para proyecto TicketBAI.

Basado en proyecto xades4j para firma y gestión de certificados.
https://github.com/luisgoncalves/xades4j

Se utiliza un certificado de dispositivo PKCS12 generado por IZENPE.  
Pueden utilizarse certificados PKCS11 en tarjeta. En este caso se utiliza OpenSC  

## Fichero de propiedades utilizado: tbai4j.properties
```
#PKCS12
#Path y password del certificado p12 de dispositivo
PKCS12_FILE=/ticketbay/tbai_cert.p12
PKCS12_PASSWORD=

#PKCS11
#Path libreria proveedor nativo
PKCS11_LIB_PATH=C:\\java\\OpenSC\\OpenSC\\pkcs11\\opensc-pkcs11.dll
#Alias del certificado (ejemplo firma e-dni)
PKCS11_ALIAS=CertFirmaDigital
#Password de la tarjeta (PIN)
PKCS11_PASSWORD=

#Url de envío facturas firmadas
TBAI_POST_URL=https://pruebas-ticketbai.araba.eus/TicketBAI/v1/facturas/

#Url del la política de firma y fichero pdf descargado en local
TBAI_POLICY_URL=https://ticketbai.araba.eus/tbai/sinadura/
TBAI_POLICY_FILE=/ticketbay/doc/tbai_sign_policy.pdf

#Url a incluir en el QR para consulta facturas
TBAI_QR_URL=https://pruebas-ticketbai.araba.eus/tbai/qrtbai/

#Java Keystore. Tiene que tener las CA's de los certificados utilzados.
JAVA_KEYSTORE_PATH=C:\\java\\jdk-11.0.2\\lib\\security
JAVA_KEYSTORE_FILE=C:\\java\\jdk-11.0.2\\lib\\security\\cacerts_prueba
JAVA_KEYSTORE_PASS=changeit
```

## Ejemplo de firma PKCS12
```
//Cargar Certificado
TbaiCertImplPkcs12 tbaicert12=new TbaiCertImplPkcs12();
tbaicert12.setPath(CERT_FILE);
tbaicert12.setPassword(CERT_PASSWORD);			

//Política de firma de la diputación de Álava
TbaiSignPolicy tbaipolicy=new TbaiSignPolicy();
tbaipolicy.setPolicyFile(POLICY_FILE);
tbaipolicy.setPolicyUrl(POLICY_URL);

//Firmar documento y guardar					
Document docsig=TbaiXadesEpesSign.sign(TBAI_FACT_XML_FILE,tbaipolicy, tbaicert12);
TbaiXMLUtils.outputDocument(docsig, TBAI_FACT_XML_FILE_SIG);
```

## Ejemplo de firma PKCS11
En este caso se utiliza OpenCS.  
https://github.com/OpenSC/OpenSC/releases  
Es posible inyectar el pin directamente sin solicitarlo.  
Se usa opensc-pkcs11.dll como libraría nativa para comunicación a través del lector.  
Tiene que haber un lector conectado y un certificado en tarjeta insertado.
```					
TbaiCertImplPkcs11 tbaicert11=new TbaiCertImplPkcs11();
tbaicert11.setPassword(CERT_PASSWORD);
Document docsig=TbaiXadesEpesSign.sign(TBAI_FACT_XML_FILE,tbaipolicy, tbaicert11);
```

## Gneración de TBAI-ID y QR a partir del documento firmado
```
//Cargar el documento firmado y generar el id y qr
TbaiID tbaiid=new TbaiID(docsig);
tbaiid.setUrl(QR_URL);
String id=tbaiid.getTbaiID();	
String qrtext=tbaiid.getUrlQR(id);
TbaiQR.generateQRcode(qrtext, TBAI_QR_IMAGE_FILE);
```

## Envío del documento firmado. Es necesario presentar un certificado cliente.
La carga del certificado es igual que para la firma.

### Ejemplo con certificado PKCS12
```
TbaiCertImplPkcs12 tbaicert12=new TbaiCertImplPkcs12();
tbaicert12.setPath(CERT_FILE);
tbaicert12.setPassword(CERT_PASSWORD);
//Enviar documento firmado.
TbaiPost tbaipost=new TbaiPost();
tbaipost.postPkcsSignedXML(tbaicert12,POST_URL,TBAI_FACT_XML_FILE_SIG);
```
### Ejemplo con certificado PKCS11
```
TbaiCertImplPkcs11 tbaicert11=new TbaiCertImplPkcs11();
tbaicert11.setPassword(CERT_PASSWORD);
//Enviar documento firmado.
TbaiPost tbaipost=new TbaiPost();
tbaipost.postPkcsSignedXML(tbaicert11,POST_URL,TBAI_FACT_XML_FILE_SIG);
```

## Ejemplo de comprobación de firma
```
try { 	 
	TbaiSignPolicy tbaipolicy=new TbaiSignPolicy();
	tbaipolicy.setPolicyFile("tbai_sign_policy.pdf"));
	XAdESVerificationResult result = TbaiXadesEpesVerify.verifySignature("fact_tbai.xsig",tbaipolicy);
	XAdESForm form=result.getSignatureForm();
	logger.info("Firma OK. {}",form.toString());
} catch (Exception e) {
	logger.error("Error validadndo firma",e);
}
```
