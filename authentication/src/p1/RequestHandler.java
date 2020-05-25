package p1;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class RequestHandler implements Runnable{
	
	private static final String[] protocols = new String[] {"TLSv1.2"};
	private FileReaderHelper helper = new FileReaderHelper();
	private Header headGenerator = new Header();
	private SSLSocket client;
	private InputStream is;
	private Map<String, String> headers;
	private static final String SALT = "cse312_hw8";
	private static final String MSG_CUT = "<form class=\"myForm\"";
	
	public RequestHandler(SSLSocket client) {
		//System.out.println("here");
		this.client = client;
		headers = new HashMap<String, String>();
		try {
			is = client.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			client.startHandshake();
			client.setSoTimeout(2000);
			while(!client.isClosed()) {
				String line = getLine();
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+line);
				String type[] = line.split(" ");
				headers.put("Path", type[1].trim() );
				
				if(type[0].equals("POST")) {
					handlePost();
					break;
				}else {
					handleGet();
					String connection = headers.get("Connection");
					if(connection != null) {
						
						type = connection.split(",");
						for(String e : type) {
							e = e.trim();
							if(e.equals("Upgrade")) {
								handleWebSocket();
							}else {
								handleNormalGet(null, null);
								break;
							}
						}
					}else {
						handleNormalGet(null, null);
						break;
					}
				}
			}
		}catch(javax.net.ssl.SSLHandshakeException e2) {
			//System.out.println("~~~~~~~~~~~~~~~");
		}catch(SocketTimeoutException e) {
			try {
				client.close();
			} catch (Exception e1) {
				System.out.println("###################");
			}
		}catch(IOException e) {
			
		}
		//System.out.println("done..\n");
	}
	
	private void handleNormalGet(String msg, String setCookie) {
		System.out.println(headers.toString());
		String head = null;
		String body = null;
		String user = null;
		String cookie = headers.get("Cookie");
		String accept = headers.get("Accept");
		String path = headers.get("Path");
		
		if(accept.equals("*/*")) {
			accept = "text/javascript";
		}
		if(setCookie != null) {
			cookie = setCookie;
		}
		if(path.equals("/")) { 
			path = "login.html";
		}
		if(cookie != null && cookie.length() >10) {
			System.out.println(cookie);
			user = getUserByCookie(cookie);
		}
		
		if(user != null) {
			if(path.equals("/login.html")) {
				path = "profile.html";
			}else if(path.equals("/profile_info")) {
				body = user;
			}
		}else {
			if(path.equals("/profile.html")) {
				head = headGenerator.create301Header("login.html");
				path = "login.html";
			}
		}
		if(body == null) {
			body = helper.getResponse(path);
		}
		if(body == null) {
			body =  "THE CONTENT WAS NOT FOUND";
			head = headGenerator.create404Header();
		}
		
		
		if(msg != null) {
			int index = body.indexOf(MSG_CUT);
			String msg1 = body.substring(0, index-1);
			msg1 += "<p>"+ msg + "<//p>";
			String msg2 = body.substring(index, body.length()-1);
			body = msg1+ msg2;
		}
		if(head == null) {
			head = headGenerator.createHeader(accept, cookie, body.length());
		}
		send(head, body);
	}
	
	private String getUserByCookie(String cookie) {
		return helper.matchToken(helper.generateHash(cookie));
	}

	private void send(String head, String body) {
		try {	
			PrintWriter writer = new PrintWriter(client.getOutputStream());
			writer.println(head);
			writer.flush();
			writer.println(body);
			writer.flush();
			writer.close();
			if(!client.isClosed()) {
				client.close();
			}
		} catch (IOException e) {

		} 
	}

	private void handleGet() {
		String line = null;
		try {
			while( (is.available() > 0) && (line = getLine()).length() > 0) {
				if(line.length() < 2) {
					break;
				}
				String type[] = line.split(":");
				switch(type[0]) {
				case "Accept":
					headers.put("Accept", type[1].split(",")[0].trim());
					break;
				case "Cookie":
					String cookie = type[1].trim().replace("token=", "");
					headers.put("Cookie", cookie);
					break;
				case "Connection":
					headers.put("Connection", type[1].trim());
					break;
				case "Upgrade":
					headers.put("Upgrade", type[1].trim());
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleWebSocket() {
		//we already know that its a websocket connection. 
		
	}
	private void handlePost() {
		System.out.println("handling post");
		
		String line = null;
		while((line = getLine()) != null && line.length() > 2) {
			//System.out.println(line);
			if(line.length() > 2) {
				String type[] = line.split(":");
				switch(type[0]) {
				case "Content-Length":
					headers.put("Content-Length", type[1].trim());
					break;
				case "Content-Type":
					String[] content = type[1].split(";");
					String content_type = content[0].trim();
					if(content_type.equals("multipart/form-data")) {
						headers.put("boundary", content[1].trim().split("=")[1].trim());
					}
					headers.put("Content-Type", content[0].trim());
					break;
				}
			}
		}
		
		int data_size = Integer.parseInt(headers.get("Content-Length")) ;
		byte data[] = new byte[data_size];
		
		try {
			for(int i = 0; i< data_size; i++ ) {
				data[i] = (byte) is.read();
			}
		}catch(IOException e) {
			System.out.println("here");
			e.printStackTrace();
		}
		if(headers.get("Content-Type").equals("multipart/form-data")) {
			handlePostData(new String(data, StandardCharsets.UTF_8));
		}
		
	}
	

	private Void handlePostData(String s) {
		String boundary = headers.get("boundary");
		String endkey = boundary + "--";
		String allLines[] = s.split("\n");
		headers.put("Accept", "text/html");
		
		for(int i = 0; i< allLines.length; i++) {
			String line = allLines[i];
			
			if(line.length() > 2) {
				if(line.contains(boundary)) {
					if(line.contains(endkey)) {
						break;
					}
					String parts[] = allLines[i+1].split("name=");
					String name = parts[1].trim().replace("\"", "");
					headers.put(name, allLines[i+3].replace("\n", "").trim());
					System.out.println("Name="+ name + " value=" + headers.get(name));
				}
			}
		}
		if(headers.get("Path").contains("login.html")) {
			
			String email = headers.get("email");
			if(email.length() <5) {
				headers.put("Path", "index.html");
				handleNormalGet(" incorrect email " , null);
				return null;
			}
			String password = headers.get("password");
			if(password.length() < 8) {
				headers.put("Path", "index.html");
				handleNormalGet(" incorrect password ", null);
				return null;
			}
			String favClass = headers.get("favClass");
			if(favClass.length() <2) {
				headers.put("Path", "index.html");
				handleNormalGet(" incorrect Fav class ", null);
				return null;
			}
			
			HashMap<String, String[]> users = helper.getUsers();
			if(users.containsKey(email)) {
				headers.put("Path", "index.html");
				handleNormalGet(" email allready exist! ", null);
				return null;
			}
			
			String token = helper.generateToken(20);
			String all_user_info = email + "," + helper.generateHash(password)+SALT + "," 
					+ favClass + "," + helper.generateHash(token) + "\n";
			WriteHelper writer = new WriteHelper();
			writer.writeCSV(all_user_info, "userFile.csv", true);
			
			handleNormalGet(null , token);
		}
		
		if(headers.get("Path").contains("profile.html")) {
			
			String msg = null;
			
			String email, password;
			email = headers.get("email");
			if(email.length() <5) {
				msg = " incorrect email ";
				return null;
			}
			password = headers.get("password");
			if(password.length() < 8) {
				msg = " incorrect password ";
				return null;
			}
			String token = null;
			if( (token = helper.login(email, helper.generateHash(password)+SALT)) != null ) {
				System.out.println(token+ "\n\n\n");
				headers.put("Path", "profile.html");
				handleNormalGet(null , token);
				return null;
			}
			headers.put("Path", "login.html");
			msg = " incorrect credentials ";
			handleNormalGet(msg , token);
		}
		return null;
	}

	private String getLine() {
		try {
			byte[] whole_line = new byte[1000];
			int counter = 0;
			int byte_in;
			while((byte_in = is.read())!= 0xA && (byte_in != -1)) {
				whole_line[counter++] = (byte) byte_in;
			}
			byte[] actual_line = new byte[counter];
			while(counter >= 1) {
				actual_line[--counter] = whole_line[counter];
			}
			String l = new String(actual_line, StandardCharsets.UTF_8);
			//System.out.println(l);
			return l;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
