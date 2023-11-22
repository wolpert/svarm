# KeyStore & TrustStore
```
keytool -genkey -keystore keystore.jks -alias control -keyalg RSA -keypass password -storepass password -dname "CN=control, OU=control, O=control, L=control, ST=control, C=control" -ext SAN=dns:control
keytool -export -alias control -file control.cer -keystore keystore.jks -storepass password
keytool -import -v -trustcacerts -alias control -keypass password -file control.cer  -keystore cacerts.jks -storepass password

keytool -genkey -keystore keystore.jks -alias node -keyalg RSA -keypass password -storepass password -dname "CN=node, OU=node, O=node, L=node, ST=node, C=node" -ext SAN=dns:node
keytool -export -alias node -file node.cer -keystore keystore.jks -storepass password
keytool -import -v -trustcacerts -alias node -keypass password -file node.cer  -keystore cacerts.jks -storepass password

keytool -genkey -keystore keystore.jks -alias proxy -keyalg RSA -keypass password -storepass password -dname "CN=proxy, OU=proxy, O=proxy, L=proxy, ST=proxy, C=proxy" -ext SAN=dns:proxy
keytool -export -alias proxy -file proxy.cer -keystore keystore.jks -storepass password
keytool -import -v -trustcacerts -alias proxy -keypass password -file proxy.cer  -keystore cacerts.jks -storepass password
```


