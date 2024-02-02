import javax.swing.*;
import java.awt.event.*;

import com.rabbitmq.client.*;




public class Client {
    private static final String SERVER_QUEUE = "server_queue";
    private static String username = "";

    public static void main(String[] args) throws Exception {//amine1234561
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel connectionChannel = connection.createChannel();
        connectionChannel.queueDeclare(SERVER_QUEUE, false, false, false, null);
        
        
        ClientLogin clientLogin = new ClientLogin(null);
        clientLogin.connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = clientLogin.usernameInput.getText();
                if (username.equals("")) {
                    JOptionPane.showMessageDialog(clientLogin, "Please enter a valid username.");
                    return;
                }
                // send a message to the server to connect the user
                // message example: connect user1
                String message = "connect " + username;
                try {
                    System.out.println("[*] Establishing connection to Server...");
                    connectionChannel.basicPublish("", SERVER_QUEUE, null, message.getBytes("UTF-8"));
                    System.out.println("[*] Sent '" + message + "' to Server");
                    clientLogin.dispose();
                    
                    ClientWindow clientWindow = new ClientWindow(username, connection);
                } catch (Exception ex) {

                    ex.printStackTrace();
                }


            }
        });


    }
}
