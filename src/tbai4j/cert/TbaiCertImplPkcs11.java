package tbai4j.cert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

import xades4j.production.TbaiPasswordProvider;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.PKCS11KeyStoreKeyingDataProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.SigningCertificateSelector;
import xades4j.utils.FileUtils;

public class TbaiCertImplPkcs11 implements TbaiCert{
	private static final String CLIENT_KEYSTORE_TYPE = "PKCS11";
	private static final String  PKCS11_LIB_PATH =  "C:\\java\\OpenSC\\OpenSC\\pkcs11\\opensc-pkcs11.dll";
	private static String SUN_PKCS11_PROVIDER = "SunPKCS11";
	
	
	private String type=CLIENT_KEYSTORE_TYPE;
	private String pkcs11Lib=PKCS11_LIB_PATH;
	private String alias="CertFirmaDigital";
	private String pkcs11name="tbai4j";
	private Integer pkcs11SlotId=null;
	private String password;
	
	
	private static final Logger logger = LoggerFactory.getLogger(TbaiCertImplPkcs11.class);

	public String getPkcs11Name() {
		return pkcs11name;
	}
	public void setPkcs11Name(String pkcs11name) {
		this.pkcs11name=pkcs11name;
	}
	public void setPkcs11SlotId(Integer slotid) {
		this.pkcs11SlotId=slotid;
	}
	public Integer getPkcs11SlotId() {
		return this.pkcs11SlotId; 
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isProviderAvailable()
    {
        return Security.getProvider(SUN_PKCS11_PROVIDER) != null;
    }
	
	public String getType() {
		return type;
	}
	
	
	public String getAlias() {
		return alias;
	}


	public void setAlias(String alias) {
		this.alias = alias;
	}


	public String getPkcs11Lib() {
		return pkcs11Lib;
	}


	public void setPkcs11Lib(String pkcs11Lib) {
		this.pkcs11Lib = pkcs11Lib;
	}

	 @Override
	public KeyingDataProvider getKeyingDataProvider() {
		
		SigningCertificateSelector certificateSelector = new SigningCertificateSelector() {
			@Override
			public Entry selectCertificate(List<Entry> availableCertificates)
		    {							
		        
		        Stream<Entry> stEntryf=availableCertificates.stream();
		    	Stream<Entry> stEntrya=stEntryf.filter(it -> it.getAlias().contains(getAlias()));
		    	Optional<Entry> optEntry=stEntrya.findFirst();
		    	Entry entry=optEntry.orElseThrow();
		    	
		    	if(logger.isDebugEnabled()) {
			    	X509Certificate x509=entry.getCertificate();		    	
			    	logger.debug("{}: Alias: {}",getType(),entry.getAlias());
			    	logger.debug("{}: IssureDN: {}",getType(),x509.getIssuerDN().getName());
			    	logger.debug("{}: SubjectDN: {}",getType(),x509.getSubjectDN().getName());
			    	logger.debug("{}: SerialNumber: {}",getType(),x509.getSerialNumber());
		    	}
		    	
		    	return entry;
		    }
		};
		
		 KeyingDataProvider keyingDataProvider =  PKCS11KeyStoreKeyingDataProvider
         .builder(getPkcs11Lib(), certificateSelector)
         .storePassword(new TbaiPasswordProvider(getPassword()))
         .build();
		 
		 
		 return keyingDataProvider;
	}
	 
	 private ProtectionParameter getKeyProtection(String password){
	        if (null == password){
	            return null;
	        }

	        return new KeyStore.CallbackHandlerProtection(new CallbackHandler()
	        {

	            @Override
	            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
	            {
	                PasswordCallback c = (PasswordCallback) callbacks[0];
	                c.setPassword(password.toCharArray());
	            }
	        });
	    }
	 
		private  KeyStore getKeyStoreSunPKCS11() throws KeyStoreException, IOException {
			
			 String newLine = System.getProperty("line.separator");
		     StringBuilder config = new StringBuilder()
		                .append("name = ").append(this.pkcs11name).append(newLine)
		                .append("library = ").append(this.pkcs11Lib).append(newLine);
		    if (this.pkcs11SlotId != null){
		            config.append("slot = ").append(this.pkcs11SlotId).append(newLine);
		    }
		    
		    String configName=FileUtils.writeTempFile(config.toString());
			
			
		    Provider p = Security.getProvider(SUN_PKCS11_PROVIDER);
		    p = p.configure(configName);
		    Security.addProvider(p);
		    
		    KeyStore.Builder builder= KeyStore.Builder.newInstance(CLIENT_KEYSTORE_TYPE, p, getKeyProtection(getPassword()));
	        return builder.getKeyStore();
	        
		}
		
		private SSLConnectionSocketFactory getSunPkcs11ConFactory() throws Exception {
		    KeyStore MyKeyStore =  getKeyStoreSunPKCS11();
		    MyKeyStore.load(null, null);
		    // loading keymanager 
		    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		    keyManagerFactory.init(MyKeyStore, null);
		    // building truststore
		    TrustManager[] trustAllManager = new TrustManager[]{new X509TrustManager() {
		        public X509Certificate[] getAcceptedIssuers() {
		            return new X509Certificate[0];
		        }
		        public void checkClientTrusted(X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(X509Certificate[] certs, String authType) {
		        }
		    }};
		    SSLContext sslContext = SSLContext.getInstance("TLS");
		    sslContext.init(keyManagerFactory.getKeyManagers(), trustAllManager, new SecureRandom());
		    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
		            new String[]{"TLSv1","TLSv1.1", "TLSv1.2", "TLSv1.3"},
		            null,
		            SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		    
			return sslConnectionSocketFactory;			
		}
		
		@Override
		public SSLConnectionSocketFactory getPkcsConFactory() throws Exception{
				 return getSunPkcs11ConFactory();
				
		}
		
		public SSLConnectionSocketFactory getWinPkcs11ConFactory() throws Exception {
			// loading windows-my store
		    KeyStore windowsMyKeyStore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
		    windowsMyKeyStore.load(null, null);
		    // loading keymanager 
		    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		    keyManagerFactory.init(windowsMyKeyStore, null);
		    // building truststore
		    TrustManager[] trustAllManager = new TrustManager[]{new X509TrustManager() {
		        public X509Certificate[] getAcceptedIssuers() {
		            return new X509Certificate[0];
		        }
		        public void checkClientTrusted(X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(X509Certificate[] certs, String authType) {
		        }
		    }};
		    SSLContext sslContext = SSLContext.getInstance("TLS");
		    sslContext.init(keyManagerFactory.getKeyManagers(), trustAllManager, new SecureRandom());
		    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
		            new String[]{"TLSv1","TLSv1.1", "TLSv1.2", "TLSv1.3"},
		            null,
		            SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		    
			return sslConnectionSocketFactory;
			
		}

}
