# KeyStore & TrustStore
```
keytool -genkey -keystore keystore.jks -alias svarm -keyalg RSA -keypass password -storepass password
keytool -export -alias svarm -file svarm.cer -keystore keystore.jks -storepass password
keytool -import -v -trustcacerts -alias svarm -keypass password -file svarm.cer  -keystore cacerts.jks -storepass password
```

