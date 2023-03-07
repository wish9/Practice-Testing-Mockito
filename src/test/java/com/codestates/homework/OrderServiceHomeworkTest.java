package com.codestates.homework;

import com.codestates.coffee.entity.Coffee;
import com.codestates.exception.BusinessLogicException;
import com.codestates.order.entity.Order;
import com.codestates.order.entity.OrderCoffee;
import com.codestates.order.repository.OrderRepository;
import com.codestates.order.service.OrderService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderServiceHomeworkTest {

    @Mock
    private OrderRepository orderRepository; // 테스트용 OrderRepository객체 생성
    @InjectMocks // 위에서 만든 객체 주입
    private OrderService orderService;

    @Test
    public void cancelOrderTest() {
        Order order = new Order();
        order.setOrderStatus(Order.OrderStatus.ORDER_COMPLETE);
        order.setOrderId(1L);

        given(orderRepository.findById(Mockito.anyLong()))
                .willReturn(Optional.of(order));

        assertThrows(BusinessLogicException.class, ()->orderService.cancelOrder(order.getOrderId()));
    }
}
