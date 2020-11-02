package ch.so.agi.softp;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.vfsutils.ftpserver.filesystem.VfsAuthenticator;
import org.vfsutils.ftpserver.filesystem.VfsFileSystemFactory;
import org.vfsutils.ftpserver.filesystem.VfsFileSystemView;
import org.vfsutils.ftpserver.filesystem.VfsInfo;
import org.vfsutils.ftpserver.usermanager.VfsUser;

@Component
public class MyFtpServer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.ftpServerHetzner}")
    private String ftpServerHetzner;
    
    @Value("${app.ftpUserHetzner}")    
    private String ftpUserHetzner;
    
    @Value("${app.ftpPwdHetzner}")    
    private String ftpPwdHetzner;
    
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
        userManager.save(user);
        
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(2221);
         
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        VfsFileSystemFactory vfsFileSystemFactory = new VfsFileSystemFactory();
        VfsAuthenticator vfsAuthentificator = new VfsAuthenticator();
        vfsAuthentificator.setVfsRoot("ftp://"+ftpUserHetzner+":"+ftpPwdHetzner+"@"+ftpServerHetzner+"/");        
//        vfsAuthentificator.setVfsRoot("sftp://" + ftpServerHetzner);
        vfsAuthentificator.setVfsType("virtual");
        
        vfsFileSystemFactory.setAuthenticator(vfsAuthentificator);

//        BaseUser vfsUser = new BaseUser();
//        vfsUser.setName(ftpUserHetzner);
//        vfsUser.setPassword(ftpPwdHetzner);
//        vfsUser.setHomeDirectory("/");
//        vfsUser.setEnabled(true);
//        vfsFileSystemFactory.createFileSystemView(vfsUser);
        
        factory.setFileSystem(vfsFileSystemFactory);
        
        FtpServer server = factory.createServer();
        server.start();
        
//        FileSystemOptions opts = new FileSystemOptions();
//        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
//        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
//
//        log.info("fubar0");
//
//        FileSystemManager fsManager = VFS.getManager();
//        FileObject localFileObject=fsManager.resolveFile("ftp://"+ftpUserHetzner+":"+ftpPwdHetzner+"@"+ftpServerHetzner+"/");
//        FileObject[] children = localFileObject.getChildren();
//        log.info("fubar1");
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
