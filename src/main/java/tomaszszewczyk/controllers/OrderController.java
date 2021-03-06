package tomaszszewczyk.controllers;

import lombok.Data;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tomaszszewczyk.entities.Order;
import tomaszszewczyk.entities.OrderDetail;
import tomaszszewczyk.repositories.OrderDetailsRepository;
import tomaszszewczyk.repositories.OrderRepository;
import tomaszszewczyk.repositories.ProductRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {

    @Data
    public static class OrderDTO {
        private Integer orderID;
        private String customerID;
        private Integer employeeID;
        private Date orderDate;
        private Date requiredDate;
        private Date shippedDate;
        private Integer shipVia;
        private BigDecimal freight;
        private String shipName;
        private String shipAddress;
        private String shipCity;
        private String shipRegion;
        private String shipPostalCode;
        private String shipCountry;
        private List<OrderDetailDTO> orderDetails;

        public OrderDTO setOrderDetailsAndReturn(List<OrderDetailDTO> orderDetailDTOList) {
            setOrderDetails(orderDetailDTOList);
            return this;
        }
    }

    @Data
    public static class OrderDetailDTO {
        private Integer productID;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal discount;
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DozerBeanMapper mapper;

    @Transactional
    @GetMapping("/count")
    public Integer getCount() {
        return orderRepository.count();
    }

    @Transactional
    @GetMapping
    public List<OrderDTO> get(@RequestParam Integer start, @RequestParam Integer end) {
        if(start == null || end == null) {
            return getAll();
        } else {
            return getSlice(start, end);
        }
    }

    private List<OrderDTO> getSlice(int start, int end) {
        return orderRepository
                .findSlice(start, end)
                .stream()
                .map(x -> mapper.map(x, OrderDTO.class))
                .map(x -> x.setOrderDetailsAndReturn(
                        orderDetailsRepository
                                .findByOrder(x.getOrderID())
                                .stream()
                                .map(y -> mapper.map(y, OrderDetailDTO.class))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    private List<OrderDTO> getAll() {
        return orderRepository
                .findAll()
                .stream()
                .map(x -> mapper.map(x, OrderDTO.class))
                .map(x -> x.setOrderDetailsAndReturn(
                        orderDetailsRepository
                                .findByOrder(x.getOrderID())
                                .stream()
                                .map(y -> mapper.map(y, OrderDetailDTO.class))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Transactional
    @GetMapping("/{id}")
    public OrderDTO getByID(@PathVariable Integer id) {
        Order order = orderRepository.findByID(id);
        OrderDTO orderDTO = mapper.map(order, OrderDTO.class);
        orderDTO.setOrderDetails(
                orderDetailsRepository
                        .findByOrder(id)
                        .stream()
                        .map(y -> mapper.map(y, OrderDetailDTO.class))
                        .collect(Collectors.toList()));

        return orderDTO;
    }

    @Transactional
    @PostMapping
    public OrderDTO creteOrder(@RequestBody OrderDTO order) {
        Order toCreate = mapper.map(order, Order.class);
        Order created = orderRepository.save(toCreate);
        saveOrderDetails(order, toCreate);
        return getByID(created.getOrderID());
    }

    @Transactional
    @PutMapping("/{id}")
    public OrderDTO updateOrder(@PathVariable Integer id, @RequestBody OrderDTO order) {
        Order toUpdate = orderRepository.findByID(id);
        order.setOrderID(toUpdate.getOrderID());
        mapper.map(order, toUpdate);
        removeOrderDetailsByOrderID(id);
        saveOrderDetails(order, toUpdate);
        return getByID(toUpdate.getOrderID());
    }

    @Transactional
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id) {
        removeOrderDetailsByOrderID(id);
        orderRepository.delete(id);
    }

    private void removeOrderDetailsByOrderID(int id) {
        for (OrderDetail orderDetail : orderDetailsRepository.findByOrder(id)) {
            orderDetailsRepository.delete(orderDetail.getId());
        }
    }

    private void saveOrderDetails(OrderDTO orderDTO, Order order) {
        for (OrderDetailDTO orderDetailDTO : orderDTO.getOrderDetails()) {
            OrderDetail detailToCreate = mapper.map(orderDetailDTO, OrderDetail.class);
            detailToCreate.setOrder(order);
            detailToCreate.setProduct(productRepository.findByID(orderDetailDTO.getProductID()));
            orderDetailsRepository.save(detailToCreate);
        }
    }
}
