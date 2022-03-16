package tbai4j.cert;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xades4j.production.TbaiPasswordProvider;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SigningCertChainException;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;
import xades4j.verification.UnexpectedJCAException;

public class TbaiCertImplPkcs12 implements TbaiCert {
	private static final String CLIENT_KEYSTORE_TYPE = "PKCS12";
	private String path;
	private String password;
	private String type=CLIENT_KEYSTORE_TYPE;
	
	private static final Logger logger = LoggerFactory.getLogger(TbaiCertImplPkcs12.class);
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	 @Override
	public KeyingDataProvider getKeyingDataProvider() throws SigningCertChainException, UnexpectedJCAException {
		boolean returnFullChain=true;
		KeyingDataProvider keyingDataProvider=FileSystemKeyStoreKeyingDataProvider
                .builder(this.type, this.path, KeyStoreKeyingDataProvider.SigningCertificateSelector.single())
                .storePassword(new TbaiPasswordProvider(this.password))
                .entryPassword(new TbaiPasswordProvider(this.password))
                .fullChain(returnFullChain)
                .build();
		
		if(logger.isDebugEnabled()) {
			List<X509Certificate>x509list=keyingDataProvider.getSigningCertificateChain();
			Iterator<X509Certificate> it=x509list.iterator();
			while(it.hasNext()) {
				X509Certificate x509=it.next();
				logger.debug("{}: IssureDN: {}",getType(),x509.getIssuerDN().getName());
		    	logger.debug("{}: SubjectDN: {}",getType(),x509.getSubjectDN().getName());
		    	logger.debug("{}: SerialNumber: {}",getType(),x509.getSerialNumber());
			}
		}
		
		return keyingDataProvider;
	}
	 

	private SSLConnectionSocketFactory getPkcs12ConFactory() throws Exception {
		
		char chpass[]=getPassword().toCharArray();
	    KeyStore cks = KeyStore.getInstance(getType());
	    cks.load(new FileInputStream(getPath()), chpass );

	    SSLContext sslcontext = SSLContexts.custom()
	            .loadKeyMaterial(cks, chpass) // load client certificate
	            .build();
		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
				sslcontext,
	            new String[]{"TLSv1","TLSv1.1", "TLSv1.2", "TLSv1.3"}, 
	            null,
	            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
	            );
		return sslConnectionSocketFactory;
		
	}
	
	 @Override
	public SSLConnectionSocketFactory getPkcsConFactory() throws Exception{
		 return getPkcs12ConFactory();
		
	}

}
