package DAO;

import Model.Account; //fixes Account to resolve to type, as in: List<Account>
//import Service.AccountService;
import Util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    /**
     * Retrieve all accounts from the accounts table
     * 
     * @return all accounts
     */
    public List<Account> getAllAccounts() {
        Connection connection = ConnectionUtil.getConnection();
        List<Account> accounts = new ArrayList<>();
        try {
            String sql = "select * from account";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                Account account = new Account(rs.getInt("account_id"), rs.getString("username"),
                        rs.getString("password"));
                accounts.add(account);
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return accounts;        
    }
    
    /**
     * Retrieve a specific account using its account ID
     * 
     * @param id an account ID (matching the account object to be retrieved)
     * @return the account object matching the account ID
     */
    public Account getAccountByID(int id)  {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "select * from account where account_id = ?";
            
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                Account account = new Account(rs.getInt("account_id"), rs.getString("username"),
                rs.getString("password"));
                return account;
            }

        }   catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }


    /**
     * Add an Account record into the database which matches the values contained in the Account object.
     * Values provided are username & password. The message_id is provided auto-generated by the database.
     * 
     * @param account Account object modeling the account to be added
     * @return the Account object that has been newly-added
     */
    public Account insertAccount(Account account) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "insert into account (username, password) values (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,account.getUsername());
            preparedStatement.setString(2,account.getPassword());

            preparedStatement.executeUpdate();
            ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
            if(pkeyResultSet.next()){
                int generated_account_id = (int) pkeyResultSet.getLong(1);
                return new Account(generated_account_id, account.getUsername(), account.getPassword());
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Update the Account identified by the account id to the values contained in the Account object
     * 
     * @param id an account ID of the account to be updated
     * @param account an Account object with fields to pass in the update 
     *        (which should not contain an account id *CHECK THIS) 
     */
    public void updateAccount(int id, Account account) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "update account set username=?, password=? where account_id=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1,account.getUsername());
            preparedStatement.setString(2,account.getPassword());
            preparedStatement.setInt(3, id);

            preparedStatement.executeUpdate();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }        
    }

    /**
     * Retrieve a specific account using its username and password
     * 
     * @param username an account username (matching the account object to be retrieved)
     * @param password an account password (matching the account object to be retrieved)
     * @return the account object matching the account login
     */
    public Account getAccountByLogin(String username, String password)  {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "select * from account where username = ? AND password = ?;";
            
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet rs = preparedStatement.executeQuery();

            if((rs.next())){
                Account account = new Account(rs.getInt("account_id"), rs.getString("username"),
                rs.getString("password"));

                return account;
            } else {
                return null;
            }

        }   catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
