package ch.so.agi.softp;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.vfsutils.ftpserver.filesystem.ShallowReadOnlyVfsFtpFileFactory;
import org.vfsutils.ftpserver.filesystem.VfsAuthenticator;
import org.vfsutils.ftpserver.filesystem.VfsFileSystemFactory;

@Component
public class MyFtpServer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.ftpServerHetzner}")
    private String ftpServerHetzner;
    
    @Value("${app.ftpUserHetzner}")    
    private String ftpUserHetzner;
    
    @Value("${app.ftpPwdHetzner}")    
    private String ftpPwdHetzner;
        
    @Value("${app.ftpPort}")    
    private int ftpPort;

    @Value("${app.ftpAnonMaxLogin}")    
    private int ftpAnonMaxLogin;

    @Value("${app.ftpAnonMaxThreads}")    
    private int ftpAnonMaxThreads;

    private FtpServer ftpServer;

    @PostConstruct
    private void start() throws FileSystemException, FtpException {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        
        BaseUser user = new BaseUser();
        user.setName("anonymous"); 
        user.setHomeDirectory("/");
        userManager.save(user);
        
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(ftpPort);
        
        DataConnectionConfigurationFactory dataConfigFactory = new DataConnectionConfigurationFactory();
        dataConfigFactory.setPassivePorts("20100-20150");
        listenerFactory.setDataConnectionConfiguration(dataConfigFactory.createDataConnectionConfiguration());
         
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        VfsFileSystemFactory vfsFileSystemFactory = new VfsFileSystemFactory();
        VfsAuthenticator vfsAuthentificator = new VfsAuthenticator();
        // sftp does not work on macos (vfs2)
        // common-vfs2 v2.7.0 hangs after "Authentication succeeded (password)." Use v2.4.1 instead.
        // pure ftp:// seems to be faster (mmmh, vielleicht auch nicht)
        vfsAuthentificator.setVfsRoot("sftp://"+ftpUserHetzner+":"+ftpPwdHetzner+"@"+ftpServerHetzner);        
        vfsAuthentificator.setVfsType("virtual");
        
        vfsFileSystemFactory.setAuthenticator(vfsAuthentificator);
        vfsFileSystemFactory.setFileFactory(new ShallowReadOnlyVfsFtpFileFactory());
                
        factory.setFileSystem(vfsFileSystemFactory);
        
        FtpServer server = factory.createServer();
        server.start();
        
//        FileSystemOptions opts = new FileSystemOptions();
//        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
//        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
        
//        FileSystemManager fsManager = VFS.getManager();
//        FileObject localFileObject=fsManager.resolveFile("sftp://"+ftpUserHetzner+":"+ftpPwdHetzner+"@"+ftpServerHetzner+"/");
//        FileObject[] children = localFileObject.getChildren();
//        for ( int i = 0; i < children.length; i++ ){
//            System.out.println( children[ i ].getName().getBaseName() );
//        }  
    }
    
    @PreDestroy
    private void stop() {
        if (ftpServer != null) {
            ftpServer.stop();
        }
    }   
}
