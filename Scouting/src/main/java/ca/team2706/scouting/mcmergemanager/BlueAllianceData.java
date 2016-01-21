package ca.team2706.scouting.mcmergemanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class BlueAllianceData {
    public String url;

    public  String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
    static class Item {
        String title;
        String link;
        String description;
    }

    static class Page {
        String title;
        String link;
        String description;
        String language;
        List<Item> items;
    }
//Example
  /*  public static void main(String[] args) throws Exception {

        String json = readUrl("http://www.javascriptkit.com/"
                + "dhtmltutors/javascriptkit.json");

        Gson gson = new Gson();
        Page page = gson.fromJson(json, Page.class);

        Log.e("yay",page.title);
        for (Item item : page.items)
            System.out.println("    " + item.title);
    }*/
}