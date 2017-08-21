package main.com.kodoma.dao;

import main.com.kodoma.connection.ConnectionPool;
import main.com.kodoma.datasource.User;
import main.com.kodoma.datasource.UsersList;
import main.com.kodoma.exceptions.WrongUserNameOrPassword;
import main.com.kodoma.mapper.Mapper;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static main.com.kodoma.util.Procedures.*;

/**
 * Created by Кодома on 26.07.2017.
 */
public class ContactDAO extends Observable implements DAO<User> {
    private Connection connection;
    private Mapper mapper = new Mapper();
    private int userID;

    private void initialize() {
        ConnectionPool pool = new ConnectionPool();
        //единожды инициализируем его указав класс драйвера, URL базы данных, а также логин и пароль к базе данных
        pool.initConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1/contact_book", "root", "root");
        connection = pool.getConnection();
    }


    public static ContactDAO instance;

    private ContactDAO() {
        initialize();
    }

    public static ContactDAO getInstance() {
        if (instance == null) {
            instance = new ContactDAO();
        }
        return instance;
    }


    @Override
    public void setObserver(Observer o) {
        this.addObserver(o);
    }

    @Override
    public void validate(String userName, String userPassword) throws SQLException, WrongUserNameOrPassword {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(VALIDATE);
        preparedStatement.setString(1, userName);
        preparedStatement.setString(2, userPassword);

        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            int id = result.getInt(1);
            if (id > 0) {
                userID = id;
                setChanged();
                notifyObservers("Пользователь " + userName + " подключен.");
            }
            else throw new WrongUserNameOrPassword();
        }
    }

    @Override
    public void add(User user) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(ADD_NEW_CONTACT);
        preparedStatement.setString(1, user.getFname());
        preparedStatement.setString(2, user.getLname());
        preparedStatement.setString(3, user.getAddress());
        preparedStatement.setInt(4, user.getPhoneNumber());
        preparedStatement.setString(5, null); //group_id
        preparedStatement.setInt(6, userID); //user_id

        preparedStatement.executeQuery();
    }

    public void editContact(User user) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(EDIT_CONTACT);
        preparedStatement.setInt(1, user.getId());
        preparedStatement.setString(2, user.getFname());
        preparedStatement.setString(3, user.getLname());
        preparedStatement.setString(4, user.getAddress());
        preparedStatement.setInt(5, user.getPhoneNumber());
        preparedStatement.setString(6, null); //group_id
        preparedStatement.setString(7, null); //user_id

        preparedStatement.executeQuery();
    }

    public void label(User user) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(LABEL_CONTACT);
        preparedStatement.setInt(1, user.getId());
        preparedStatement.setString(2, user.getGroupName());
        preparedStatement.setInt(3, userID);

        preparedStatement.executeQuery();
    }

    public void removeContact(User user) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(REMOVE_CONTACT);
        preparedStatement.setInt(1, user.getId());
        preparedStatement.setInt(2, userID);

        preparedStatement.executeQuery();
    }

    public void showContactByID(User user) throws SQLException, IOException {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(SHOW_CONTACT_BY_ID);
        preparedStatement.setInt(1, user.getId());
        preparedStatement.setInt(2, userID);

        ResultSet result = preparedStatement.executeQuery();
        UsersList users = mapper.mapToUser(result);
        setChanged();
        notifyObservers(users.toString());
    }

    @Override
    public void showAllContacts() throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(SHOW_ALL_CONTACTS);
        preparedStatement.setInt(1, userID);

        ResultSet result = preparedStatement.executeQuery();
        UsersList users = mapper.mapToUser(result);
        setChanged();
        notifyObservers(users.toString());
    }

    @Override
    public void showContactByName(User user) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(SHOW_CONTACT_BY_NAME);
        preparedStatement.setString(1, user.getFname());
        preparedStatement.setInt(2, userID);

        ResultSet result = preparedStatement.executeQuery();

        UsersList users = mapper.mapToUser(result);
        setChanged();
        notifyObservers(users.toString());
    }

    @Override
    public void showContactsOfGroup(String groupName) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(SHOW_CONTACTS_OF_GROUP);
        preparedStatement.setString(1, groupName);
        preparedStatement.setInt(2, userID);

        ResultSet result = preparedStatement.executeQuery();

        UsersList users = mapper.mapToUser(result);
        setChanged();
        notifyObservers(users.toString());
    }

    @Override
    public void showAllGroupsNames() throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(SHOW_ALL_GROUPS_NAMES);
        preparedStatement.setInt(1, userID);

        ResultSet result = preparedStatement.executeQuery();

        String groupNames = mapper.mapToGroup(result);
        setChanged();
        notifyObservers(groupNames);
    }

    @Override
    public void deleteLabel(User user) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(DELETE_LABEL);
        preparedStatement.setLong(1, user.getId());
        preparedStatement.setInt(2, userID);

        preparedStatement.executeQuery();
    }

    @Override
    public void editGroup(String name, String newName) throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(EDIT_GROUP);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, newName);

        ResultSet result = preparedStatement.executeQuery();

        while (result.next()) {
            System.out.println(result.getString(2));
        }
    }

    @Override
    public void removeGroup(String name) throws Exception {
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(REMOVE_GROUP);
        preparedStatement.setString(1, name);

        preparedStatement.executeQuery();
    }
}
