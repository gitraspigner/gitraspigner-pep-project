package DAO;

import Model.Message;
import Util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    /**
     * Retrieve all messages from the message table
     * 
     * @return all messages
     */
    public List<Message> getAllMessages() {
        Connection connection = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        try {
            String sql = "select * from message";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                Message message = new Message(rs.getInt("message_id"), rs.getInt("posted_by"),
                        rs.getString("message_text"), rs.getLong("time_posted_epoch"));
                messages.add(message);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return messages;        
    }
    
    /**
     * Retrieve a specific message using its message ID
     * 
     * @param id an message ID (matching the message object to be retrieved)
     * @return the message object matching the message ID
     */
    public Message getMessageByID(int id)  {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "select * from message where message_id = ?";
            
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                Message message = new Message(rs.getInt("message_id"), rs.getInt("posted_by"),
                rs.getString("message_text"), rs.getLong("time_posted_epoch"));
                return message;
            }

        }   catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve all messages posted by an account ID provided
     * 
     * @param id an Account ID
     * @return all messages posted by the account ID
     */
    public List<Message> getMessagesByAccountID(int id)  {
        Connection connection = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        try {
            String sql = "select * from message where posted_by=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                Message message = new Message(rs.getInt("message_id"), rs.getInt("posted_by"),
                        rs.getString("message_text"), rs.getLong("time_posted_epoch"));
                messages.add(message);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return messages; 
    }


    /**
     * Add a message record into the database which matches the values contained in the message object.
     * Values provided are posted_by, message_text, and time_posted_epoch. The message_id is 
     * auto-generated by the database.
     * 
     * @param message message object modeling the message to be created
     * @return the message object that has been newly-created
     */
    public Message insertMessage(Message message) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "insert into message (posted_by, message_text, time_posted_epoch) values (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1,message.getPosted_by());
            preparedStatement.setString(2,message.getMessage_text());
            preparedStatement.setLong(3, message.getTime_posted_epoch());

            preparedStatement.executeUpdate();
            ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
            if(pkeyResultSet.next()){
                int generated_message_id = (int) pkeyResultSet.getLong(1);
                return new Message(generated_message_id, message.getPosted_by(), message.getMessage_text(), 
                    message.getTime_posted_epoch());
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Update the message identified by the message id to the values contained in the message object
     * 
     * @param id an message ID of the message to be updated
     * @param Message an message object with fields to pass in the update 
     *         
     */
    public void updateMessage(int id, Message message) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "update message set posted_by=?, message_text=?, time_posted_epoch=? where message_id=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1,message.getPosted_by());
            preparedStatement.setString(2,message.getMessage_text());
            preparedStatement.setLong(3,message.getTime_posted_epoch());
            preparedStatement.setInt(4, id);

            preparedStatement.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }        
    }

    /**
     * Delete a pecific message using its message ID
     * 
     * @param id an message ID (matching the message object to be deleted)
     * @return the deleted message object matching the message ID
     */
    public Message deleteMessageByID(int id)  {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "delete from message where message_id = ?";
            
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            Message deletedMessage = getMessageByID(id); //get deleted message before executing delete
            // ^ this may not be necessary if pkeyresultset returns the deleted fields
            //                 !!!!! ^^^^
            preparedStatement.executeUpdate(); //execute delete

            //ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
            /**
             * if(pkeyResultSet.next()){
                /**
                 * int deleted_message_id = (int) pkeyResultSet.getInt(1);
                int deleted_posted_by = pkeyResultSet.getInt(2);
                String deleted_message_text = pkeyResultSet.getString(3);
                long deleted_time_posted_epoch = pkeyResultSet.getLong(4);
                return new Message(deleted_message_id, deleted_posted_by, deleted_message_text, 
                    deleted_time_posted_epoch);
             }
             */
            return deletedMessage;

        }   catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
    
}
