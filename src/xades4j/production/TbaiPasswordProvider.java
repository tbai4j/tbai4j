package xades4j.production;

import java.security.cert.X509Certificate;

import xades4j.providers.impl.KeyStoreKeyingDataProvider;

public class TbaiPasswordProvider implements KeyStoreKeyingDataProvider.KeyStorePasswordProvider,
        KeyStoreKeyingDataProvider.KeyEntryPasswordProvider
{
    private char[] password;

    public TbaiPasswordProvider(String password)
    {
        this.password = password.toCharArray();
    }

    @Override
    public char[] getPassword()
    {
        return password;
    }

    @Override
    public char[] getPassword(String alias, X509Certificate certificate)
    {
        return password;
    }
}
