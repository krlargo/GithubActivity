package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            // Get event type
            String type = event.getString("type");
            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Add type of event as header
            sb.append("<h3 class=\"type\">");
            sb.append(type);
            sb.append("</h3>");
            // Add formatted date
            sb.append(" on ");
            sb.append(formatted);
            sb.append("<br />");
            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");

            // Display commit history below
            sb.append("<br /> <div>");
            // Retrieve commits in JSON form
            JSONObject payload = event.getJSONObject("payload");
            JSONArray commits = payload.getJSONArray("commits");

            for (int currentCommit = 0; currentCommit < commits.length(); currentCommit++) {
                String commitHash = commits.getJSONObject(currentCommit).getString("sha");
                String commitMsg = commits.getJSONObject(currentCommit).getString("message");

                sb.append("Commit SHA (First 8 letters): ");
                sb.append(commitHash.substring(0, Math.min(commitHash.length(), 8)));
                sb.append("<br />Commit Message: ");
                sb.append(commitMsg);
                sb.append("<br /> <br />");
            }

            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        String url = BASE_URL + user + "/events";
        System.out.println(url);
        JSONObject json = Util.queryAPI(new URL(url));
        System.out.println(json);
        JSONArray events;

        int counter = 0;
        int pageNumber = 1;

        do {
            String url2 = url + "?page=" + pageNumber + "&";

            JSONObject currentPage = Util.queryAPI(new URL(url2));
            events = currentPage.getJSONArray("root");

            for (int i = 0; i < events.length() && counter < 10; i++) {
                String type = events.getJSONObject(i).getString("type");

                if (type.equals("PushEvent")) {
                    eventList.add(events.getJSONObject(i));
                    counter++;
                }
            }

            pageNumber++;

        } while(counter < 10 || events.length() != 0);

        return eventList;
    }
}