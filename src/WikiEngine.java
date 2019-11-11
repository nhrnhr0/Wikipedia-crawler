





import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.Location;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiEngine {
    private static final int chromeDriverSize = 4;
    private static ChromeDriver[] drivers = new ChromeDriver[chromeDriverSize];
    private static final String URL_BASE = "https://en.wikipedia.org/wiki/";
    private static boolean isInit = false;
    private static ExecutorService executeService;
    private static int requestedPagesCount = 0;
    private static int loadedPageCount = 0;
    private static int totalLinksFound = 0;
    private static final String LINK_REGEX = "href\\s*=\\s*\\\"\\/wiki\\/([^\"&^:]*)\\\"";
    private static final Pattern linkPattern = Pattern.compile(LINK_REGEX);
    private static Instant engineTimer;


    private WikiEngine() {
        Log.Error("WikiEngine ctor: the ctor should never be called");
    }

    public static int getTotalLinksFound() { return totalLinksFound; }
    public static int getRequestedPagesCount() {
        return requestedPagesCount;
    }
    public static int getLoadedPageCount() {
        return loadedPageCount;
    }

    private static void Init() {
        executeService = Executors.newFixedThreadPool(chromeDriverSize);
        System.setProperty("webdriver.chrome.driver", "chromedriver77.exe");
        //driver = new ChromeDriver();
        int yPosOffset = 85;
        for(int i = 0;i < chromeDriverSize; i++) {
            drivers[i] = new ChromeDriver();
            drivers[i].manage().window().setPosition(new Point(1300,0 + yPosOffset*i));
        }
        engineTimer = Instant.now();
        isInit = true;
    }

    static void close() {
        if(executeService != null)
            executeService.shutdownNow();
        for(int i = 0;i < drivers.length;i++) {
            if(drivers[i] != null) {
                drivers[i].close();
            }
        }
        Log.Info("WikiEngine is closed");
    }

    public static Future<WikiPage> requestPage(String url) {
        if(isInit == false)
            Init();
        return executeService.submit(new PageRequest(url, requestedPagesCount++%chromeDriverSize));
    }

    static float calcEngineSpeed() {
        if(engineTimer == null) {
            return 0.0f;
        }
        float secsFromStart = ChronoUnit.MILLIS.between(engineTimer, Instant.now()) / 1000.0f;
        float ret = (getLoadedPageCount() / secsFromStart)*60;
        return ret;
    }


    private static class PageRequest implements Callable<WikiPage> {
        private String url;
        private int driverIndex;
        public PageRequest(String pageUrl, int driverIndex) {
            this.url = pageUrl;
            this.driverIndex = driverIndex;
        }

        @Override
        public WikiPage call() throws Exception {
            String innerHTML = "";
            String title = "";
            synchronized (drivers[driverIndex]) {
                Timer.Start("PageRequest_call driver sync_" + driverIndex);
                drivers[driverIndex].get(URL_BASE + url);
                innerHTML = drivers[driverIndex].findElement(By.id("mw-content-text")).getAttribute("innerHTML");
                title = drivers[driverIndex].findElement(By.id("firstHeading")).getText();
                Timer.Stop("PageRequest_call driver sync_" + driverIndex);
            }

            Timer.Start("PageRequest_call collect links_" + driverIndex);
            HashSet<String> linkSet = new HashSet<>();
            Matcher m = linkPattern.matcher(innerHTML);
            while(m.find()) {
                String link = m.group(1);
                linkSet.add(link);
            }
            WikiPage retPage = new WikiPage();
            retPage.url = url;
            retPage.title = title;
            retPage.links = new ArrayList<>(linkSet.size());
            retPage.links.addAll(linkSet);
            ++loadedPageCount;
            totalLinksFound += linkSet.size();
            Timer.Stop("PageRequest_call collect links_" + driverIndex);
            return retPage;
        }
    }
}