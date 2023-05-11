package com.example.ctdemo.service;

import com.commercetools.api.client.ByProjectKeyRequestBuilder;
import com.commercetools.api.models.common.AddressDraft;
import com.commercetools.api.models.common.AddressDraftBuilder;
import com.commercetools.api.models.common.BaseAddressBuilder;
import com.commercetools.api.models.customer.*;
import com.commercetools.api.models.graph_ql.GraphQLRequest;
import com.commercetools.api.models.graph_ql.GraphQLRequestBuilder;
import com.commercetools.api.models.type.*;
import com.example.ctdemo.model.customer.CustomerAddress;
import com.example.ctdemo.model.customer.CustomerResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vrap.rmf.base.client.ApiHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CustomerService {
    @Autowired
    private ByProjectKeyRequestBuilder byProjectKeyRequestBuilder;
    private final ObjectMapper objectMapper;

    public CustomerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<Customer> query(String id) {
        return byProjectKeyRequestBuilder.customers().withId(id).get().execute().thenApply(ApiHttpResponse::getBody);
    }

    public JsonNode queryCustFNameGql(String name) {
        GraphQLRequest gqlRequest = GraphQLRequestBuilder.of()
                .query("query ReturnASingleCustomerSearch($customerFilter: String) {\n" +
                        "  customers(where: $customerFilter) {\n" +
                        "    results{\n" +
                        "      firstName\n" +
                        "      lastName\n" +
                        "      middleName\n" +
                        "      email\n" +
                        "      id\n" +
                        "    }    \n" +
                        "  }\n" +
                        "}")
                .variables(b -> b.addValue("customerFilter", "firstName=\"" + name + "\""))
                .build();
        /*CompletableFuture<GraphQLResponse> graphQLResponseCompletableFuture = byProjectKeyRequestBuilder.graphql().post(gqlRequest).execute().thenApply(ApiHttpResponse::getBody);
        CompletableFuture<Object> objectCompletableFuture = graphQLResponseCompletableFuture.thenApply(e -> e.getData());
        Object join = objectCompletableFuture.join();
        return objectCompletableFuture;*/
        ApiHttpResponse<JsonNode> jsonNodeApiHttpResponse = byProjectKeyRequestBuilder.graphql().post(gqlRequest).executeBlocking(JsonNode.class);
        //return jsonNodeApiHttpResponse.getBody().get("data").get("customers").get("results");
        return jsonNodeApiHttpResponse.getBody().at("/data/customers/results");
        //return byProjectKeyRequestBuilder.graphql().post(gqlRequest).execute().thenApply(ApiHttpResponse::getBody);
    }


    public CompletableFuture<Customer> updateCustomerNationality(String name, String nationality) throws JsonProcessingException {
        //JsonNode jsonNode = queryCustFNameGql(name);
        JsonNode jsonNode = queryCustFNameGql(name);//.get("data").get("customers").get("results");
        if (jsonNode.size() > 0) {
            CustomerResult customerResult = objectMapper.treeToValue(jsonNode.get(0), CustomerResult.class);
            String id = customerResult.getId();
            CompletableFuture<Customer> customerCf = query(id);
            return customerCf.thenApply(c -> {
                CustomerSetCustomTypeAction customerSetTypeAction = CustomerSetCustomTypeActionBuilder.of()
                        .type(TypeResourceIdentifierBuilder.of().key("type-customer").build())
                        .fields(FieldContainerBuilder.of().addValue("nationality", nationality).build())
                        .build();
                // CustomerSetCustomFieldAction customerSetCustomFieldAction  = CustomerSetCustomFieldActionBuilder.of().name("nationality").value(nationality).build();
                /*CustomerSetCustomFieldAction customerSetCustomFieldAction = CustomerUpdateActionBuilder.of()
                        .setCustomFieldBuilder()
                        .name("nationality")
                        .value(nationality)
                        .build();*/
                CustomerUpdate customerUpdate = CustomerUpdateBuilder.of()
                        .version(c.getVersion())
                        .actions(customerSetTypeAction).build();
                return byProjectKeyRequestBuilder.customers()
                        .withId(id)
                        .post(customerUpdate)
                        .executeBlocking().getBody();
            });

        }
        return null;

    }

    public CompletableFuture<Customer> updateCustomerAddress(String name, CustomerAddress customerAddress) throws JsonProcessingException {
        JsonNode jsonNode = queryCustFNameGql(name);//.get("data").get("customers").get("results");
        if (jsonNode.size() > 0) {
            CustomerResult customerResult = objectMapper.treeToValue(jsonNode.get(0), CustomerResult.class);
            String id = customerResult.getId();
            CompletableFuture<Customer> customerCf = query(id);
            return customerCf.thenApply(c -> {
                List<CustomerUpdateAction> customerUpdateActionList = new ArrayList<>();
                AddressDraft addressDraft = AddressDraftBuilder.of()
                        .city(customerAddress.getCity())
                        .apartment(customerAddress.getBuilding())
                        .country(customerAddress.getCountry())
                        .custom(CustomFieldsDraftBuilder.of()
                                .type(TypeResourceIdentifierBuilder.of().key("type-customer-address").build())
                                .fields(FieldContainerBuilder.of().addValue("floor", customerAddress.getFloor()).addValue("door", customerAddress.getDoor()).build())
                                .build())
                        .build();
                CustomerAddAddressAction customerAddAddressAction = CustomerUpdateActionBuilder.of()
                        .addAddressBuilder().address(addressDraft).build();
                return byProjectKeyRequestBuilder.customers()
                        .withId(c.getId())
                        .post(CustomerUpdateBuilder.of()
                                .version(c.getVersion())
                                .actions(customerAddAddressAction).build())
                        .executeBlocking().getBody();

                /*CustomerAddAddressAction customerAddAddressAction1 = CustomerAddAddressActionBuilder.of()
                        .address(BaseAddressBuilder.of()
                                .city(customerAddress.getCity())
                                .apartment(customerAddress.getBuilding())
                                .country(customerAddress.getCountry())
                                .build())
                        .build();

                customerUpdateActionList.add(customerAddAddressAction);
                *//*customerUpdateActionList.add(CustomerSetAddressCustomTypeActionBuilder.of()
                        .type(TypeResourceIdentifierBuilder.of().key("type-customer-address").build())
                        .fields(FieldContainerBuilder.of().addValue("floor",customerAddress.getFloor()).addValue("door",customerAddress.getDoor()).build())
                        .build());*//*

                CustomerUpdate customerUpdate = CustomerUpdateBuilder.of()
                        .version(c.getVersion())
                        .actions(customerAddAddressAction).build();
                Customer customer = byProjectKeyRequestBuilder.customers()
                        .withId(id)
                        .post(customerUpdate)
                        .executeBlocking().getBody();
                CustomerSetAddressCustomTypeAction custAct = CustomerSetAddressCustomTypeActionBuilder.of()
                        .addressId(customer.getAddresses().get(0).getId())
                        .type(TypeResourceIdentifierBuilder.of().key("type-customer-address").build())
                        .fields(FieldContainerBuilder.of().addValue("floor", customerAddress.getFloor()).addValue("door", customerAddress.getDoor()).build())
                        .build();
                return byProjectKeyRequestBuilder.customers()
                        .withId(customer.getId())
                        .post(CustomerUpdateBuilder.of()
                                .version(customer.getVersion())
                                .actions(custAct).build())
                        .executeBlocking().getBody();*/
            });

        }
        return null;
    }
}