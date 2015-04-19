package pe.chalk.takoyaki;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pe.chalk.takoyaki.data.Data;
import pe.chalk.takoyaki.data.Menu;
import pe.chalk.takoyaki.filter.*;
import pe.chalk.takoyaki.logger.Prefix;
import pe.chalk.takoyaki.logger.PrefixedLogger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author ChalkPE <amato0617@gmail.com>
 * @since 2015-04-07
 */
public class Target extends Thread implements Prefix {
    private static final String STRING_CONTENT = "http://cafe.naver.com/%s";
    private static final String STRING_ARTICLE = "http://cafe.naver.com/ArticleList.nhn?search.clubid=%d&search.boardtype=L";

    private static final Pattern PATTERN_CLUB_ID = Pattern.compile("var g_sClubId = \"(\\d+)\";");

    private URL contentUrl;
    private URL articleUrl;

    private Takoyaki takoyaki;
    private String prefix;
    private PrefixedLogger logger;

    private long interval;
    private int timeout;

    private Collector collector;

    private final String address;
    private final int clubId;
    private List<Menu> menus;

    public Target(Takoyaki takoyaki, JSONObject jsonObject) throws JSONException, IOException {
        this.takoyaki = takoyaki;
        this.prefix = jsonObject.getString("prefix");
        this.logger = this.getTakoyaki().getLogger().getPrefixed(this);

        this.interval = jsonObject.getLong("interval");
        this.timeout = jsonObject.getInt("timeout");

        JSONArray filtersArray = jsonObject.getJSONArray("filters");
        List<Filter<? extends Data>> filters = new ArrayList<>(filtersArray.length());

        for(int i = 0; i < filtersArray.length(); i++){
            JSONObject filterObject = filtersArray.getJSONObject(i);

            Filter<? extends Data> filter;
            switch(filterObject.getString("type")){
                case ArticleFilter.NAME:
                    filter = new ArticleFilter(this, filterObject);
                    break;
                case CommentaryFilter.NAME:
                    filter = new CommentaryFilter(this, filterObject);
                    break;
                case VisitationFilter.NAME:
                    filter = new VisitationFilter(this, filterObject);
                    break;
                default:
                    continue;
            }
            filters.add(filter);
        }
        this.collector = new Collector(filters);

        this.address = jsonObject.getString("address");
        this.contentUrl = new URL(String.format(STRING_CONTENT, this.getAddress()));

        Document contentDocument = Jsoup.parse(this.contentUrl, this.getTimeout());
        this.setName(contentDocument.select("h1.d-none").text());
        Matcher clubIdMatcher = Target.PATTERN_CLUB_ID.matcher(contentDocument.head().getElementsByTag("script").first().html());
        if(!clubIdMatcher.find()){
            throw new JSONException("Cannot find menuId of " + this.getName());
        }
        this.clubId = Integer.parseInt(clubIdMatcher.group(1));
        this.menus = contentDocument.select("a[id^=menuLink]").stream()
                .map(element -> new Menu(this, Integer.parseInt(element.id().substring(8)), element.text()))
                .collect(Collectors.toList());

        String[] strings = new String[this.menus.size()];
        for(int i = 0; i < this.menus.size(); i++){
            strings[i] = this.menus.get(i).toString();
        }
        Files.write(Paths.get(this.getAddress() + "-menus.json"), new JSONArray(strings).toString(4).getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        this.articleUrl = new URL(String.format(STRING_ARTICLE, this.getClubId()));

        this.getLogger().debug("카페명: " + this.getName() + " (ID: " + this.getClubId() + ")");
        this.getLogger().debug("게시판 수: " + this.getMenus().size() + "개\n");
    }

    public Takoyaki getTakoyaki(){
        return this.takoyaki;
    }

    public PrefixedLogger getLogger(){
        return this.logger;
    }

    public long getInterval(){
        return this.interval;
    }

    public int getTimeout(){
        return this.timeout;
    }

    public String getAddress(){
        return this.address;
    }

    public int getClubId(){
        return this.clubId;
    }

    public List<Menu> getMenus(){
        return this.menus;
    }

    public Menu getMenu(int menuId){
        for(Menu menu : this.getMenus()){
            if(menu.getId() == menuId){
                return menu;
            }
        }
        return null;
    }

    @Override
    public String getPrefix(){
        return this.prefix;
    }

    @Override
    public void run(){
        while(this.getTakoyaki().isAlive()){
            try{
                Thread.sleep(this.getInterval());

                Document contentDocument = Jsoup.parse(this.contentUrl, this.getTimeout());
                Document articleDocument = Jsoup.parse(this.articleUrl, this.getTimeout());

                this.collector.collect(contentDocument, articleDocument);
            }catch(IOException e){
                this.getLogger().error(e.getMessage());
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}