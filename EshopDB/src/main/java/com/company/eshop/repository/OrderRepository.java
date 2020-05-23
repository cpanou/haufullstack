package com.company.eshop.repository;

import com.company.eshop.application.MyApplication;
import com.company.eshop.model.Order;
import com.company.eshop.model.Order;
import com.company.eshop.model.OrderStatus;
import com.company.eshop.repository.templates.OrderTemplate;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OrderRepository {
    private static final Logger log = Logger.getLogger(MyApplication.class.getName());
    //Singleton Pattern
    private static OrderRepository instance;

    private List<Order> orders = new ArrayList<>();

    private OrderRepository(){
    }

    public static void init(){
        instance = new OrderRepository();
    }

    public static OrderRepository getInstance() {
        return instance;
    }
    //END Singleton


    private long getNewOrderId() {
        return orders.size() + 1;
    }

    /*
     * CRUD OPERATIONS
     *
     * CREATE -> add
     * READ -> get
     * UPDATE -> get -> edit
     * DELETE -> remove
     *
     * */

    public List<Order> getOrders(){
        log.info("fetching all orders");
        return orders;
    }

    public Order addOrder(Order order) {

        try (Connection connection = DataBaseUtils.createConnection();
             PreparedStatement statement = connection.prepareStatement(OrderTemplate.QUERY_INSERT_ORDER,Statement.RETURN_GENERATED_KEYS);
             PreparedStatement statementSelect = connection.prepareStatement(OrderTemplate.QUERY_SELECT_ORDERS_ORDER_ID)){

            statement.setTimestamp(1, Timestamp.valueOf(order.getSubmitted() ) );
            statement.setString(2, order.getStatus().name() );
            statement.setLong(3, order.getUserId() );

            int result = statement.executeUpdate();
            if (result == 0)
                return null;

            ResultSet keySet = statement.getGeneratedKeys();
            if(!keySet.next()){
                return null;

            }
            long neOrderId = keySet.getLong(1);




        } catch (SQLException e) {
            e.printStackTrace();
        }

        return order;

    }

    public Order getOrder(long orderId) {
        log.info("fetching order with id: " + orderId);
        for( Order order: orders) {
            if( order.getOrderId() == orderId)
                return order;
        }
        log.info("order with id : " + orderId + " not found");
        return null;
    }

    public Order deleteOrder(long orderId) {
        log.info("Deleting order with id: " + orderId);
        Order order = getOrder(orderId);
        if(order == null) return null;

        orders.remove(order);
        log.info("order with id : " + orderId + " deleted");
        return order;
    }

    public Order updateOrder(Order order) {
        log.info("updating order with id: " + order.getOrderId());
        deleteOrder(order.getOrderId());
        addOrder(order);
        log.info("order with id : " + order.getOrderId() + " deleted");
        return order;
    }
}
