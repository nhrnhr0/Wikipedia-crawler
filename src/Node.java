import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Node {
    private String url;
    private Future<WikiPage> futurePage;
    private boolean isRequested;
    private Node[] children;
    private int nodesAdded = 0;


    Node(String url) {
        this.url = url;
        isRequested = false;
        futurePage = null;
    }

    public String getUrl() {
        return url;
    }

    void RequestPage() {
        if(futurePage != null && futurePage.isDone()) {
            Log.Error("Node RequestPage: loaded page is requested");
            return;
        }
        if(isRequested) {
            Log.Error("Node RequestPage: requested page is requested");
            return;
        }
        futurePage = WikiEngine.requestPage(url);
        isRequested = true;
    }

    boolean isRequested() {
        return isRequested;
    }

    boolean isLoaded() {
        if(isRequested() == false) {
            Log.Error("Node isLoaded: called on a non requested page");
            return false;
        }
        if(futurePage == null)
            return false;
        return futurePage.isDone();
    }

    public WikiPage getPage() {
        if(isRequested() == false) {
            Log.Error("Node RequestPage: trying to get an unrequested page");
            return null;
        }
        if(isLoaded()) {
            try {
                return futurePage.get();
            } catch (InterruptedException e) {
                Log.Error("Node getPage: InterruptedException");
                e.printStackTrace();
            } catch (ExecutionException e) {
                Log.Error("Node getPage: ExecutionException");
                e.printStackTrace();
            }
        }
        return null;
    }

    Node getChild(int i) {
        if(childrenSize() <= i) {
            Log.Error("Node getChild got " + i + " when page.links.size() = " + childrenSize());
            return null;
        }

        if(children[i] == null) {
            Node n = new Node(getPage().links.get(i));
            Log.Debug("Node getChild: " + ++nodesAdded + ") adding to node " + getUrl() + " > " + n.getUrl());
            children[i] = n;
        }
        return children[i];
    }


    int childrenSize() {
        if(isLoaded()) {
            int size =  getPage().links.size();
            if(children == null) {
                children = new Node[size];
            }
            return size;
        }
        Log.Error("Node childrenSize: on a no loaded page");
        return -1;
    }



    private boolean _isFirstCheck = true;
    public boolean isFirstCheck() {
        if(_isFirstCheck) {
            _isFirstCheck = false;
            return true;
        }
        return false;
    }
}



/*import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Node {
    private WikiPage page;
    private Node[] children;
    Node getChild(int i) {
        if(page.links.size() <= i) {
            Log.Error("Node getChild got " + i + " when page.links.size() = " + page.links.size());
            return null;
        }

        if(children[i] == null) {
            Node n = new Node(page.links.get(i));
            children[i] = n;
        }
        return children[i];
    }

    String getUrl() {
        return page.url;
    }

    Node(final String url) {
        this(WikiEngine.get(url));
    }

    int childrenSize() {
        return page.links.size();
    }

    private Node(WikiPage page) {
        Log.Info("Node Node: adding new page " + page.url);
        this.page = page;
        children = new Node[this.page.links.size()];
    }

}
*/