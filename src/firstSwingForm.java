import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class firstSwingForm {
    private JPanel config;
    private JTextField startTxt;
    private JTextField endTextField;
    private JPanel mainPanel;
    private JTextField maxDepthTextField;
    private JButton startBtn;
    private JTree wikiTree;
    private JPanel treePanel;
    private JPanel data;
    private JLabel pageReqCountLbl;
    private JLabel pageLoadCountLbl;
    private JButton reloadTreeBtn;
    private static firstSwingForm instance;
    private static Thread crawlerThread;

    public static void main(String args[]) {
        JFrame frame = new JFrame("App");
        instance = new firstSwingForm();
        frame.setContentPane(instance.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800,600);
        frame.setVisible(true);

        Crawler c = new Crawler();
        c.setMaxDepth(4);
        instance.startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(crawlerThread != null) {
                    Log.Error("main crawler is already running");
                }
                crawlerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {c.BFS(instance.startTxt.getText(), instance.endTextField.getText());};
                });
                crawlerThread.start();

            }
        });

        instance.reloadTreeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node root = c.getRoot();
                DefaultTreeModel model = (DefaultTreeModel) instance.wikiTree.getModel();
                DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)model.getRoot();
                treeRoot.removeAllChildren();
                treeRoot.add(new DefaultMutableTreeNode(root.getUrl()));
                copyNodeToTree(root, (DefaultMutableTreeNode) treeRoot.getChildAt(0));
                model.reload();
            };

            public void copyNodeToTree(Node root, DefaultMutableTreeNode treeRoot) {
                if(root == null) {
                    Log.Error("copyNodeToTree root == null");
                    return;
                }

                for(int i = 0; i < root.childrenSize(); i++) {
                    if(root.getChild(i).isLoaded()) {
                        treeRoot.add(new DefaultMutableTreeNode(root.getChild(i).getUrl()));
                        copyNodeToTree(root.getChild(i), (DefaultMutableTreeNode) treeRoot.getChildAt(i));
                    }else return;
                }
            }
        });
        Timer.printAll();
    }

}
