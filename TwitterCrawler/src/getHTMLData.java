
import net.htmlparser.jericho.*;
import java.util.*;

import javax.print.attribute.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import java.io.*;
import java.net.*;

public class getHTMLData {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		System.out.println("Hello there");
		
		/*String sourceUrlString="http://www.twitter.com";
		
		Source source=new Source(new URL(sourceUrlString));
		
		System.out.println("Test:" + source);
		String renderedText=source.getRenderer().toString();
		
		System.out.println("\nSimple rendering of the HTML document:\n");
		System.out.println(renderedText);*/
		
	    URL url = new URL("https://twitter.com/search?q=storm%20desmond&src=typd");
	    URLConnection connection = url.openConnection();
	    System.out.println("Marker 1:" + connection);
	    InputStream is = connection.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    System.out.println("Marker 2:" + br);
	    
	    String thisLine = null;
	    
	    //System.out.println("TEST 2:" + br.readLine());
	    int lineno = 0;
	    while ((thisLine = br.readLine()) != null) {
            System.out.println(lineno +":"+ thisLine);
            lineno++;
         }       

	    System.out.println("Marker 3:" + br);
	    lineno = 0;
	    while ((thisLine = br.readLine()) != null) {
            System.out.println(lineno +":"+ thisLine);
            lineno++;
         }   
	    System.out.println("Marker 4:" + br);
	    
	    HTMLEditorKit htmlKit = new HTMLEditorKit();
	    HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
	    HTMLEditorKit.Parser parser = new ParserDelegator();
	    HTMLEditorKit.ParserCallback callback = htmlDoc.getReader(0);
	    parser.parse(br, callback, true);

	    for (HTMLDocument.Iterator iterator = htmlDoc.getIterator(HTML.Tag.A); iterator.isValid(); iterator
	        .next()) {

	      javax.swing.text.AttributeSet attributes = iterator.getAttributes();
	      String srcString = (String) attributes.getAttribute(HTML.Attribute.HREF);
	      System.out.print(srcString);
	      int startOffset = iterator.getStartOffset();
	      int endOffset = iterator.getEndOffset();
	      int length = endOffset - startOffset;
	      String text = htmlDoc.getText(startOffset, length);
	      System.out.println("  " + text);
	    }
		
	}

}
