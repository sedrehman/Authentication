package p1;

import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;


/**
 * 
 * @author syed rehman
 * @email syedrehm@buffalo.edu
 *
 */

/*
 * Java ~ TLS 1.2
 * documentation referral for the use of java.net.ssl.*
 * https://docs.oracle.com/javadb/10.8.3.0/adminguide/cadminsslclient.html
 */
public class Server {
	private static int PORT = 8000;
	private static URL url = Server.class.getResource("keystore.jks");
	private static final String protocol = "TLSv1.2"; //"TLSv1", "TLSv1.1", 
	private static final char[] password = "cse312".toCharArray();
	
	public static void main(String[] args) {
		
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	        ks.load(url.openStream() ,password);

	        KeyManagerFactory keyMan =  KeyManagerFactory.getInstance("SunX509");
	        keyMan.init(ks, password);
	        
	        TrustManagerFactory trustMan = TrustManagerFactory.getInstance("SunX509");
	        trustMan.init(ks);
	        
	        SSLContext sslCon = SSLContext.getInstance(protocol);
	        sslCon.init(keyMan.getKeyManagers(), trustMan.getTrustManagers(), null);
			
	        SSLServerSocketFactory serverFactory = sslCon.getServerSocketFactory();
	        
	        SSLServerSocket server = (SSLServerSocket) serverFactory.createServerSocket(PORT);
	        		
			System.out.println("running server at port -- "+ PORT);
			while(true) {
				SSLSocket client = (SSLSocket) server.accept();
				new Thread(new RequestHandler(client)).start();
			}
		} catch (Exception e) {
			System.out.println("exception");
//			e.printStackTrace();
		}
	}

}
