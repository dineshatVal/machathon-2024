package com.example.ctdemo.controller;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.order.Order;
import com.example.ctdemo.service.CartService;
import com.example.ctdemo.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;


    @PostMapping("/placeorderAnon")
    public CompletableFuture<CompletableFuture<?>> makeOrderAnonymous(@RequestParam String anonymousId) throws JsonProcessingException {
        CompletableFuture<Optional<Cart>> cartForUser = cartService.getCartForAnonUser(anonymousId);
        return cartForUser.thenApply(c -> {
            if (c.isPresent()) {
                return orderService.placeorder(c.get());
            }
            return CompletableFuture.completedFuture(null);
        });
    }
}
