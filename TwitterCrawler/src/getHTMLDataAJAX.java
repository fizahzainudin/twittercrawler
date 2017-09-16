

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

public class getHTMLDataAJAX {

	
	public static SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.37";
	public static int tweet_count = 0;
	public static String DBurl = "jdbc:postgresql://localhost:5432/TwitterScraper";
	public static int log_sequence = 0;
	
	public static String run_ID = "searching hurricane harvey 2017-07-01 to 2017-08-27 Rerun";
	
	
	public static String searchterm = "hurricane harvey";
	public static String startdate = "2017-01-01";
	public static String endate = "2017-08-28";
	
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		
		calStart.setTime(simpleDate.parse(startdate));
		calEnd.setTime(simpleDate.parse(endate));
		
		Date endDAteStop =  calStart.getTime();
		
		
		Date DateBoundUpper = calEnd.getTime();
		calEnd.add(Calendar.DATE, -1);
		Date DateBoundLower = calEnd.getTime();
		
		
		while (!DateBoundUpper.equals(endDAteStop)) {
			
			System.out.println("Upper:" + DateBoundUpper);
			writeToDBlog("Upper:" + DateBoundUpper);
			System.out.println("Lower:" + DateBoundLower);
			writeToDBlog("Lower:" + DateBoundLower);
			
			System.out.println("Calling robot");
			writeToDBlog("Calling robot");
			robotEngine(DateBoundLower, DateBoundUpper, searchterm);
			
			DateBoundUpper = DateBoundLower;
			calEnd.add(Calendar.DATE, -1);
			DateBoundLower = calEnd.getTime();
			
			
		}
				
