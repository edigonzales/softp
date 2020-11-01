# softp

## todo
- ask about sources of `vfsutils-ftpserver` and `vfsutils-utils`
- ask to put these libs on mavencentral
- ~~config user. e.g. max connections / explicit read only etc. etc.~~

## hints

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