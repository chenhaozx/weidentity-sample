
# 生成服务端证书
# keytool -genkey -v -alias tomcat -keyalg RSA -keystore E:/ssl/tomcat.keystore -validity 365

# 生成客户端证书
# keytool -genkey -v -alias clientkey -keyalg RSA -storetype PKCS12 -keystore E:/ssl/clientkey.p12

# 由于不能直接将PKCS12格式的证书库导入，所以必须先把客户端证书导出为一个单独的CER文件
# keytool -export -alias clientkey -keystore E:/ssl/clientkey.p12 -storetype PKCS12 -storepass 123456 -rfc -file E:/ssl/clientkey.cer

# 服务器信任客户端证书
# keytool -import -v -file E:/ssl/clientkey.cer -keystore E:/ssl/tomcat.keystore

# 客户端信任服务器证书
# keytool -keystore E:/ssl/tomcat.keystore -export -alias tomcat -file E:/ssl/tomcat.cer
