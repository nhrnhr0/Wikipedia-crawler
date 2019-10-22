import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WikiPage {

    public String url;
    public String title;
    public List<String> links;

    WikiPage() {
        links = new ArrayList<>();
    }
}
