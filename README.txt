# SecurityProject

 Grupo n.33
 Pedro Pais, n.º 41375
 Pedto Candido, n.º15674
 Antonio Rodrigues n.º40853


Run comandos client

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient -init myrep

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -push myrep

java  -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -push myrep/1.txt

java  -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -pull myrep

java   -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -pull myrep/1.txt

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -share myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -remove myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient pedro 10.101.148.173:23456 -p ant

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -share myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -share myrep pedro

Run comandos server

java -Djava.security.manager -Djava.security.policy=server.policy -cp bin server.MyGitServer 23456 


1) Gerar par de chaves do servidor

keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .myGitServerKeyStore -alias myGitServer


2) Gerar par de chaves dos utilizadores

keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .antonioKeyStore -alias antonio

keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .pedroKeyStore -alias pedro


3) criar truststores dos clientes (slide 8)

//export do certificado a partir da keystore do servidor

keytool -exportcert -alias myGitServer -file myGitServer.cer -keystore .myGitServerKeyStore

//import do certificado para a truststore do cliente

keytool -importcert -alias myGitServer -keystore .antonioTrustStore -file myGitServer.cer
keytool -importcert -alias myGitServer -keystore .pedroTrustStore -file myGitServer.cer




4) criar truststore do servidor (slide 8)

//export do certificado a partir da keystore do cliente

keytool -exportcert -alias antonio -file antonio.cer -keystore .antonioKeyStore
keytool -exportcert -alias pedro -file pedro.cer -keystore .pedroKeyStore


//import dos certificados para a truststore do servidor

keytool -importcert -alias antonio -keystore .myGitServerTrustStore -file antonio.cer
keytool -importcert -alias pedro -keystore .myGitServerTrustStore -file pedro.cer
