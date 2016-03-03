import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.GeoData;
import com.flickr4java.flickr.photos.Photo;

import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class FlickrUtility {

    String lastId = null;

    Twitter twitter = TwitterFactory.getSingleton();

    public FlickrUtility(String consumerKey, String consumerSecret)
            throws TwitterException, IOException {
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the pin (if available) or hit enter. [PIN]:");
            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                } else {
                    accessToken = twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else {
                    te.printStackTrace();
                }
            }
        }

    }

    public void updateStatus(String apiKey, String sharedSecret, String userName)
            throws TwitterException, IOException, FlickrException {
        StatusUpdate flickrTweet = createFlickrTweet(apiKey, sharedSecret, userName);
        if (flickrTweet != null) {
            twitter.updateStatus(flickrTweet);
        }
    }

    private StatusUpdate createFlickrTweet(String apiKey, String sharedSecret, String userName)
            throws FlickrException, IOException {
        Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
        ArrayList<Photo> photos = flickr.getPeopleInterface()
                .getPhotos(userName, null, null, null, null, null, null, null, null, 1, 1);
        Photo photo = photos.get(0);

        /* Check to see if this photograph is new */
        if (lastId == null || photo.getId()
                .equals(lastId)) {
            /* If it isn't a new photograph, just return null */
            lastId = photo.getId();
            return null;
        }

        /* Map the title from Flickr to the text of the Tweet and include
        the photo's link so it can be viewed in high quality
         */
        String statusText = photo.getTitle() + ": " + photo.getUrl();
        StatusUpdate flickrTweet = new StatusUpdate(statusText);

        /* Set the media of the Tweet to be the photo itself */
        URL url = new URL(photo.getLargeUrl());
        flickrTweet.setMedia(photo.getLargeUrl(), url.openStream());

        /* Map GeoData from Flickr to a GeoLocation for Twitter */
        GeoData geoData = flickr.getGeoInterface()
                .getLocation(photo.getId());
        Float latitude = geoData.getLatitude();
        Float longitude = geoData.getLongitude();
        GeoLocation geoLocation = new GeoLocation(latitude, longitude);
        flickrTweet.setLocation(geoLocation);

        /* Updates the last posted picture to be this one so it isn't
        posted twice */
        lastId = photo.getId();
        return flickrTweet;
    }
}


