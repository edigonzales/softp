# softp

## about
_Softp_ ist ein FTP-Proxy für (Geo-)Daten, die auf einem externen FTP gespeichert sind. Externe (vertrauenswürdige) FTP erlauben meistens keinen `anonymous`-Zugang, um selber nicht als Spamschleuder zu gelten. Durch den Proxy kann auch eine "so.ch"-URL verwendet werden. Der externe FTP wird als virtuelles Filesystem eingebunden.

Verwendet wird [_Apache FtpServer_](https://mina.apache.org/ftpserver-project/) des _Apache Mina_-Projektes.

## hints
- Unter macOS scheint das virtuelle Filesystem nicht zu funktionieren, falls es mit "sftp://" eingebunden wird. Mit "ftp://" funktioniert es problemlos.
- "sftp://" mit common-vfs2 > v2.4.1 funktioniert nicht. Siehe Ticket in JIRA...
- Die `vfsutils-ftpserver` und `vfsutils-utils` Bibliotheken sind in keinem Maven-Repo verfügbar und müssen lokal eingebunden werden.
- `vfsutils-ftpserver` musste gefixed werden, damit sie mit dem Hetzern-FTP funktioniert (welcher gewissen Metainformationen nicht preisgab). Siehe Mailingliste und E-Mails.

## build

### without testing (no env var for external ftp credentials are set)
```
./gradlew clean build -x test
```

```
docker build -t sogis/softp .
```

## run  (locally)
```
docker run -p 8080:8080 -p 21:2221 -p 20100-20150:20100-20150 --env ftpServerHetzner=XXXXXXXX --env ftpUserHetzner=YYYYYYYY --env ftpPwdHetzner=ZZZZZZZ sogis/softp
```

## run (AGI SO)

TODO: 
- Ports beim AIO?
- TLS? Funktioniert es überhaupt?

## code snippets

### local user
```
BaseUser user = new BaseUser();
user.setName("demo");
user.setPassword("demo");
user.setHomeDirectory("/tmp");
```

### read-only
Ohne explizite `WritePermissions` hat der `anonymous` user nur Leserechte.

```
List<Authority> authorities = new ArrayList<Authority>();
authorities.add(new WritePermission());
user.setAuthorities(authorities);
```

### vfs providers
```
try {
    for (String schema : VFS.getManager().getSchemes()) {
        log.info(schema);
    }
} catch (Exception e) {
    
}
```

### list files from sftp
```
FileSystemOptions opts = new FileSystemOptions();
SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);

FileSystemManager fsManager = VFS.getManager();
FileObject localFileObject=fsManager.resolveFile("sftp://"+ftpUserHetzner+":"+ftpPwdHetzner+"@"+ftpServerHetzner+"/");
FileObject[] children = localFileObject.getChildren();
log.info("fubar1");
for ( int i = 0; i < children.length; i++ ){
    System.out.println( children[ i ].getName().getBaseName() );
}
```

## todo
- docs (~~readme~~ and fe)
- expose passive ports to config
- ask about sources of `vfsutils-ftpserver` and `vfsutils-utils`
- ask to put these libs on mavencentral
- ~~config user. e.g. max connections / explicit read only etc. etc.~~
- save Hetzner credentials in Keepass.
