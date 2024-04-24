package com.example.ctdemo.controller;

import com.commercetools.api.models.cart.Cart;
import com.example.ctdemo.model.customer.CustomerAddress;
import com.example.ctdemo.service.CartService;
import com.example.ctdemo.service.ShippingAddressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/shippingAddress")
public class ShippingAddressController {

    @Autowired
    private ShippingAddressService shippingAddressService;

    @Autowired
    private CartService cartService;

    @PostMapping("/addAddress")
    public CompletableFuture<?> addShippingAddress(@RequestParam String anonymousId, @RequestBody CustomerAddress address) throws JsonProcessingException {
        CompletableFuture<Optional<Cart>> cartForAnonUser = cartService.getCartForAnonUser(anonymousId);

        return cartForAnonUser.thenApply(c -> {
            if (c.isPresent()) {
                return shippingAddressService.setShippingAddress(c.get(), address);
            }
            return CompletableFuture.completedFuture(null);
        }).thenCompose(e -> e);
    }
}
