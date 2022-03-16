package tbai4j.cert;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SigningCertChainException;
import xades4j.verification.UnexpectedJCAException;

public interface TbaiCert {

	public KeyingDataProvider getKeyingDataProvider() throws SigningCertChainException, UnexpectedJCAException;
	
	public SSLConnectionSocketFactory getPkcsConFactory() throws Exception;
}
