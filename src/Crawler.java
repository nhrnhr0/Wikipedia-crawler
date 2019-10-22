import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

class Crawler {
    private Node root;
    private int maxDepth;


    void setMaxDepth(int depth) {
        this.maxDepth = depth;
    }

    Crawler() {
    }

    Node getRoot() {
        return root;
    }

    boolean BFS(String startValue, String endValue) {

        root = new Node(startValue);
        root.RequestPage();
        while(!root.isLoaded());

        Log.Info("Crawler BFS: root node init");
        for(int i = 0; i < maxDepth; ++i) {
            Log.Info("Crawler BFS: starting level: " + (i+1));
            boolean found = searchOnLevel(root, i+1, endValue);
            if(found)
                return true;
        }
        return false;
    }


    private boolean searchOnLevel(Node root, int level, String target) {
        if (root == null) {
            Log.Error("Crawler searchOnLevel: root is null");
            return false;
        }

        if(level == 0)
            return false;

        if(level == 1) {
            if(root.getUrl().equals(target)) {
                Log.Info("Crawler searchOnLevel: target found");
                Log.Debug(root.getUrl() + " > ");
                return true;
            }
        }else if(level > 1) {

            // if any children not requested yet, then request them
            for(int i = 0;i < root.childrenSize(); ++i) {
                if(root.getChild(i).isRequested() == false)
                    root.getChild(i).RequestPage();

            }

            // call searchOnLevel to every child (going down one level)
            for(int i = 0; i < root.childrenSize(); i++) {
                while(!root.getChild(i).isLoaded()); // wait for the child to get loaded
                boolean found = searchOnLevel(root.getChild(i), level-1, target);
                if(found) {
                    Log.Debug(root.getUrl() + " > ");
                    return true;
                }
            }
        }
        return false;
    }



}
