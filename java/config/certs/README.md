# KeyStore & TrustStore
```
keytool -genkey -keystore keystore.jks -alias control -keyalg RSA -keypass password -storepass password -dname "CN=control, OU=control, O=control, L=control, ST=control, C=control" -ext SAN=dns:control
keytool -export -alias control -file control.cer -keystore keystore.jks -storepass password
keytool -import -v -trustcacerts -alias control -keypass password -file control.cer  -keystore cacerts.jks -storepass password
```


