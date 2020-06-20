package com.company.eshop.user;

import com.company.eshop.error.UserNotFoundException;
import com.company.eshop.order.Order;
import com.company.eshop.order.OrderRepository;
import com.company.eshop.order.OrderStatus;
import com.company.eshop.orderproducts.OrderProduct;
import com.company.eshop.orderproducts.OrderProductRepository;
import com.company.eshop.product.Product;
import com.company.eshop.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private OrderProductRepository orderProductRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    //Mapper as a component
    private UserDtoMapper userMapper;

    public List<UserResponseDto> getUserList() {

        //Business logic
        List<User> userList = new ArrayList<>();
        userRepository.findAll()
                .forEach(userList::add);

        //Mapper from User to Response Object
        List<UserResponseDto> userResponseDtos = new ArrayList<>();
        for(User user: userList) {
            UserResponseDto userResponse = userMapper.fromUser(user);
            userResponseDtos.add(userResponse);
        }
        return userResponseDtos;
    }

    public UserResponseDto getUser(Long id) {

        //Business logic
        User user = userRepository.findById(id).orElse(null);
        if (user == null)
            throw new UserNotFoundException();

        //Mapper from User to Response Object
        return userMapper.fromUser(user);
    }

    public UserResponseDto createUser(CreateUserRequestDto userRequestDto) {
        //Mapper from Request to User
        User user = userMapper.fromRequest(userRequestDto);

        //Business logic
        String encodedPass = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);
        user = userRepository.save(user);

        //Mapper from User to Response
        return userMapper.fromUser(user);
    }








    /**
     * Creates a New Order for a specified User
     * @param products a List of productIds identifying products in the Database.
     * @param userId the id of the user for whom the order will be created.
     * @return
     */
    public Order checkout(List<Product> products, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            throw new UserNotFoundException();
        Order order = new Order();
        order.setStatus(OrderStatus.SUBMITTED);
        order.setUser(user);
        List<OrderProduct> orderProducts = new ArrayList<>();
        for( Product product: products) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(productRepository.findById(product.getId()).orElse(null));
            orderProducts.add(orderProduct);
        }
        order.setProductList(orderProducts);
        order = orderRepository.save(order);
        List<OrderProduct> products1 = orderProductRepository.saveAll(orderProducts);
        order.setProductList(products1);
        return order;
    }

    public List<Order> getOrders(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user == null)
            throw new UserNotFoundException();
        return orderRepository.findAllByUser(user);
    }

    public List<Order> getOrders(Long userId, OrderStatus status) {
        User user = userRepository.findById(userId).orElse(null);
        if(user == null)
            throw new UserNotFoundException();
        return orderRepository.findAllByUserAndStatus(user, status);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null)
            throw new UsernameNotFoundException(username);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());

    }
}
