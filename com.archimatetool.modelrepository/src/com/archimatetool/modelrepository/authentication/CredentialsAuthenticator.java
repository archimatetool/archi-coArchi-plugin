/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelrepository.authentication;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.eclipse.osgi.util.NLS;

import com.archimatetool.modelrepository.ModelRepositoryPlugin;
import com.archimatetool.modelrepository.preferences.IPreferenceConstants;
import com.archimatetool.modelrepository.repository.IRepositoryConstants;
import com.archimatetool.modelrepository.repository.RepoUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


/**
 * Authenticator for SSH and HTTP
 * 
 * @author Phillip Beauvoir
 */
public final class CredentialsAuthenticator {
    
    public interface SSHIdentityProvider {
        File getIdentityFile() throws IOException;
        String getIdentityPassword() throws IOException;
    }
    
    /**
     * SSH Identity Provider. Default is with details from Prefs
     */
    private static SSHIdentityProvider sshIdentityProvider = new SSHIdentityProvider() {
        @Override
        public File getIdentityFile() throws IOException {
            File file = new File(ModelRepositoryPlugin.INSTANCE.getPreferenceStore().getString(IPreferenceConstants.PREFS_SSH_IDENTITY_FILE));
            
            if(!file.exists()) {
                throw new IOException(NLS.bind(Messages.CredentialsAuthenticator_0, file));
            }
            
            return file;
        }
        
        @Override
        public String getIdentityPassword() throws IOException {
            String password = null;
            
            if(ModelRepositoryPlugin.INSTANCE.getPreferenceStore().getBoolean(IPreferenceConstants.PREFS_SSH_IDENTITY_REQUIRES_PASSWORD)) {
                SimpleCredentialsStorage scs = new SimpleCredentialsStorage(
                        new File(ModelRepositoryPlugin.INSTANCE.getUserModelRepositoryFolder(), IRepositoryConstants.SSH_CREDENTIALS_FILE));

                if(scs.hasCredentialsFile()) {
                    password = scs.getUsernamePassword().getPassword();
                }
                else {
                    throw new IOException(Messages.CredentialsAuthenticator_1);
                }
            }
            
            return password;
        }
    };
    
    public static void setSSHIdentityProvider(SSHIdentityProvider sshIdentityProvider) {
        CredentialsAuthenticator.sshIdentityProvider = sshIdentityProvider;
    }
    
    /**
     * Factory method to get the default TransportConfigCallback for authentication for repoURL
     * npw can be null and is ignored if repoURL is SSH
     */
    public static TransportConfigCallback getTransportConfigCallback(final String repoURL, final UsernamePassword npw) throws IOException {
        // SSH
        if(RepoUtils.isSSH(repoURL)) {
            return new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    transport.setRemoveDeletedRefs(true); // Delete remote branches that we don't have
                    
                    if(transport instanceof SshTransport) {
                        ((SshTransport)transport).setSshSessionFactory(getSshSessionFactory());
                    }
                }
                
                protected SshSessionFactory getSshSessionFactory() {
                    return new JschConfigSessionFactory() {

                        @Override
                        protected void configure(OpenSshConfig.Host host, Session session) {
                            session.setConfig("StrictHostKeyChecking", "no"); //$NON-NLS-1$ //$NON-NLS-2$
                        }

                        @Override
                        protected JSch createDefaultJSch(FS fs) throws JSchException {
                            JSch jsch = super.createDefaultJSch(fs);
                            
                            // TODO - we might not need to do this as it sets default locations for rsa_pub
                            jsch.removeAllIdentity();
                            
                            File file = null;
                            String pw = null;
                            try {
                                file = sshIdentityProvider.getIdentityFile();
                                pw = sshIdentityProvider.getIdentityPassword();
                            }
                            catch(IOException ex) {
                                throw new JSchException(ex.getMessage());
                            }
                            
                            if(pw != null) {
                                jsch.addIdentity(file.getAbsolutePath(), pw);
                            }
                            else {
                                jsch.addIdentity(file.getAbsolutePath());
                            }
                            
                            return jsch;
                        }
                    };
                }
            };
        }
        
        // HTTP
        if(npw != null) {
            return new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    transport.setCredentialsProvider(new UsernamePasswordCredentialsProvider(npw.getUsername(), npw.getPassword()));
                    transport.setRemoveDeletedRefs(true); // Delete remote branches that we don't have
                };
            };
        }
        
        throw new IOException(Messages.CredentialsAuthenticator_2 + " " + repoURL); //$NON-NLS-1$
    }
}
