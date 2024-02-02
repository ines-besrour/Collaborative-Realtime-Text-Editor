import java.awt.*;
import java.awt.event.*;
//this class will be used to create the client interface
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class ClientInterface {

    private static final String SERVER_QUEUE = "server_queue";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PADDING = 30;
    private static final int TEXTAREA_HEIGHT = 300;
    private static final int TEXTAREA_WIDTH = 400;
    private static final int TEXTAREA_PADDING = 50;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_PADDING = 20;
    private static final int LABEL_WIDTH = 100;
    private static final int LABEL_HEIGHT = 20;
    private static final int LABEL_PADDING = 20;

    private JFrame frame;
    private JTextArea textArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private JLabel usernameLabel;
    private String username;
    private String selectedUser;
    private ArrayList<String> connectedUsers;

    public ClientInterface(){
        connectedUsers = new ArrayList<String>();
        frame = new JFrame("Client");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        //create a text area to display messages
        textArea = new JTextArea();
        textArea.setBounds(PADDING, PADDING, TEXTAREA_WIDTH, TEXTAREA_HEIGHT);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        frame.add(textArea);

        //create a text area to input messages
        inputArea = new JTextArea();
        inputArea.setBounds(PADDING, TEXTAREA_HEIGHT + TEXTAREA_PADDING, TEXTAREA_WIDTH, TEXTAREA_HEIGHT);
        inputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        frame.add(inputArea);

        //create a button to send messages
        sendButton = new JButton("Send");
        sendButton.setBounds(PADDING, HEIGHT - BUTTON_HEIGHT - BUTTON_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT);
        frame.add(sendButton);
        sendButton.addActionListener(e -> actionPerfomed(e));

        //create a label to display the username
        usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(TEXTAREA_WIDTH + TEXTAREA_PADDING, PADDING, LABEL_WIDTH, LABEL_HEIGHT);
        frame.add(usernameLabel);

        frame.setVisible(true);
    }

    //callback for the send button
    public void actionPerfomed(ActionEvent e){
        String message = inputArea.getText();
        if(message.equals("")){
            return;
        }
        //send a message to the server to connect the user
    }

}

