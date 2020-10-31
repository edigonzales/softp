package ch.so.agi.softp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
        user.setHomeDirectory("/tmp");

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
//        factory.setFileSystem(fileSystem);
         
        FtpServer server = factory.createServer();
        try {
            server.start();
        } catch (FtpException e) {
            log.error("failed to start ftp server:", e);
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
