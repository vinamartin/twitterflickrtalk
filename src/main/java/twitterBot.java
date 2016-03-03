import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.flickr4java.flickr.FlickrException;

import twitter4j.TwitterException;

public class twitterBot {
    public static void main(String args[]) throws Exception {
        /*
        Command Line args
         */
        String consumerKey = args[0];
        String consumerSecret = args[1];
        final String apiKey = args[2];
        final String sharedSecret = args[3];
        final String flickrUserName = args[4];

        Timer timer = new Timer();
        final FlickrUtility flickrUtility = new FlickrUtility(consumerKey, consumerSecret);

        TimerTask minutelyTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    flickrUtility.updateStatus(apiKey, sharedSecret, flickrUserName);
                } catch (TwitterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FlickrException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(minutelyTask, 0l, 60000);

    }
}



