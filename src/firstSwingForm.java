import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;

public class firstSwingForm {
    private JPanel config;
    private JTextField startTxt;
    private JTextField hitlerTextField;
    private JPanel mainPanel;

    public static void main(String args[]) {
        JFrame frame = new JFrame("App");
        frame.setContentPane(new firstSwingForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        ChromeDriver driver = new ChromeDriver();
        driver.navigate().to("www.google.com");
    }

}
