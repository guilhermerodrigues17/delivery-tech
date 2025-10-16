package com.deliverytech.delivery_api.config;

import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.model.*;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.repository.ConsumerRepository;
import com.deliverytech.delivery_api.repository.OrderRepository;
import com.deliverytech.delivery_api.repository.ProductRepository;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ConsumerRepository consumerRepository;
    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("---|Iniciando seeding de dados para teste|---");

        consumerRepository.deleteAll();
        restaurantRepository.deleteAll();
        productRepository.deleteAll();
        orderRepository.deleteAll();

        saveConsumers();
        saveRestaurants();
        saveProducts();
        saveOrders();

        queriesTest();

    }

    private void saveConsumers() {
        System.out.println("---|Inserindo Clientes|---");

        Consumer c1 = new Consumer();
        c1.setName("Mateus da Silva");
        c1.setEmail("mateus@email.com");
        c1.setPhoneNumber("11111111111");
        c1.setAddress("Rua A, 123");
        c1.setActive(true);

        Consumer c2 = new Consumer();
        c2.setName("Maria da Silva");
        c2.setEmail("maria@email.com");
        c2.setPhoneNumber("22222222222");
        c2.setAddress("Rua B, 123");
        c2.setActive(true);

        Consumer c3 = new Consumer();
        c3.setName("Daniel da Silva");
        c3.setEmail("daniel@email.com");
        c3.setPhoneNumber("33333333333");
        c3.setAddress("Rua C, 123");
        c3.setActive(true);

        consumerRepository.saveAll(Arrays.asList(c1, c2, c3));

        System.out.println("---|3 clientes inseridos com sucesso!|---");
    }

    private void saveRestaurants() {
        System.out.println("\n---|Inserindo Restaurantes|---");

        Restaurant r1 = new Restaurant();
        r1.setName("Lanchonete A");
        r1.setCategory("Fast Food");
        r1.setAddress("Rua A, 123");
        r1.setDeliveryTax(new BigDecimal("5.00"));
        r1.setPhoneNumber("11111111112");
        r1.setActive(true);

        Restaurant r2 = new Restaurant();
        r2.setName("Lanchonete B");
        r2.setCategory("Italiana");
        r2.setAddress("Rua B, 123");
        r2.setDeliveryTax(new BigDecimal("10.00"));
        r2.setPhoneNumber("11111111113");
        r2.setActive(true);

        restaurantRepository.saveAll(Arrays.asList(r1, r2));

        System.out.println("---|2 restaurantes inseridos com sucesso!|---");
    }

    private void saveProducts() {
        System.out.println("\n---|Inserindo Produtos|---");

        Restaurant r1 = restaurantRepository.findByName("Lanchonete A")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));

        Restaurant r2 = restaurantRepository.findByName("Lanchonete B")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));

        Product p1 = new Product();
        p1.setName("Cheeseburguer tradicional");
        p1.setDescription("Lanche com pão, carne e queijo");
        p1.setCategory("Hamburguer de carne");
        p1.setAvailable(true);
        p1.setPrice(new BigDecimal("29.99"));
        p1.setRestaurant(r1);

        Product p2 = new Product();
        p2.setName("Batata frita média");
        p2.setDescription("Batatas cortadas no formato crinkles fritas com sal");
        p2.setCategory("Acompanhamentos");
        p2.setAvailable(true);
        p2.setPrice(new BigDecimal("12.99"));
        p2.setRestaurant(r1);

        Product p3 = new Product();
        p3.setName("Coca Cola Lata");
        p3.setDescription("Refrigerante de lata - 300mL");
        p3.setCategory("Bebidas");
        p3.setAvailable(true);
        p3.setPrice(new BigDecimal("9.99"));
        p3.setRestaurant(r1);

        Product p4 = new Product();
        p4.setName("Penne ao molho pomodoro");
        p4.setDescription("300g de massa com molho fresco de tomates italianos");
        p4.setCategory("Massas");
        p4.setAvailable(true);
        p4.setPrice(new BigDecimal("39.99"));
        p4.setRestaurant(r2);

        Product p5 = new Product();
        p5.setName("Palha italiana");
        p5.setDescription("Sobremesa italiana saborosa");
        p5.setCategory("Sobremesas");
        p5.setAvailable(true);
        p5.setPrice(new BigDecimal("18.99"));
        p5.setRestaurant(r2);

        productRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5));

        System.out.println("---|5 Produtos inseridos com sucesso!|---");
    }

    private void saveOrders() {
        System.out.println("\n---|Inserindo Pedidos|---");

        // Busca consumers para compor objeto de Order
        Consumer c1 = consumerRepository.findByEmail("mateus@email.com")
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Consumer c2 = consumerRepository.findByEmail("maria@email.com")
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        // Busca restaurants para compor objeto de Order
        Restaurant r1 = restaurantRepository.findByName("Lanchonete A")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));

        Restaurant r2 = restaurantRepository.findByName("Lanchonete B")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));

        // Busca products para compor objeto OrderItem
        List<Product> pl1 = productRepository.findByRestaurantIdAndAvailableTrue(r1.getId());
        List<Product> pl2 = productRepository.findByRestaurantIdAndAvailableTrue(r2.getId());

        var p1 = pl1.get(0);
        var p2 = pl1.get(1);
        var p3 = pl1.get(2);
        var p4 = pl2.get(0);
        var p5 = pl2.get(1);

        // Cria orderItems
        OrderItem oi1 = new OrderItem();
        oi1.setProduct(p1);
        oi1.setQuantity(1);
        oi1.setUnitPrice(p1.getPrice());
        oi1.setSubtotal(p1.getPrice().multiply(BigDecimal.valueOf(oi1.getQuantity())));

        OrderItem oi2 = new OrderItem();
        oi2.setProduct(p2);
        oi2.setQuantity(1);
        oi2.setUnitPrice(p2.getPrice());
        oi2.setSubtotal(p2.getPrice().multiply(BigDecimal.valueOf(oi2.getQuantity())));

        OrderItem oi3 = new OrderItem();
        oi3.setProduct(p3);
        oi3.setQuantity(1);
        oi3.setUnitPrice(p3.getPrice());
        oi3.setSubtotal(p3.getPrice().multiply(BigDecimal.valueOf(oi3.getQuantity())));

        OrderItem oi4 = new OrderItem();
        oi4.setProduct(p4);
        oi4.setQuantity(1);
        oi4.setUnitPrice(p4.getPrice());
        oi4.setSubtotal(p4.getPrice().multiply(BigDecimal.valueOf(oi4.getQuantity())));

        OrderItem oi5 = new OrderItem();
        oi5.setProduct(p5);
        oi5.setQuantity(1);
        oi5.setUnitPrice(p5.getPrice());
        oi5.setSubtotal(p5.getPrice().multiply(BigDecimal.valueOf(oi5.getQuantity())));

        var o1ItemsList = Arrays.asList(oi1, oi2, oi3);
        var o1Subtotal = o1ItemsList.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        var o2ItemsList = Arrays.asList(oi4, oi5);
        var o2Subtotal = o2ItemsList.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        Order o1 = new Order();
        o1.setConsumer(c1);
        o1.setDeliveryAddress(c1.getAddress());
        o1.setRestaurant(r1);
        o1.setDeliveryTax(r1.getDeliveryTax());
        o1.setStatus(OrderStatus.PENDING);

        o1.setItems(o1ItemsList);
        o1ItemsList.forEach(orderItem -> orderItem.setOrder(o1));

        o1.setSubtotal(o1Subtotal);
        o1.setTotal(o1Subtotal.add(o1.getDeliveryTax()));


        Order o2 = new Order();
        o2.setConsumer(c2);
        o2.setDeliveryAddress(c2.getAddress());
        o2.setRestaurant(r2);
        o2.setDeliveryTax(r2.getDeliveryTax());
        o2.setStatus(OrderStatus.PENDING);

        o2.setItems(o2ItemsList);
        o2ItemsList.forEach(orderItem -> orderItem.setOrder(o2));

        o2.setSubtotal(o2Subtotal);
        o2.setTotal(o2Subtotal.add(o2.getDeliveryTax()));

        orderRepository.saveAll(Arrays.asList(o1, o2));

        System.out.println("---|2 Pedidos inseridos com sucesso!|---");
    }

    private void queriesTest() {
        System.out.println("\n---|Testando consultas dos repositories|---");

        // Testes de consumer
        System.out.println("\n---|Testando ConsumerRepository|---");

        var consumerByEmail = consumerRepository.findByEmail("mateus@email.com");
        System.out.println("\nCliente por email: "
                + (consumerByEmail.isPresent() ? consumerByEmail.get().getName()
                : "Não encontrado"));

        var activeConsumers = consumerRepository.findByActiveTrue();
        System.out.println("\nClientes ativos: " + activeConsumers.size());

        var consumersByName = consumerRepository.findByNameContainingIgnoreCase("silva");
        System.out.println("\nClientes com 'silva' no nome: " + consumersByName.size());

        var existsEmail = consumerRepository.existsByEmail("maria@email.com");
        System.out.println("\nO e-mail 'maria@email.com' está em uso? -> " + existsEmail);

        // Testes de restaurant
        System.out.println("\n---|Testando RestaurantRepository|---");

        var restaurantByName = restaurantRepository.findByName("Lanchonete A");
        System.out.println("\nRestaurante por nome: "
                + (restaurantByName.isPresent() ? restaurantByName.get().getName()
                : "Não encontrado"));

        var restaurantExistsByName = restaurantRepository.existsByName("Lanchonete B");
        System.out.println(
                "\nExiste restaurante com o nome 'Lanchonete B'? -> " + restaurantExistsByName);

        var restaurantsByCategory = restaurantRepository.findByCategory("Fast Food");
        System.out.println("\nRestaurantes de Fast Food: " + restaurantsByCategory.size());

        var activeRestaurants = restaurantRepository.findByActiveTrue();
        System.out.println("\nRestaurantes ativos: " + activeRestaurants.size());

        var restaurantsByDeliveryTax =
                restaurantRepository.findByDeliveryTaxLessThanEqual(new BigDecimal("10.00"));
        System.out.println("\nRestaurantes com taxa de entrega menor ou igual a R$10,00: "
                + restaurantsByDeliveryTax.size());

        var top5ByOrderRestaurants = restaurantRepository.findTop5ByOrderByNameAsc();
        System.out.println("\nTop 5 restaurantes: ");
        top5ByOrderRestaurants.forEach(restaurant -> System.out.println(restaurant.getName()));

        // Testes de product
        System.out.println("\n---|Testando ProductRepository|---");

        var productsByRestaurant =
                productRepository.findByRestaurantId(restaurantByName.get().getId());
        var productsAvailableByRestaurant = productRepository
                .findByRestaurantIdAndAvailableTrue(restaurantByName.get().getId());

        System.out.println("\nProdutos por restaurante: ");
        productsByRestaurant.forEach(
                product -> System.out.println("Restaurante: " + restaurantByName.get().getName()
                        + " | " + product.getName() + " | " + product.getPrice()));

        System.out.println("\nProdutos disponíveis por restaurante: ");
        productsAvailableByRestaurant.forEach(
                product -> System.out.println("Restaurante: " + restaurantByName.get().getName()
                        + " | " + product.getName() + " | " + product.getPrice()));

        var productsByCategory = productRepository.findByCategory("Acompanhamentos");
        var productsAvailableByCategory =
                productRepository.findByCategoryAndAvailableTrue("Bebidas");

        System.out.println("\nProdutos - Acompanhamentos: ");
        productsByCategory.forEach(
                product -> System.out.println(product.getName() + " | " + product.getPrice()));

        System.out.println("\nBebidas disponíveis: ");
        productsAvailableByCategory.forEach(
                product -> System.out.println(product.getName() + " | " + product.getPrice()));

        var availableProducts = productRepository.findByAvailableTrue();
        System.out.println("\nProdutos disponíveis: " + availableProducts.size());

        var productsByPrice = productRepository.findByPriceLessThanEqual(new BigDecimal("20.00"));
        System.out.println(
                "\nProdutos com preço menor ou igual a R$20,00: " + productsByPrice.size());

        // Testes de order
        System.out.println("\n---|Testando OrderRepository|---");

        var ordersByConsumer = orderRepository.findByConsumerId(consumerByEmail.get().getId());
        System.out.println("\nPedidos feitos pelo cliente 'Mateus': " + ordersByConsumer.size());

        var ordersByStatus = orderRepository.findByStatus(OrderStatus.PENDING);
        System.out.println("\nPedidos com status 'PENDING': " + ordersByStatus.size());

        var startDate = LocalDateTime.of(2025, 10, 13, 0, 1);
        var endDate = LocalDateTime.of(2025, 10, 13, 23, 59);

        var ordersByDate = orderRepository.findByOrderDateBetween(startDate, endDate);
        System.out.println("\nPedidos feitos no dia 13/10/2025: " + ordersByDate.size());

        var top10Orders = orderRepository.findTop10ByOrderByOrderDateDesc();
        System.out.println("\nTop 10 Pedidos ordenado por subtotal: ");
        top10Orders.forEach(order -> System.out.println(order.getConsumer().getName() + " | "
                + order.getRestaurant().getName() + " | " + order.getSubtotal()));

        System.out.println("\n---|Testes finalizados!|---");
    }
}
