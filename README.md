# SecurityProject

 Grupo n.33
 Pedro Pais, n.º 41375
 Pedro Candido, n.º15674
 Antonio Rodrigues n.º40853


Launch server:

java -Djava.security.manager -Djava.security.policy=server.policy −Djavax.net.ssl.keyStore=.myGitServerKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword1 −Djavax.net.ssl.trustStore=.myGitServerTrustStore -cp bin server.MyGitServer 23456 

Run client:

java -Djava.security.manager -Djava.security.policy=client.policy −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient -init myrep

java -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -push myrep

java  -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -push myrep/1.txt

java  -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -pull myrep

java   -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -pull myrep/1.txt

java -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -share myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -remove myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient pedro 10.101.148.173:23456 -p ant

java -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -share myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy  −Djavax.net.ssl.keyStore=.myGitClientKeyStore
−Djavax.net.ssl.keyStorePassword=badpassword2 −Djavax.net.ssl.trustStore=.myGitClientTrustStore -cp bin client.MyGitClient antonio 10.101.148.173:23456 -p ant -share myrep pedro


