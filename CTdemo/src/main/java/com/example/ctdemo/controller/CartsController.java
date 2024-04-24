package com.example.ctdemo.controller;

import com.commercetools.api.models.cart.Cart;
import com.example.ctdemo.model.cart.ItemToCart;
import com.example.ctdemo.service.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/carts")
public class CartsController {
    @Autowired
    private CartService cartService;
    
    @PostMapping("/addAnonCart")
    public CompletableFuture<Cart> addItemToCartAnonymous(@RequestBody ItemToCart itemToCart) throws JsonProcessingException {
        return cartService.addItemToCartAnonymous(itemToCart);
    }
}
