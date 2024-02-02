import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Function;


public class ClientLogin extends JFrame {
    //a window that has one input field, a label for the input and a connect button
    public JTextArea usernameInput;
    private static final int WIDTH = 150;
    private static final int PADDING = 30;
    public JButton connectButton;
    Function<ActionEvent, String> callback;

    public ClientLogin(Function<ActionEvent, String> fcallback){
        super("Login");
        this.callback = fcallback;
        setSize(WIDTH+PADDING*3, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(PADDING, 20, WIDTH, 20);
        add(usernameLabel);

        usernameInput = new JTextArea();
        usernameInput.setBounds(PADDING, 40, WIDTH, 20);
        //give the input field focus and a border
        usernameInput.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(usernameInput);

        connectButton = new JButton("Connect");
        connectButton.setBounds(PADDING, 80, WIDTH, 30);
        add(connectButton);
        setVisible(true);
    }
    

    //callback for the connect button
    public void actionPerfomed(ActionEvent e){
        String username = usernameInput.getText();
        if(username.equals("")){
            JOptionPane.showMessageDialog(this, "Please enter a valid username");
            return;
        }
        //send a message to the server to connect the user
        //message example: connect user1
        String message = "connect " + username;
    }
}
    