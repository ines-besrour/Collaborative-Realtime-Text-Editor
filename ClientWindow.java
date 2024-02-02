import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;

import com.rabbitmq.client.*;

public class ClientWindow extends JFrame {
    private String username = "";
    private ArrayList<String> connectedUsers = new ArrayList<String>();
    private Integer countUsers;
    private Connection connection;
    private Map<String, JComponent> userComponents = new HashMap<String, JComponent>();

    public ClientWindow(String username, Connection connection) throws Exception {
        this.username = username;
        this.connection = connection;
        this.countUsers = 0;
        Channel userChannel = connection.createChannel();
        userChannel.queueDeclare(username, false, false, false, null);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[+] Received '" + message + "' from Server");

            String command = message.split(" ")[0];
            switch(command){
                case "inituser":
                    System.out.println("[*] Initializing user list...");
                    countUsers = Integer.parseInt(message.split(" ")[1]);
                    for(int i = 2; i < message.split(" ").length; i++){
                        connectedUsers.add(message.split(" ")[i]);
                    }
                    System.out.println("[*] Added user list: "+connectedUsers );
                    initWindow();
                    break;
                case "removeuser":
                    String toRemove = message.split(" ")[1];
                    connectedUsers.remove(toRemove);
                    System.out.println("[*] Removed user '" + toRemove + "' from user list");
                    userComponents.get(toRemove).setVisible(false);
                    userComponents.remove(toRemove);
                    countUsers --;
                    break;
                case "newuser":
                    String newUser = message.split(" ")[1];
                    connectedUsers.add(newUser);
                    addNewUserGui(newUser, countUsers+1);
                    countUsers ++;
                    System.out.println("[*] Added user '" + newUser + "' to user list");
                    break;
                case "message":
                    String fromUser = message.split(" ")[1];
                    String messageContent = "";
                    // messagecontent is a string of all the words after the first 2 words
                    for(int i = 2; i < message.split(" ").length; i++){
                        messageContent += message.split(" ")[i] + " ";
                    }
                    System.out.println("[*] Received message from '" + fromUser + "': " + messageContent);
                    // get the textfield of the user who sent the message
                    JTextField textField = (JTextField) userComponents.get(fromUser);
                    textField.setText(messageContent);
                    break;
                case "lock":
                    String locker = message.split(" ")[1];
                    String userToLock = message.split(" ")[2];
                    if(locker.equals(username)){
                        System.out.println("[*] You locked user field '" + userToLock + "'...");
                        break;
                    } else {
                        System.out.println("[*] User '" + locker + "' locked user field '" + userToLock + "'...");
                    }
                    JTextField textFieldToLock = (JTextField) userComponents.get(userToLock);
                    textFieldToLock.setEditable(false);
                    break;
                case "unlock":
                    String unlocker = message.split(" ")[1];
                    String userToUnlock = message.split(" ")[2];
                    if(unlocker.equals(username)){
                        System.out.println("[*] You unlocked user field '" + userToUnlock + "'...");
                        break;
                    } else {
                        System.out.println("[*] User '" + unlocker + "' unlocked user field '" + userToUnlock + "'...");
                    }
                    JTextField textFieldToUnlock = (JTextField) userComponents.get(userToUnlock);
                    textFieldToUnlock.setEditable(true);
                    break;
                default:
                    System.out.println("[!] Unknown command received from Server: " + command);
                    break;
            }
        };
        userChannel.basicConsume(username, true, deliverCallback, consumerTag -> {});
        System.out.println("[*] Listening for messages from Server...");
    }

    public void initWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Client: " + username);
        setLayout(new GridLayout(connectedUsers.size()+1, 1));
        // make size dynamic
        setSize(700, 500);

        // visible
        setVisible(true);




        // GridLayout layout = new GridLayout(connectedUsers.size()+1, 3);
        // setLayout(layout);
        // // setSize(700, 500);
        // setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // // JLabel label = new JLabel("WELCOME, Total of " + countUsers + " connected users.");
        // // label.setBounds(10,10,150, 30);
        // // add(label);
        // // addNewUserGui(username, 0);
        // //foreach connected user, add an input field and a trigger function where whenever the user edits the text, it sends a message to the server
        for (String connecteduser: connectedUsers){
            if(connecteduser.length()>0){
                Integer index = connectedUsers.indexOf(connecteduser);
                addNewUserGui(connecteduser, index+1);
            }

        }

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    onClose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public void addNewUserGui(String newuser, Integer index) {
        System.out.println("[*] Adding new user GUI for " + newuser);
        JLabel userLabel = new JLabel(newuser + ": ");
        userLabel.setSize(70, 20);

        JTextField userTextField = new JTextField();
        userTextField.setSize(400, 20);
        // userTextField justify left and up
        userTextField.setEditable(true);
        // if the userfield is highlighted, send a message to the server
        userTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("[*] User '" + username + "' is editing the text field of user '" + newuser + "'");
                try {
                    Channel connectionChannel = connection.createChannel();
                    connectionChannel.queueDeclare("server_queue", false, false, false, null);
                    String message = "lock " + username + " " + newuser;
                    // lock [user locking the field] [user whose field is being locked]
                    connectionChannel.basicPublish("", "server_queue", null, message.getBytes("UTF-8"));
                    System.out.println("[*] Sent lock message to Server");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void focusLost(FocusEvent e) { 
                System.out.println("[*] User '" + username + "' has stopped editing the text field of user '" + newuser + "'");
                try {
                    Channel connectionChannel = connection.createChannel();
                    connectionChannel.queueDeclare("server_queue", false, false, false, null);
                    String message = "unlock " + username + " " + newuser;
                    // unlock [user unlocking the field] [user whose field is being unlocked]
                    connectionChannel.basicPublish("", "server_queue", null, message.getBytes("UTF-8"));
                    System.out.println("[*] Sent unlock message to Server");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });


        JComponent userComponent = new JComponent() {
        };
        userComponent.setLayout(new GridLayout(1, 2));
        userComponent.setBounds(10, 10 + index * 50, 500, 20);
        userComponent.add(userLabel);
        userComponent.add(userTextField);
        
        userComponents.put(newuser, userComponent);
        add(userComponent);
        userComponent.setVisible(true);
        // update the window
        setVisible(true);

    }


    public void onClose() throws Exception {
        Channel connectionChannel = connection.createChannel();
        connectionChannel.queueDeclare("server_queue", false, false, false, null);
        String message = "disconnect " + username;
        connectionChannel.basicPublish("", "server_queue", null, message.getBytes("UTF-8"));
        System.out.println("[*] Disconnected from Server");
    }

}
