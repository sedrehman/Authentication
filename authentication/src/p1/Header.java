package p1;

public class Header {
	
	public Header() {
		//nada;
	}


	public  static String chop(String in) {
	    if ((in != null) && (in.length() > 0) && (in.charAt(in.length() - 1) == '\n' )) {
	        in = in.substring(0, in.length() - 1);
	    }
	    return in;
	}
	
	public String createHeader(String type, String cookie, double size ) {
		return ( "HTTP/1.1 200 OK\nLocation: /\n" + "Content-Type: "+ type +"\n" + "Set-Cookie: token=" + cookie +"; Path=login.html; Expires: Wed, 28 Feb 2021 13:42:32 GMT\n" +"Connection: keep-alive\n" + Double.toString(size) + "\n");
	}
	
	public String create301Header(String location) {
		 return "HTTP/1.1 301 OK\nLocation: "+ location + "\n"; 
	}
	
	public String create404Header() {
		return "HTTP/1.1 404 Not Found\n";
	}
	
	public String createWebSocketHeader(String key) {
		return "HTTP/1.1 101 Switching Protocols\nUpgrade: websocket\nConnection: Upgrade\nSec-WebSocket-Accept: " + key + "\n";
	}
}
