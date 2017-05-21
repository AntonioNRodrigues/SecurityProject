# SecurityProject

 Grupo n.33
 Pedro Pais, n.� 41375
 Pedro Candido, n.�15674
 Antonio Rodrigues n.�40853


Run client:

# criar um repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient -init myrep

# push de um repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -push myrep

# push de um file
java  -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -push myrep/1.txt

# pull de uma repositorio
java  -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -pull myrep

# pull de um file
java   -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -pull myrep/1.txt

# share repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -share myrep pedro

# remove repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -remove myrep pedro

# registo de um user
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient pedro 10.101.148.173:4567 -p ant

# share repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -share myrep pedro

# share repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -share myrep pedro

# Run server

java -Djava.security.manager -Djava.security.policy=server.policy -cp bin server.MyGitServer 4567


0) Ligação TLS/SSL
1) Gerar par de chaves do servidor
#Este Comando faz com que se ainda não tivermos definido a keystore cria uma.
#gera uma um par de chaves do tipo RSA (Chaves assimetricas) com um tamanho de 2048 bits 
#o nome da keystore eh -myGitServerKeyStore com um alias de myGitServer
keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .myGitServerKeyStore -alias myGitServer


2) Gerar par de chaves dos utilizadores [NÃO FOI UTILIZADO]
#Para resolver o problema do share e do remove repositorios 
#cada Utilizador tinha a sua keystore com o seu par de chaves asimetricas 
keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .antonioKeyStore -alias antonio
keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .pedroKeyStore -alias pedro

3) criar truststores dos clientes []
//export do certificado a partir da keystore do servidor
// este cetificado tem a chave publica do servidor.
keytool -exportcert -alias myGitServer -file myGitServer.cer -keystore .myGitServerKeyStore

//import do certificado para a truststore do cliente
// neste caso iriamos importar para keystore de cada cliente o certificado (Chave publica do servidor).
// isto fará com que o cliente possa assinar assinar coisas (com a chave publica do servidor) que só este poderia decifrar
// visto que so com a chave privada e que as poderia decifrar.
keytool -importcert -alias myGitServer -keystore .antonioTrustStore -file myGitServer.cer
keytool -importcert -alias myGitServer -keystore .pedroTrustStore -file myGitServer.cer


4) criar truststore do servidor
//export do certificado a partir da keystore do cliente
keytool -exportcert -alias antonio -file antonio.cer -keystore .antonioKeyStore
keytool -exportcert -alias pedro -file pedro.cer -keystore .pedroKeyStore


//import dos certificados para a truststore do servidor
keytool -importcert -alias antonio -keystore .myGitServerTrustStore -file antonio.cer
keytool -importcert -alias pedro -keystore .myGitServerTrustStore -file pedro.cer
