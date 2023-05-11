package com.example.ctdemo.service;

import com.commercetools.api.client.ByProjectKeyRequestBuilder;
import com.commercetools.api.models.cart.*;
import com.example.ctdemo.model.cart.ItemToCart;
import com.example.ctdemo.model.customer.CustomerResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vrap.rmf.base.client.ApiHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CartService {
    @Autowired
    private ByProjectKeyRequestBuilder byProjectKeyRequestBuilder;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ObjectMapper objectMapper;
    
    public CompletableFuture<Cart> addItemToCart(String name, ItemToCart itemToCart) throws JsonProcessingException {
        JsonNode jsonNode = customerService.queryCustFNameGql(name);//.get("data").get("customers").get("results");
        String id = null;
        if (jsonNode.size() > 0) {
            CustomerResult customerResult = objectMapper.treeToValue(jsonNode.get(0), CustomerResult.class);
            id = customerResult.getId();
        } else {
            return CompletableFuture.completedFuture(null);
        }
        LineItemDraft lineItemDraft = LineItemDraftBuilder.of()
                .sku(itemToCart.getSku())
                .quantity(itemToCart.getQuantity())
                .build();
        CartDraft cartDraft = CartDraftBuilder.of()
                .lineItems(lineItemDraft)
                .currency("EUR")
                .customerId(id)
                .build();

        return byProjectKeyRequestBuilder.carts()
                .post(cartDraft)
                .execute().thenApply(ApiHttpResponse::getBody);
    }

    public CompletableFuture<Optional<Cart>> getCartForUser(String name) throws JsonProcessingException {
        JsonNode jsonNode = customerService.queryCustFNameGql(name);//.get("data").get("customers").get("results");
        String id = null;
        if (jsonNode.size() > 0) {
            CustomerResult customerResult = objectMapper.treeToValue(jsonNode.get(0), CustomerResult.class);
            id = customerResult.getId();
        } else {
            return CompletableFuture.completedFuture(null);
        }
        return byProjectKeyRequestBuilder.carts()
                .get()
                .withWhere("customerId = \"" + id + "\"" + "and cartState = \"" + CartState.CartStateEnum.ACTIVE + "\"")
                .withLimit(1)
                .execute().thenApply(ApiHttpResponse::getBody).thenApply(e -> e.getResults().stream().findFirst());

    }
}
