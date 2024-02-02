import java.util.ArrayList;
import com.rabbitmq.client.*;

public class Server {
    private static final String SERVER_QUEUE = "server_queue";
    private static int userCount = 0;
    public static void main(String[] args) throws Exception{
        ArrayList<String> connectedUsers = new ArrayList<String>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        Channel connectionChannel = connection.createChannel();
        connectionChannel.queueDeclare(SERVER_QUEUE, false, false, false, null);


        DeliverCallback deliverCallback = (consumerTag, delivery) -> 
        {
            //message example: connect user1            
            String message = new String(delivery.getBody(), "UTF-8");
            String command = message.split(" ")[0];
            String username = message.split(" ")[1];
            //check if user is already connected
            
            //declare queue for this user which will be used to listen for new message edits
            Channel userChannel = connection.createChannel();   
            userChannel.queueDeclare(username, false, false, false, null);
            switch(command){
                case "connect":
                {
                    if(connectedUsers.contains(username)){
                        System.out.println("[!] User '" + username + "' is already connected");
                        return;
                    }
                    //send the user a list of all connected users
                    //message example: inituser 2 user1 user2
                    connectedUsers.add(username);
                    userCount ++;

                    String connectedUsersString = "inituser " + String.valueOf(userCount) + " ";
                    connectedUsersString += ' ' + String.join(" ", connectedUsers);
                    userChannel.basicPublish("", username, null, connectedUsersString.getBytes("UTF-8"));

                    System.out.println("[+] User '" + username + "' connected, total user count is " + userCount + ".");
                    System.out.println("[.] :::: Sent '" + connectedUsersString + "' to User '" + username + "'");
                    //send a message to all connected users to add this user to their list
                    //message example: adduser user1
                    connectedUsers.forEach((user) -> {
                        if(!user.equals(username)){
                            String addUserMessage = "newuser " + username;
                            try {
                                userChannel.basicPublish("", user, null, addUserMessage.getBytes("UTF-8"));
                                System.out.println("[.] :::: Sent '" + addUserMessage + "' to User '" + user + "'");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                }
                case "disconnect":
                {
                    connectedUsers.remove(username);
                    userCount --;

                    //send a message to all connected users to remove this user from their list
                    //message example: removeuser user1

                    String removeUserMessage = "removeuser " + username;
                    System.out.println("[-] User '" + username + "' disconnected.");

                    for(String user : connectedUsers){
                        userChannel.basicPublish("", user, null, removeUserMessage.getBytes("UTF-8"));
                        System.out.println("[.] :::: Sent '" + removeUserMessage + "' to User '" + user + "'");
                    }
                    break;
                }
                case "lock":
                {
                    System.out.println("[.] Received lock request from User '" + username + "' for channel '" + message.split(" ")[2] + "'");
                    String locker = message.split(" ")[1];
                    String toLock = message.split(" ")[2];
                    String lockMessage = "lock " + locker + " " + toLock;
                    // send everyone a message to lock the tolock
                    for(String user : connectedUsers){
                        if(!user.equals(locker)){
                            userChannel.basicPublish("", user, null, lockMessage.getBytes("UTF-8"));
                            System.out.println("[.] :::: Sent '" + lockMessage + "' to User '" + user + "'");
                        }
                    }
                    break;
                }
                case "unlock":
                {
                    System.out.println("[.] Received unlock request from User '" + username + "' for channel '" + message.split(" ")[2] + "'");
                    String locker = message.split(" ")[1];
                    String toUnlock = message.split(" ")[2];
                    String unlockMessage = "unlock " + locker + " " + toUnlock;
                    // send everyone a message to unlock the tolock
                    for(String user : connectedUsers){
                        if(!user.equals(locker)){
                            userChannel.basicPublish("", user, null, unlockMessage.getBytes("UTF-8"));
                            System.out.println("[.] :::: Sent '" + unlockMessage + "' to User '" + user + "'");
                        }
                    }
                    break;
                }
                default:
                    System.out.println("[!] Invalid command received : " + command);
            }
        };

        connectionChannel.basicConsume(SERVER_QUEUE, true, deliverCallback, consumerTag -> { });
        System.out.println("[.] Server is running...");
        System.out.println("[.] Waiting for messages...");




    }
}