		System.out.println("Robot Terminated");
		writeToDBlog("Robot Terminated");

	}
	
	public static void writeToDB(String TweetID, String userName, String Tweet, String timeStamp, String geolocation, String Run_ID, String tweetNo) throws Exception {
		
		Class.forName("org.postgresql.Driver");
		
		
		
		Properties props = new Properties();
		props.setProperty("user","postgres");
		props.setProperty("password","password1");

		Connection conn = DriverManager.getConnection(DBurl, props);
		
		Statement st = conn.createStatement();
		
		try {
			st.executeQuery("insert into tweet select '" + TweetID + "', '" + userName + "', '" + Tweet + "', '" + timeStamp + "', '" + geolocation + "', '" + Run_ID + "' ,'" + tweetNo + "'");
		} catch (Exception E) {
			
			if (E.toString().equals("org.postgresql.util.PSQLException: No results were returned by the query.")) System.out.println("Committed to DB");
		}
		finally {
			conn.close();
			writeToDBlog("Committed to DB");
		}
		
		conn.close();
		
	}
	
	public static void writeToDBlog(String log_message) throws Exception {
		
		
		Class.forName("org.postgresql.Driver");
		
		
		
		Properties props = new Properties();
		props.setProperty("user","postgres");
		props.setProperty("password","password1");

		Connection conn = DriverManager.getConnection(DBurl, props);
		
		Statement st = conn.createStatement();

		
		try {
			st.executeQuery("insert into log select '"  + run_ID + "' ,'" +  new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "', '" + log_message  + "', '" + log_sequence + "'");
		} catch (Exception E) {
			
			if (E.toString().equals("org.postgresql.util.PSQLException: No results were returned by the query.")) System.out.println("Committed to DB");
		}
		finally {
			conn.close();
			//writeToDBlog("Committed to DB");
		}
		
		conn.close();
		log_sequence++;
	}
	
	public static void robotEngine (Date startDate, Date endDate, String searchTerms) throws Exception{
		
		
		String searchString = getSearchString(startDate, endDate, searchterm);
		System.out.println(searchString);
		writeToDBlog("searchString: " + searchString);
		
		String twitterIDString = parseHTML(searchString) ;
		
		String searchParameters = searchString.substring(searchString.indexOf("&q="), searchString.indexOf("&src"));
		System.out.println(searchParameters);
		writeToDBlog("searchParameters: " + searchParameters);
		
		System.out.println(twitterIDString);
		
		if (twitterIDString.equals("The End")) return;
		
		JSONCall("https://twitter.com/i/search/timeline?vertical=default"+searchParameters+"&src=typd&include_available_features=1&include_entities=1&max_position=TWEET-" + twitterIDString + "-BD1UO2FFu9QAAAAAAAAETAAAAAcAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&reset_error_state=false", twitterIDString.substring(twitterIDString.indexOf("-")+1, twitterIDString.length()));
	   
	    
		
	}
	
	public static String convertDateString(Date dateString) throws Exception {
		
		//Date convertedDate = simpleDate.parse(dateString);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateString);
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		String MonthText = month + "";
		String DayText = day + "";
		
		if  (MonthText.length() == 1)  MonthText = "0" +  MonthText;
		
		if  (DayText.length() == 1)  DayText = "0" +  DayText;

		String dateInString = year + "-" + MonthText + "-" +  DayText;
		
		return dateInString;
		
	}
	
	public static String getSearchString(Date SearchStartDate, Date SearchEndDate, String searchterm) throws Exception {
		

		String startDateString = convertDateString(SearchStartDate);
		String endDateString = convertDateString(SearchEndDate);
		
		String searchterms[] = searchterm.split(" ");
		String terms = "";
		int termcount = 0;

		
		while(termcount < searchterms.length) {
			terms = terms + searchterms[termcount] + "%20";
			termcount++;
		}
		
		System.out.println(terms);
		writeToDBlog("terms: " + terms);
		
		String searchString = "https://twitter.com/search?l=&q=" + terms + "since%3A" + startDateString + "%20until%3A" + endDateString + "&src=typd";
		
		return searchString;
	}
	
	public static String parseHTML(String urlString) throws Exception {
		
		URL searchURL = new URL(urlString);
		
		URLConnection hc = searchURL.openConnection();
        hc.setRequestProperty("User-Agent", userAgent);
        
		
		String userName = null;
		String TwitterID = null;
		String Tweet = null;
		String timeStamp = null;
		String JSONTweetID = null;
		
		InputStream input = hc.getInputStream();
		
		InputStreamReader isr2 = new InputStreamReader(input);
		
		BufferedReader in = new BufferedReader(isr2);
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) {

			if(inputLine.indexOf("data-max-position=\"TWEET-") >= 0 && JSONTweetID == null) {
				
				if (inputLine.indexOf("data-max-position=\"TWEET--\" data-min-position=\"TWEET--\"") >=0) return "The End";
				
				JSONTweetID = inputLine.substring(inputLine.indexOf("data-max-position=\"TWEET-") + 25, inputLine.indexOf("BD1UO2FFu9QA")-1);
		        
		    }
		       //System.out.print("here\n" + userName);     
		    if(inputLine.indexOf("data-permalink-path") >= 0 && userName == null) {
		       	System.out.println("Found Tweet");
		       	writeToDBlog("Found Tweet");
		       	tweet_count++;
		       	System.out.println("Tweet no:" + tweet_count);
		       	writeToDBlog("Tweet no:" + tweet_count);
		       	userName = inputLine.substring(22, inputLine.indexOf("/status/"));
		       	System.out.println("Username:" + userName);
		       	writeToDBlog("userName:" + userName);
		       	TwitterID = inputLine.substring(inputLine.indexOf("/status/")+8,inputLine.length());
		       	System.out.println("Twitter ID:" + TwitterID);
		       	writeToDBlog("TwitterID:" + TwitterID);

		    }

		    if(inputLine.indexOf("tweet-timestamp js-permalink js-nav js-tooltip\" title=\"") >= 0 && timeStamp == null) {
		    	
		       	timeStamp = inputLine.substring(inputLine.indexOf("tweet-timestamp js-permalink js-nav js-tooltip\" title=\"") + 55, inputLine.indexOf("data-conversation-id=")-3);
		       	System.out.println("TimeStamp: " + timeStamp);
		       	writeToDBlog("timeStamp: " + timeStamp);
		        
		       	
		    }
		    
		    if(inputLine.indexOf("<p class=\"TweetTextSize") >= 0 && Tweet == null) {
		    	Tweet = inputLine.substring(inputLine.indexOf("<p class=\"TweetTextSize") + 86, inputLine.length());
		        System.out.println("Tweet :" + Tweet);
		        writeToDBlog("Tweet: " + Tweet);
		        
		        writeToDB(TwitterID, userName, Tweet, timeStamp, "N/A", run_ID, String.valueOf(tweet_count));
		       	
		       	userName = null;
		    	TwitterID = null;
		    	Tweet = null;
		    	timeStamp = null;
		    }
		            
		            
		}
		in.close();
		
		        
		if(JSONTweetID == null) {
			return "The End";
		}
		return JSONTweetID;
		
	}
	
	public static void JSONCall(String JSONCallString, String staticTweetID) throws Exception {
	    

		int json_count = 1;
		
		System.out.println("Starting JSON Call");
		writeToDBlog("Starting JSON Call");
		System.out.println("Starting JSON Call no:" + json_count);
		writeToDBlog("Starting JSON Call no:" + json_count);
	    //String LastTweetID = processTweets("https://twitter.com/i/search/timeline?vertical=default&q=storm%20desmond&src=typd&include_available_features=1&include_entities=1&max_position=TWEET-887797549733695491-896002744108146689-BD1UO2FFu9QAAAAAAAAETAAAAAcAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&reset_error_state=false");
		
		
		String LastTweetID = processTweets(JSONCallString);
		
		//System.out.println("https://twitter.com/i/search/timeline?vertical=default&q=" + searchterm1 + "%20"+ searchterm2 +"%20since%3A2015-12-10%20until%3A2015-12-11&src=typd&include_available_features=1&include_entities=1&max_position=TWEET-675088042596634624-675101975634157568-BD1UO2FFu9QAAAAAAAAETAAAAAcAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&reset_error_state=false");
		
		System.out.println("End of JSON call");
		writeToDBlog("End of JSON call");
	    System.out.println("Last tweet ID:" + LastTweetID);
	    writeToDBlog("Last tweet ID:" + LastTweetID);
	    String CurrentLastTweetID = null;
	    
	    String searchParameters = JSONCallString.substring(JSONCallString.indexOf("&q="), JSONCallString.indexOf("&src"));
	    
	    System.out.println(LastTweetID.equals("THE END"));
	    
	    //while (!(CurrentLastTweetID.equals(LastTweetID) || CurrentLastTweetID.equals("THE END"))) {
	    while (!(LastTweetID.equals("THE END"))) {
	    	
	    	CurrentLastTweetID = LastTweetID;
	    	json_count++;
	    	//System.out.println("JSON call: https://twitter.com/i/search/timeline?vertical=default&q=storm%20desmond&src=typd&include_available_features=1&include_entities=1&max_position=TWEET-" + LastTweetID + "-896002744108146689-BD1UO2FFu9QAAAAAAAAETAAAAAcAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&reset_error_state=false");
	    	System.out.println("Starting JSON Call");
	    	writeToDBlog("Starting JSON Call");
	    	System.out.println("Starting JSON Call no:" + json_count);
	    	writeToDBlog("Starting JSON Call no:" + json_count);
	    	LastTweetID = processTweets("https://twitter.com/i/search/timeline?vertical=default" + searchParameters + "&src=typd&include_available_features=1&include_entities=1&max_position=TWEET-" + LastTweetID + "-" + staticTweetID + "-BD1UO2FFu9QAAAAAAAAETAAAAAcAAAASAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&reset_error_state=false");
	    	System.out.println("End of JSON call");
	    	writeToDBlog("End of JSON call");
	    	System.out.println("Last tweet ID:" + LastTweetID);
	    	writeToDBlog("Last tweet ID:" + LastTweetID);
	    }
	
	}
	
	public static String processTweets(String URLtoLoad) throws Exception{
		
		URL website = new URL(URLtoLoad);
		
	    URLConnection hc = website.openConnection();
        hc.setRequestProperty("User-Agent", userAgent);
        
        InputStream input = hc.getInputStream();
        
        InputStreamReader isr2 = new InputStreamReader(input);
	    BufferedReader br2 = new BufferedReader(isr2);
        
	    String thisLine = null;
	    String parts[] = null;
	    String parts_tweets[] = null;
	    String Tweets = null;
	    String Username = null;
	    String TweetID = null;
	    String Tweet = null;
	    String TimeStamp = null;
	    
	    System.out.println("Parsing JSON file");
	    writeToDBlog("Parsing JSON file");
	    
	    int indexno = 0;
	    int no_of_tweets = 0;
	    int current_tweet_count = 1;
	    while ((thisLine = br2.readLine()) != null) {

            parts = thisLine.split("\":\"");


            indexno = parts[2].indexOf("ndata-permalink-path");

            

            if (indexno == -1) return "THE END";
            
            Tweets = parts[2].substring(indexno);
            
            
            parts_tweets = Tweets.split("ndata-permalink-path");
            
            no_of_tweets = parts_tweets.length;
            
            while (current_tweet_count <  no_of_tweets)	{
            	//System.out.println("Marker 7:" + parts_tweets[current_tweet_count]);
            	
            	Username = parts_tweets[current_tweet_count].substring(5, parts_tweets[current_tweet_count].indexOf("\\/status\\/"));
            	
            	System.out.println("Tweet found");
            	writeToDBlog("Tweet found");
            	tweet_count++;
            	System.out.println("Tweet no:" +tweet_count);
            	writeToDBlog("Tweet no:" +tweet_count);
            	System.out.println("Start Reading Tweet");
            	writeToDBlog("Start Reading Tweet");
            	System.out.println("Username:" + Username);
            	writeToDBlog("Username: " + Username);
            	
            	
            	//System.out.println("Marker 9:" + (parts_tweets[current_tweet_count].indexOf("\\/status\\/") + 10));
            	
            	//TweetID = parts_tweets[current_tweet_count].substring(parts_tweets[current_tweet_count].indexOf("\\/status\\/")+10, parts_tweets[current_tweet_count].indexOf("\ndata-conversation-id="));
            	
            	//TweetID = parts_tweets[current_tweet_count].substring(parts_tweets[current_tweet_count].indexOf("\\/status\\/")+10, parts_tweets[current_tweet_count].indexOf("\ndata-conversation-id="));
            	
            	
            	TweetID = parts_tweets[current_tweet_count].substring(parts_tweets[current_tweet_count].indexOf("\\/status\\/")+10, parts_tweets[current_tweet_count].indexOf("ndata-conversation-id=")-3);
            	
            	System.out.println("Tweet ID:" + TweetID);
            	writeToDBlog("TweetID: " + TweetID);
            	
            	Tweet = parts_tweets[current_tweet_count].substring(parts_tweets[current_tweet_count].lastIndexOf("data-aria-label-part=")+20, parts_tweets[current_tweet_count].indexOf("stream-item-footer"));
            	
            	System.out.println("Tweet:" + Tweet);
            	writeToDBlog("Tweet: " + Tweet);
            	
            	TimeStamp = parts_tweets[current_tweet_count].substring(parts_tweets[current_tweet_count].indexOf("tweet-timestamp js-permalink js-nav js-tooltip")+56, parts_tweets[current_tweet_count].lastIndexOf("data-conversation-id")-2);
            	
            	System.out.println("Timestamp:" + TimeStamp);
            	writeToDBlog("TimeStamp: " + TimeStamp);
            	
            	writeToDB(TweetID, Username, Tweet, TimeStamp, "N/A", run_ID, String.valueOf(tweet_count));
            	
            	current_tweet_count++;
            }
         }  
		
		return TweetID;
	}

}
