import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
    private JButton reloadTreeBtn;
    private JLabel lblPageRequestedCount;
    private JLabel lblPageLoadedCount;
    private JLabel lblAvgLinksPerPage;
    private JLabel lblCurrSearchLvl;
    private JLabel pageLoadSpeedLbl;
    private JLabel timeFromStartLbl;
    private static firstSwingForm instance;
    private static Thread crawlerThread;
    private static boolean keepUpdateUI;
    private static Thread uiUpdateThread;
    private static Crawler c;
    private static Instant crawlerStartTime;

    public static void main(String args[]) {
        JFrame frame = new JFrame("App");
        instance = new firstSwingForm();
        frame.setContentPane(instance.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800,600);
        frame.setVisible(true);

        c = new Crawler();
        c.setMaxDepth(4);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                WikiEngine.close();
                //System.exit(0);
            }
        });

        instance.startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(crawlerThread != null) {
                    Log.Error("main crawler is already running");
                }
                crawlerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        instance.startBtn.setEnabled(false);
                        c.BFS(instance.startTxt.getText(), instance.endTextField.getText());
                        instance.startBtn.setEnabled(true);
                    };
                });
                crawlerThread.start();

            }
        });

        instance.reloadTreeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        instance.reloadTreeBtn.setEnabled(false);
                        Node root = c.getRoot();
                        DefaultTreeModel model = (DefaultTreeModel) instance.wikiTree.getModel();
                        DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode)model.getRoot();
                        treeRoot.removeAllChildren();
                        treeRoot.add(new DefaultMutableTreeNode(root.getTitle() + "(" + root.childrenSize() + ")"));
                        copyNodeToTree(root, (DefaultMutableTreeNode) treeRoot.getChildAt(0));
                        model.reload();
                        instance.reloadTreeBtn.setEnabled(true);
                    }
                }).start();

            };

            void copyNodeToTree(Node root, DefaultMutableTreeNode treeRoot) {
                if(root == null) {
                    Log.Error("copyNodeToTree root == null");
                    return;
                }

                for(int i = 0; i < root.childrenSize(); i++) {
                    if(root.getChild(i) != null) {
                        if (root.getChild(i).isRequested() && root.getChild(i).isLoaded()) {
                            treeRoot.add(new DefaultMutableTreeNode(root.requestChild(i).getTitle() + "(" + root.requestChild(i).childrenSize() + ")"));
                            copyNodeToTree(root.getChild(i), (DefaultMutableTreeNode) treeRoot.getChildAt(i));
                        } else return;
                    }
                }
            }
        });



        keepUpdateUI = true;
        uiUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                UiUpdateLoop();
            }
        });
        uiUpdateThread.start();


        Timer.printAll();
    }


    private static final String strPageRequestCount = "requested pages count: ";
    private static final String strPageLoadedCount = "loaded pages count: ";
    private static final String strAvgLinksPerPage = "avg. links per page: ";
    private static final String strCurrSearchLvl = "currently looking target at level: ";
    private static final String strPageLoadedSpeed = " pages/min";


    static void UiUpdateLoop() {
        crawlerStartTime = Instant.now();
        while(keepUpdateUI) {
            updateUI();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



        }
    }

    public static void updateUI() {

        instance.lblPageRequestedCount.setText(strPageRequestCount + WikiEngine.getRequestedPagesCount());
        instance.lblPageLoadedCount.setText(strPageLoadedCount + WikiEngine.getLoadedPageCount());
        if(WikiEngine.getLoadedPageCount() > 0)
            instance.lblAvgLinksPerPage.setText(strAvgLinksPerPage + (WikiEngine.getTotalLinksFound() / WikiEngine.getLoadedPageCount()));
        instance.lblCurrSearchLvl.setText(strCurrSearchLvl + instance.c.getCurrentSearchLvl() + " out of " + instance.c.getMaxDepth());
        if(crawlerStartTime != null) {
            //instance.timeFromStartLbl.setText( crawlerStartTime.());
            long milsFromStart = ChronoUnit.MILLIS.between(crawlerStartTime, Instant.now());
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
            String time = sdf.format(milsFromStart);
            instance.timeFromStartLbl.setText(time);
            instance.pageLoadSpeedLbl.setText(WikiEngine.calcEngineSpeed() + strPageLoadedSpeed);
        }

    }
}
