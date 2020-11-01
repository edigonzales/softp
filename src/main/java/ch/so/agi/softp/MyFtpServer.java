package ch.so.agi.softp;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vfsutils.ftpserver.filesystem.VfsAuthenticator;
import org.vfsutils.ftpserver.filesystem.VfsFileSystemFactory;

@Component
public class MyFtpServer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private FtpServer ftpServer;

    @PostConstruct
    private void start() {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
//        BaseUser user = new BaseUser();
//        user.setName("demo");
//        user.setPassword("demo");
//        user.setHomeDirectory("/tmp");
        
        BaseUser user = new BaseUser();
        user.setName("anonymous");
//        user.setHomeDirectory("/tmp");

        try {
            userManager.save(user);
        } catch (FtpException e) {
            log.warn("init user fail:", e);
            return;
        }
        
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(2221);
         
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);
//        connectionConfigFactory.setMaxLogins(maxLogin);
//        connectionConfigFactory.setMaxThreads(maxThreads);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());
        
        // http://vfs-utils.sourceforge.net/ftpserver/apidocs/index.html
        VfsFileSystemFactory vfsFileSystemFactory = new VfsFileSystemFactory();
        BaseUser vfsUser = new BaseUser();
        vfsUser.setName("XXXXXX");
        vfsUser.setPassword("YYYYYY");
        
        VfsAuthenticator vfsAuthentificator = new VfsAuthenticator();
        vfsAuthentificator.setVfsRoot("ftp://XXXXXX:YYYYYYY@ftp.infogrips.ch");
        vfsAuthentificator.setVfsType("virtual");
        try {
            vfsAuthentificator.authenticate("XXXXX", "YYYYYYY", "/");
        } catch (FileSystemException e) {
            log.error("could not connect to vfs: ", e);
            e.printStackTrace();
            return;
        }
        vfsFileSystemFactory.setAuthenticator(vfsAuthentificator);
        
        try {
            vfsFileSystemFactory.createFileSystemView(vfsUser);
        } catch (FtpException e) {
            log.error("could not create vfs file system: ", e);
            e.printStackTrace();
            return;
        }

        factory.setFileSystem(vfsFileSystemFactory);
         
        FtpServer server = factory.createServer();
        
        try {
            server.start();
        } catch (FtpException e) {
            log.error("failed to start ftp server: ", e);
            return;
        }


    }
    
    
    @PreDestroy
    private void stop() {
        if (ftpServer != null) {
            ftpServer.stop();
        }
    }   
}
