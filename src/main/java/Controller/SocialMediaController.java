package Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

//import DAO.MessageDAO;
import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpTester;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

/**
 * DONE: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
AccountService accountService;
MessageService messageService;

public SocialMediaController(){
    accountService = new AccountService();
    messageService = new MessageService();
}

    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::postUserHandler);
        app.post("/login", this::loginUserHandler);
        app.post("/messages", this::postMessageHandler);
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/messages/{message_id}", this::getMessageByIDHandler);  
        app.delete("/messages/{message_id}", this::deleteMessageByIDHandler);
        app.patch("/messages/{message_id}", this::updateMessageByIDHandler);
        app.get("/accounts/{account_id}/messages", this::getAllMessagesByIDHandler);
        return app;
    }

    /**
     * Handler to register a new user account.
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an Account object.
     * If accountService returns a null account (meaning posting a account was unsuccessful, the API will return a 400
     * message (client error).
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object. 
     */
    private void postUserHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        Account addedAccount = accountService.addAccount(account);

        //if adding the user is successful, post the new message to add in the response body via ctx object/variable
        if(addedAccount != null) {
            ctx.json(mapper.writeValueAsString(addedAccount));
            ctx.status(200);
        } else { //addedAccount==null or other login requirements not met
            ctx.status(400);
        }
    }

    /**
     * Handler to handle a user account login.
     * 
     * As a user, I should be able to verify my login on the endpoint POST localhost:8080/login. 
     * The request body will contain a JSON representation of an Account, not containing an account_id. 
     * In the future, this action may generate a Session token to allow the user to securely use the site. 
     * We will not worry about this for now.

     * The login will be successful if and only if the username and password provided in the request body JSON match a real account existing on the database. If successful, the response body should contain a JSON of the account in the response body, including its account_id. The response status should be 200 OK, which is the default.
     * If the login is not successful, the response status should be 401. (Unauthorized)
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an Account object.
     * If accountService returns a null account (meaning posting a account was unsuccessful, the API will return a 400
     * message (client error).
     * 
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object. 
     */
    private void loginUserHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        
        //Using mapperobject instead of pathparam and adjusting endpoint accordingly
        //to not use (username and password, instead retrieve them from ctx.body/mapper Account object):
        //String username = ctx.pathParam("username");
        //String password = ctx.pathParam("password");
        //System.out.println(username + " " + password); // for debug but doesn't work
        
        Account account = mapper.readValue(ctx.body(), Account.class);
        String username = account.getUsername();
        String password = account.getPassword();
        
        Account foundAccount = accountService.getAccountByLogin(username, password);
        
        if(foundAccount != null) {
            ctx.json(mapper.writeValueAsString(foundAccount));
            ctx.status(200);
        } else {
            ctx.status(401);
        }
    }

    /**
     * Handler to post a new message.
     * The creation of the message will be successful if and only if the message_text is not blank, 
     * is not over 255 characters, and posted_by refers to a real, existing user. 
     * 
     * If successful, the response body should contain a JSON of the message, including its 
     * message_id. The response status should be 200 (which is the default). 
     * The new message should be persisted to the database.
     * If not successful, the response status should be 400. (Client error)
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an Account object.
     * If accountService returns a null account (meaning posting a account was unsuccessful, the API will return a 400
     * message (client error).
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object. 
     */
    private void postMessageHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message message = mapper.readValue(ctx.body(),Message.class);
        // message in body has message_id AND posted_by which refers to an account id
        int foundAccountID = message.getPosted_by();
        Account foundAccount = accountService.getAccountByID(foundAccountID);

        //then post the new message to add in the response body via ctx object/variable
        String messageText = message.getMessage_text();
        long messageTimePosted = message.getTime_posted_epoch();


        //message not blank, not message over 255 char, and related to real user
        if(messageText.compareTo(new String("")) != 0 && 
            messageText.length() < 255 && foundAccount != null) {
       
            //the message ID isn't given until the message has been added
            //using message service
            Message messageToAdd = new Message(foundAccountID, messageText, messageTimePosted); 
            //messageToAdd has no message_id
            Message messageAdded = messageService.addMessage(messageToAdd); 
            //messageAdded has a message_id now
            //now, write messageAdded to json response using mapper
            ctx.json(mapper.writeValueAsString(messageAdded));
            ctx.status(200);
        } else { //foundAccount == null etc
            ctx.status(400);
        }
    }

    /**
     * Handler to retrieve all messages.
     * 
     * 
     * As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages.
     * The response body should contain a JSON representation of a list containing all messages 
     * retrieved from the database. 
     * It is expected for the list to simply be empty if there are no messages.
     * The response status should always be 200, which is the default.
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an object.
     * 
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object. 
     */
    private void getAllMessagesHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Message> messages = messageService.getAllMessages();
        ctx.json(mapper.writeValueAsString(messages));
        ctx.status(200);
    }

    /**
     * Handler to retrieve message matching a given message_id
     * 
     * As a user, I should be able to submit a GET request on the 
     * endpoint GET localhost:8080/messages/{message_id}.
     * The response body should contain a JSON representation of the message 
     * identified by the message_id. It is expected for the response body to 
     * simply be empty if there is no such message. The response status should 
     * always be 200, which is the default.
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an object.
     * 
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object. 
     */
    private void getMessageByIDHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        int id = Integer.parseInt(ctx.pathParam("message_id"));
        Message message = MessageService.getMessageByID(id);
        if (message != null) {
            ctx.json(mapper.writeValueAsString(message));
        }
        ctx.status(200);
    }


    /*
     * Handler to delete message matching a given message_id
     * 
     * As a User, I should be able to submit a DELETE request on the 
     * endpoint DELETE localhost:8080/messages/{message_id}.
     * The deletion of an existing message should remove an 
     * existing message from the database. If the message existed, the response body 
     * should contain the now-deleted message. The response status should be 200, which is the default.
     * If the message did not exist, the response status should be 200, but 
     * the response body should be empty. This is because the DELETE verb is intended to be idempotent, 
     * ie, multiple calls to the DELETE endpoint should respond with the same type of response.
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an object.
     * 
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object.  
     */
    private void deleteMessageByIDHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        int id = Integer.parseInt(ctx.pathParam("message_id"));
        Message message = MessageService.deleteMessageByID(id);
        //if existed & deleted, the response body should include the NOW-DELETED message
        //if did not exist & no delete, the response body should be empty
        if (message != null) {
            ctx.json(mapper.writeValueAsString(message));
        } //may need to write to json empty string if no delete performed
        ctx.status(200);
    }
    
     /*
     * Handler to update message matching a given message_id
     * 
     * As a user, I should be able to submit a PATCH request on 
     * the endpoint PATCH localhost:8080/messages/{message_id}. The request body should 
     * contain a new message_text values to replace the message identified by message_id. 
     * The request body can not be guaranteed to contain any other information. 
     * 
     * The update of a message should be successful if and only if the message id already 
     * exists and the new message_text is not blank and is not over 255 characters. If the 
     * update is successful, the response body should contain the full updated message (including message_id, 
     * posted_by, message_text, and time_posted_epoch), and the response status should be 200, which is the default. 
     * The message existing on the database should have the updated message_text.
     * If the update of the message is not successful for any reason, the response status should be 400. (Client error)
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an object.
     * 
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object.  
     */
    private void updateMessageByIDHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        int idToUpdate = Integer.parseInt(ctx.pathParam("message_id")); //id to update from pathParam
        Message newMessage = mapper.readValue(ctx.body(),Message.class); //message(text) from body
        String newMessageText = newMessage.getMessage_text();



        //message not blank, not message over 255 char, and related to real user
        if(newMessageText.compareTo(new String("")) != 0 && 
                newMessageText.length() < 255 && MessageService.getMessageByID(idToUpdate) != null) {

            Message updatedMessage = messageService.updateMessage(idToUpdate, newMessage);
            updatedMessage.setMessage_text(newMessageText);

            //if existed & replaced, the response body should include the NOW-UPDATED message
            //if did not exist & no update, the response body should be empty (null)
            ctx.json(mapper.writeValueAsString(updatedMessage));
            ctx.status(200);

        } else { //updatedMessage == null etc
            ctx.status(400);
        }        
    }

    /**
     * Handler to retrieve all messages by a particular user
     * 
     * The response body should contain a JSON representation of a list containing all messages 
     * posted by a particular user, which is retrieved from the database. It is expected for the 
     * list to simply be empty if there are no messages. The response status should always be 200, 
     * which is the default.
     * 
     * The Jackson ObjectMapper will automatically convert the JSON of the POST request into an object.
     * 
     * @param ctx the context object handles information HTTP requests and generates responses within Javalin. It will
     *            be available to this method automatically thanks to the app.post method.
     * @throws JsonProcessingException will be thrown if there is an issue converting JSON into an object. 
     */
    private void getAllMessagesByIDHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        int accountID = Integer.parseInt(ctx.pathParam("account_id")); //id to update from pathParam
        List<Message> messages = MessageService.getMessagesByAccountID(accountID);
        ctx.json(mapper.writeValueAsString(messages));
        ctx.status(200);
    }    
    


}