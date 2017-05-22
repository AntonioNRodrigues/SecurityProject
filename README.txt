# SecurityProject

 Grupo n.33
 Pedro Pais, n.Âº 41375
 Pedto Candido, n.Âº15674
 Antonio Rodrigues n.Âº40853


Run comandos client

# criar um repositorio
java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient -init myrep

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -push myrep

java  -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -push myrep/1.txt

java  -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -pull myrep

java   -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -pull myrep/1.txt

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -share myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -remove myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient pedro 10.101.148.173:4567 -p ant

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -share myrep pedro

java -Djava.security.manager -Djava.security.policy=client.policy -cp bin client.MyGitClient antonio 10.101.148.173:4567 -p ant -share myrep pedro

Run comandos server

java -Djava.security.manager -Djava.security.policy=server.policy -cp bin server.MyGitServer 4567


0) Ligaçao TLS/SSL
1) Gerar par de chaves do servidor
# Este Comando faz com que se ainda nÃ£o tivermos definido a keystore cria uma.
# gera uma um par de chaves do tipo RSA (Chaves assimetricas) com um tamanho de 2048 bits 
# o nome da keystore eh -myGitServerKeyStore com um alias de myGitServer
keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .myGitServerKeyStore -alias myGitServer


2) Gerar par de chaves dos utilizadores [NÃƒO FOI UTILIZADO]
# Para resolver o problema do share e do remove repositorios 
# cada Utilizador tinha a sua keystore com o seu par de chaves asimetricas 
keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .antonioKeyStore -alias antonio
keytool -genkeypair -keysize 2048 -keyalg RSA -keystore .pedroKeyStore -alias pedro

3) criar truststores dos clientes []
//export do certificado a partir da keystore do servidor
// este cetificado tem a chaeve publica do servidor.
keytool -exportcert -alias myGitServer -file myGitServer.cer -keystore .myGitServerKeyStore

//import do certificado para a truststore do cliente
// neste caso iriamos importar para keystore de cada cliente o certificado (Chave publica do servidor).
// isto farÃ¡ com que o cliente possa assinar assinar coisas (com a chave publica do servidor) que sÃ³ este poderia decifrar
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


1.TLS/SSL
1.1 keystore and truststore created with the respective commands above. 
1.2 Import the certificates to the keystore.
1.3 The "Stores" are loaded into the java code in the client(myGitClient.java) and in the server (myGitServer.java)
			
			The "Stores" are loaded into the java code in the client(myGitClient.java) and in the server (myGitServer.java)
			System.setProperty("javax.net.ssl.keyStore", ".myGitServerKeyStore"); //myGitClientKeyStore
			System.setProperty("javax.net.ssl.trustStore", ".myGitServerTrustStore"); //myGitClientTrustStore
			System.setProperty("javax.net.ssl.keyStorePassword", "badpassword1"); //badpassword2
			// System.setProperty("javax.net.debug", "all");

			String trustStore = System.getProperty("javax.net.ssl.trustStore");
			if (trustStore == null) {
				System.out.println("javax.net.ssl.trustStore is not defined");
			} else {
				System.out.println("javax.net.ssl.trustStore = " + trustStore);
			}

1.4 Change the type of socket to the SSLServerSocket

2. [HMAC --> SHA-256 ] Integridade dos ficheiros shared.txt && users.txt && owner.txt
2.1 [AES 128 bits / PBKDF2 With Hmac SHA 256 bits] SecretKey gerada com a password do servidor 
2.2 ficheiro HMAC --> owner.txt.hmac && 
2.3 Na altura de persistir um reposotorio (se não existe um ficheiro owner e criado, 
	cifra-se com a Server.key e constroi-se o seu owner.txt.hmac)
2.4 na altura de registar um utilizador em shared.txt (persistShareduser) shared.txt.hmac 
	checks the integrity of the current file (compara-se os hmacs) faz.se o append da info e
	depois tem de ser atualizar o file shared.txt.hmac
2.5 O ficheiro de owner.txt e ficheiro de users.txt.hmac eh igual (check the integrity compares the hmac)
	faz-se o decipher to memory append and creates a new hmac


3. [NONCE] Confidencialidade do users.txt e shared.txt
3.1 cifrado com a secretkey que foi criada com a password do servidor 
	[ESSA SECRETKEY DEVERIA TER SIDO CIFRADA COM A CHAVE PUBLICA DO SERVIDOR. 
	Assim só o servidor com a sua chave privada eh que consegueria decifrá-la]
3.2 NONCE --> UUID.randomUUID() 
3.3 Qdo eh estabelecida a connection Client/server o server manda o nonce para o cliente
3.4 Esse nonce fica guardado em memoria como atributo do cliente e eh passado para o cliente 
	cada vez que eh feito algum pedido do mesmo para o servidor (através do construtor).
3.5 No construtor do user eh chamado o calcSintese que faz o MessageDigest [SHA-256] para calcular 
	a sintese da concatenação do passnonce.
3.6 Do lado do servidor eh calculado a mesma coisa e compara-se as sinteses. 
	[FALHA--> Esta opção tomada apresenta um problema, pois a password (atributo do user) eh sempre passada em claro pela rede, 
	se não contarmos com o TLS]

4. assinatura digital (signature of file) [SHA256withRSA com private key of user (loaded from the cliente Keystore)]

	