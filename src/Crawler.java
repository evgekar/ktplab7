import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.LinkedList;


public class Crawler {

    // just alias for depth which is ignored in URLDepthPair.equals()
    final static int AnyDepth = 0;


    private LinkedList<URLDepthPair> m_Processed = new LinkedList<URLDepthPair>();
    private LinkedList<URLDepthPair> m_NotProcessed = new LinkedList<URLDepthPair>();

    private int m_Depth;
    private String m_StartHost;
    // prefix has no slash to support https too
    private String m_Prefix = "http";

    public Crawler(String host, int depth) {
        m_StartHost = host;
        m_Depth = depth;
        m_NotProcessed.add(new URLDepthPair(m_StartHost, m_Depth));
    }

    public void Scan() throws IOException {

       while (m_NotProcessed.size() > 0) {
           Process(m_NotProcessed.removeFirst());
           }
       }


    public void Process(URLDepthPair pair) throws IOException{
        // set up a connection and follow the redirect
        URL url = new URL(pair.getURL());
        URLConnection connection = url.openConnection();
        String redirect = connection.getHeaderField("Location");
        if (redirect != null) {
            connection = new URL(redirect).openConnection();
        }
        m_Processed.add(pair);
        if (pair.getDepth() == 0) return;

        // reading references
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        while ((input = reader.readLine()) != null) {
            while (input.contains("a href=\"" + m_Prefix)) {
                input = input.substring(input.indexOf("a href=\"" + m_Prefix) + 8);
                String link = input.substring(0, input.indexOf('\"'));
                if(link.contains(" "))
                    link = link.replace(" ", "%20");
                // avoid multiple visiting of the same link
                if (m_NotProcessed.contains(new URLDepthPair(link, AnyDepth)) ||
                        m_Processed.contains(new URLDepthPair(link, AnyDepth))) continue;
                m_NotProcessed.add(new URLDepthPair(link, pair.getDepth() - 1));
            }
        }
        // close the connection
        reader.close();

    }

    public void getSites() {
        // printing the links
        for (var elem : m_Processed)
            System.out.println(elem.getURL());
        System.out.println("Links visited: " + m_Processed.size());
    }


}
