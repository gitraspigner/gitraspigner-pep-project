package Service;

import Model.Message;
import DAO.MessageDAO;

//import java.util.ArrayList;
import java.util.List;

public class MessageService {
    static MessageDAO MessageDAO;
    /**
     * No-args constructor for a MessageService instantiates a plain MessageDAO.
     * There is no need to modify this constructor.
     */
    public MessageService(){
        MessageDAO = new MessageDAO();
    }

    /**
     * Constructor for a MessageService when a MessageDAO is provided.
     * This is used for when a mock MessageDAO that exhibits mock behavior is used in the test cases.
     * This would allow the testing of MessageService independently of MessageDAO.
     * There is no need to modify this constructor.
     * @param MessageDAO
     */
    public MessageService(MessageDAO MessageDAO){
        MessageService.MessageDAO = MessageDAO;
    }

    /**
     * DONE: Use the MessageDAO to add a new Message to the database.
     *
     * This method should also return the added Message. A distinction should be made between *transient* and
     * *persisted* objects - the *transient* Message Object given as the parameter will not contain the Message's id,
     * because it is not yet a database record. When this method is used, it should return the full persisted Message,
     * which will contain the Message's id. This way, any part of the application that uses this method has
     * all information about the new Message, because knowing the new Message's ID is necessary. This means that the
     * method should return the Message returned by the MessageDAO's insertMessage method, and not the Message provided by
     * the parameter 'Message'.
     *
     * @param Message an object representing a new Message.
     * @return the newly added Message if the add operation was successful, including the Message_id. We do this to
     *         inform our provide the front-end client with information about the added Message.
     */
    public Message addMessage(Message Message){
        return MessageDAO.insertMessage(Message);
    }

    /**
     * DONE: Use the MessageDAO to update an existing Message from the database.
     * You should first check that the Message ID already exists. To do this, you could use an if statement that checks
     * if MessageDAO.getMessageById returns null for the Message's ID, as this would indicate that the Message id does not
     * exist.
     *
     * @param Message_id the ID of the Message to be modified.
     * @param Message an object containing all data that should replace the values contained by the existing Message_id.
     *         the Message object does not contain a Message ID.
     * @return the newly updated Message if the update operation was successful. Return null if the update operation was
     *         unsuccessful. We do this to inform our application about successful/unsuccessful operations. (eg, the
     *         user should have some insight if they attempted to edit a nonexistent Message.)
     */
    public Message updateMessage(int Message_id, Message Message){
        if (MessageDAO.getMessageByID(Message_id) != null) {
            MessageDAO.updateMessage(Message_id, Message);
            return MessageDAO.getMessageByID(Message_id);
        } else  {
            return null;
        }
    }

    /**
     * DONE: Use the MessageDAO to retrieve a List containing all Messages.
     * You could use the MessageDAO.getAllMessages method.
     *
     * @return all Messages in the database.
     */
    public List<Message> getAllMessages() {
        return MessageDAO.getAllMessages();
    }  
    
    /**
     * Return message using message_id provided
     * 
     * @param id Message id to check for
     * @return Message with message_id matching id parameter
     */
    public static Message getMessageByID(int id) {
        return MessageDAO.getMessageByID(id);
    }

    /**
     * Delete message specified using message_id parameter
     * 
     * @param id Message id to delete
     * @return Message found and deleted matching message_id parameter
     */
    public static Message deleteMessageByID(int id) {
        return MessageDAO.deleteMessageByID(id);
    }

    public static List<Message> getMessagesByAccountID(int id) {
        return MessageDAO.getMessagesByAccountID(id);
    }
}
