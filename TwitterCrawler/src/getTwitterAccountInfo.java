import java.io.*;
//import org.apache.commons.io.*;
import java.net.*;
//import java.nio.channels.Channels;
//import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.sql.*;


public class getTwitterAccountInfo {

	public static SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.37";

	public static String DBurl = "jdbc:postgresql://localhost:5432/TwitterScraper";
	
	public static int log_sequence = 0;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		getHTMLDataAJAX htmlAjax = new getHTMLDataAJAX();
		htmlAjax.run_ID = "Get twitter account info";
		System.out.println("TEST");
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
		Properties props = new Properties();
		props.setProperty("user","pos	gres");
		props.setProperty("password","password1");
		
		Statement st = null;
		Connection conn = null;
		
		Statement stWrite = null;
		Connection connWrite = null;
		
		try {
			conn = DriverManager.getConnection(DBurl, props);
			st = conn.createStatement();
			
			connWrite = DriverManager.getConnection(DBurl, props);
			stWrite = conn.createStatement();
		}catch (Exception e) {
			System.out.println(e);
		}
		
		
		ResultSet rs = null;
		
		String username = null;
		String userURL = null;
		
		URL searchURL = null;
		
		URLConnection hc = null;
		
		InputStream input = null;
		
		InputStreamReader isr2 = null;
		
		BufferedReader in = null;
		
        String inputLine = null;
        String name = null;
        String bio = null;
        String location = null;
        String profile_url = null;
        String join_date = null;
        String verified = "N";
        
		try {
			rs = st.executeQuery("select username from twitter_users where data_collected = 'N'");
			
			while(rs.next()) {
				
				System.out.println("HERE TOP");
				
				username = rs.getString(1);
				
				System.out.println(username);
				userURL = "https://twitter.com/" + username;
				
				System.out.println(userURL);
				
				searchURL = new URL(userURL);
				
				hc = searchURL.openConnection();
				hc.setRequestProperty("User-Agent", userAgent);
				try
				{
					input = hc.getInputStream();
					isr2 = new InputStreamReader(input);
					in = new BufferedReader(isr2);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e);
					continue;
				}
				
				
				while ((inputLine = in.readLine()) != null) {
					
					//System.out.println(inputLine);
					
					if(inputLine.indexOf("<title>") > 0) {
						System.out.println("User found");
						//System.out.println(inputLine.indexOf("<title>") + 7);
						//System.out.println(inputLine.indexOf("(")-1);
						name = inputLine.substring(inputLine.indexOf("<title>") + 7, inputLine.indexOf("(@")-1);
						System.out.println("Name: " + name);
					}
					
					if(inputLine.indexOf("meta name=\"description\"") > 0) {
						//System.out.println(inputLine);
						bio = inputLine.substring(inputLine.indexOf("meta name=\"description\"") + 33, inputLine.indexOf("\">"));
						System.out.println("bio: " + bio);
					}
					
					if(inputLine.indexOf("&quot;location&quot;:&quot;") > 0) {
					
						location = inputLine.substring(inputLine.indexOf("&quot;location&quot;:&quot;") +26, inputLine.length());
						
						location = location.substring(1,location.indexOf("&"));
						
						System.out.println("location: " + location);
					}
					
					if(inputLine.indexOf("ProfileHeaderCard-urlText u-dir") > 0) {
						

						
						profile_url = inputLine.substring(inputLine.indexOf("title") + 7, inputLine.length()-2);
						
						System.out.println("URL in Profile: " + profile_url);
					}
					
					if(inputLine.indexOf("ProfileHeaderCard-joinDateText") > 0) {
						join_date = inputLine.substring(inputLine.indexOf("title=") + 7, inputLine.indexOf(">")-1);
						
						System.out.println("Join Date: " + join_date);
					}
					
					if(inputLine.indexOf("verified&quot;:true,&quot") > 0) {
						verified = "Y";
					}
					
				}
				
				try {
					stWrite.executeQuery("update twitter_users set \"Name\" = '" + name + "', description = '" + bio + "', geo_location = '" + location + "',  joined_date = '" + join_date + "',  \"URL\" = '" + profile_url + "', verified_account = '" + verified + "', data_collected = 'Y' where username = '" + username + "'");
				} catch (Exception e) {}
				System.out.println("HERE A");
				verified = "N";
			}
			
		} catch (Exception E) {
			
			System.out.println(E);
			
			E.printStackTrace();
		}
			
		
		
	}

}
