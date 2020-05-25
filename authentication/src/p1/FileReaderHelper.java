package p1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.net.ssl.SSLSocket;


public class FileReaderHelper {
	
	protected String getResponse(String choice) {
		return readFileToString("website/" + choice);
	}
	
	protected String getCssResponse() {		
		return readFileToString("website/style.css");
	}
	
	protected String getJSResponse() {
		return readFileToString("website/script.js");
	}
	
	protected String readFileToString(String path) {
		URL url = this.getClass().getResource(path);
		if(url == null) {
			System.out.println(path + "    is incorrect");
			return null;
		}
		String result = "";
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(url.getFile()));
			BufferedReader reader = new BufferedReader(isr);
			String line = reader.readLine();
			while(line != null) {
				result += line + "\n";
				line = reader.readLine();
			}
			
			reader.close();
			isr.close();
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String login(String username, String password) {
		HashMap<String, String[]> users = getUsers();
		if(users.containsKey(username)) {
			String u[] = users.get(username);
			if(u[1].equals(password)) {
				
				String token = generateToken(20);
				u[3] = generateHash(token);
				users.put(username, u);
				saveUsers(users);
				
				return token;
			}
		}
		return null;
	}
	
	protected String matchToken(String token) {
		String userFile = getResponse("userFile.csv");
		String users[] = userFile.split("\n");
		String u[];
		for(int i =0; i<users.length; i++) {
			u = users[i].split(",");
			if(u[3].equals(token)) {
				return u[0] + "," + u[2];
			}
		}
		return null;
	}
	
	
	protected HashMap<String, String[]> getUsers(){
		HashMap<String, String[]> all = new HashMap<>();
		String res = getResponse("userFile.csv");
		if(res == null) {
			return all;
		}
		String users[] = getResponse("userFile.csv").split("\n");
		String user[];
		for(String u: users) {
			user= u.split(",");
			all.put(user[0], user);
		}
		return all;
	}
	
	protected void saveUsers(Map<String, String[]> users) {
		WriteHelper writter = new WriteHelper();
		StringBuilder sb = new StringBuilder();
		String u[];
		String user = "";
		for(Entry<String, String[]> e: users.entrySet()) {
			user = "";
			u = e.getValue();
			user += u[0] + "," + u[1] + "," + u[2] + "," + u[3] + "\n";
			sb.append(user);
		}
		writter.writeCSV(sb.toString(), "userFile.csv", false);
	}
	
	
	protected void sendImageFile(String path, SSLSocket socket) {
		path = "website/" + path;
		URL url = this.getClass().getResource(path);
		if( url != null) {
			File file = new File(url.getFile());
			try {
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
				writer.println("HTTP/1.1 200 OK\nContent-Type: image/jpeg\nContent-length:"+ Long.toString(file.length())+"\n");
				writer.flush();
				
				OutputStream os = socket.getOutputStream();
				byte[] buffer = new byte[1024];
	            int bytesRead;
	            InputStream is = new FileInputStream(file);
	            while ((bytesRead = is.read(buffer)) != -1) {
	                os.write(buffer, 0, bytesRead);
	            }
				
				writer.close();
				os.close();
				is.close();
				socket.close();
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("incorrect path :" + path);
		}
	}
	
	protected String readBody(int len, Socket client, InputStream is) throws IOException {
		
		byte[] dataBytes = new byte[len];
		System.out.println("before~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		
		is.read(dataBytes);
		String s = new String(dataBytes, StandardCharsets.UTF_8);
		System.out.println("here~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		return s;
	}
	
	protected String sendAllImages() {
		URL url = this.getClass().getResource("../images");
		File folder = new File(url.getFile());
		ArrayList<String> filenames = listFilesForFolder(folder);
		String img_tag = "";
		
		for(int i = 0; i< filenames.size(); i++) {
			img_tag += "<p><img src="+ filenames.get(i)+ " alt=\":)\" width=60%></p>\n";
		}
		
		return img_tag;
	}
	
	private ArrayList<String> listFilesForFolder(final File folder) {
		System.out.println(folder.getAbsolutePath());
		ArrayList<String> filenames = new ArrayList<>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	            System.out.println(fileEntry.getName());
	            filenames.add(fileEntry.getName());
	        }
	    }
	    return filenames;
	}
	
	protected String generateToken(int size) {
		byte[] array = new byte[size];
		new Random().nextBytes(array);
		StringBuilder sb = new StringBuilder();
		for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
	}
	
	protected String generateHash(String in) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte hash_bytes[] = md.digest(in.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : hash_bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
	}
	
}





