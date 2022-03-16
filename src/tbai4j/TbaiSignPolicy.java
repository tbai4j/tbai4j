package tbai4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import xades4j.properties.ObjectIdentifier;
import xades4j.properties.SignaturePolicyBase;
import xades4j.properties.SignaturePolicyIdentifierProperty;
import xades4j.providers.SignaturePolicyInfoProvider;

public class TbaiSignPolicy {
	private String policyUrl="https://ticketbai.araba.eus/tbai/sinadura/";
	private String policySHA256="d69VEBc4ED4QbwnDtCA2JESgJiw+rwzfutcaSl5gYvM=";
	private String policyFile="C:\\ticketbay\\doc\\tbai_sign_policy.pdf";
	
	public String getPolicyUrl() {
		return policyUrl;
	}
	public void setPolicyUrl(String policyUrl) {
		this.policyUrl = policyUrl;
	}
	public String getPolicySHA256() {
		return policySHA256;
	}
	public void setPolicySHA256(String policySHA256) {
		this.policySHA256 = policySHA256;
	}
	public String getPolicyFile() {
		return policyFile;
	}
	public void setPolicyFile(String policyFile) {
		this.policyFile = policyFile;
	}
	
	public SignaturePolicyInfoProvider getSignaturePolicyInfoProvider() throws FileNotFoundException {
	
		 String policyUrl=getPolicyUrl();
	     InputStream policyInputStream = new FileInputStream(getPolicyFile());
	     SignaturePolicyInfoProvider policyInfoProvider = new SignaturePolicyInfoProvider() {
	    	 @Override
	        public SignaturePolicyBase getSignaturePolicy()
	        {
	            return new SignaturePolicyIdentifierProperty(
	                    new ObjectIdentifier(policyUrl),
	                    policyInputStream)
	                .withLocationUrl(policyUrl);
	        }
	     };
        return policyInfoProvider;
	 }

}
