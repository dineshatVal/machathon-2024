package com.example.ctdemo.service;

import com.commercetools.api.client.ByProjectKeyRequestBuilder;
import com.commercetools.api.models.cart.*;
import com.commercetools.api.models.common.AddressBuilder;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderFromCartDraft;
import com.commercetools.api.models.order.OrderFromCartDraftBuilder;
import com.example.ctdemo.model.customer.CustomerResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vrap.rmf.base.client.ApiHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {
    @Autowired
    private ByProjectKeyRequestBuilder byProjectKeyRequestBuilder;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ObjectMapper objectMapper;

    public CompletableFuture<Order> placeorder(Cart cart){
        CartSetShippingAddressAction cartSetShippingAddressAction = CartSetShippingAddressActionBuilder.of()
                .address(AddressBuilder.of().country("DE").build()).build();
        CartUpdate cartUpdate = CartUpdateBuilder.of()
                .actions(cartSetShippingAddressAction)
                .version(cart.getVersion()).build();
        CompletableFuture<Cart> updatedCart = byProjectKeyRequestBuilder.carts()
                .withId(cart.getId())
                .post(cartUpdate).execute().thenApply(ApiHttpResponse::getBody);

        return updatedCart.thenApply(f -> {
            OrderFromCartDraft orderFromCartDraft = OrderFromCartDraftBuilder.of()
                    .cart(CartResourceIdentifierBuilder.of().id(f.getId()).build())
                    .version(f.getVersion())
                    .build();

            return byProjectKeyRequestBuilder.orders()
                    .post(orderFromCartDraft)
                    .execute().thenApply(ApiHttpResponse::getBody);
        }).thenCompose(g -> g);

    }

    public CompletableFuture<List<Order>> getAllOrders(String fname) throws JsonProcessingException {
        JsonNode jsonNode = customerService.queryCustFNameGql(fname);
        String id = null;
        if(jsonNode.size()>0){
            CustomerResult customerResult = objectMapper.treeToValue(jsonNode.get(0), CustomerResult.class);
            id = customerResult.getId();
        } else {
            return null;
        }

        return byProjectKeyRequestBuilder.orders()
                .get()
                .withWhere("customerId = \"" + id + "\"")
                .execute().thenApply(ApiHttpResponse::getBody).thenApply(f -> f.getResults());

    }
}
