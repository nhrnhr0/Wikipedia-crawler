import java.util.Stack;

class Crawler {
    private Node root;
    private int maxDepth;
    private int currentSearchLvl;


    void setMaxDepth(int depth) {
        this.maxDepth = depth;
        this.currentSearchLvl = -1;
    }

    Crawler() {
    }

    Node getRoot() {
        return root;
    }
    int getMaxDepth() {return maxDepth;}
    int getCurrentSearchLvl() {return currentSearchLvl;}

    Stack<Node> BFS(String startValue, String endValue) {

        root = new Node(startValue);
        root.RequestPage();
        while(!root.isLoaded());

        Log.Info("Crawler BFS: root node init");
        for(int i = 0; i < maxDepth; ++i) {
            Log.Info("Crawler BFS: starting level: " + (i+1));
            currentSearchLvl = i+1;
            Stack<Node> found = searchOnLevel(root, i+1, endValue);
            if(found!= null)
                return found;
        }
        return null;
    }


    private Stack<Node> searchOnLevel(Node root, int level, String target) {
        if (root == null) {
            Log.Error("Crawler searchOnLevel: root is null");
            return null;
        }

        if(level == 0)
            return null;

        if(level == 1) {
            if(root.getUrl().equals(target)) {
                Log.Info("Crawler searchOnLevel: target found");
                Log.Debug(root.getUrl() + " > ");
                Stack<Node> ret = new Stack<Node>();
                ret.push(root);
                return ret;
            }
        }else if(level > 1) {

            // if any children not requested yet, then request them
            for(int i = 0;i < root.childrenSize(); ++i) {
                if(root.requestChild(i).isRequested() == false)
                    root.requestChild(i).RequestPage();

            }

            // call searchOnLevel to every child (going down one level)
            for(int i = 0; i < root.childrenSize(); i++) {
                while(!root.requestChild(i).isLoaded()); // wait for the child to get loaded
                Stack<Node> found = searchOnLevel(root.requestChild(i), level-1, target);
                if(found != null) {
                    Log.Debug(root.getUrl() + " > ");
                    found.push(root);
                    return found;
                }
            }
        }
        return null;
    }



}
