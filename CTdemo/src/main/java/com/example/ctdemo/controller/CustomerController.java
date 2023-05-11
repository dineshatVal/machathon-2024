package com.example.ctdemo.controller;

import com.commercetools.api.models.customer.Customer;
import com.example.ctdemo.model.customer.CustomerAddress;
import com.example.ctdemo.service.CustomerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public CompletableFuture<Customer> queryCustomer(@RequestParam String id) {
        return customerService.query(id);
    }

    @GetMapping("/graphql/fname")
    public JsonNode queryCustomerByFname(@RequestParam String name) {
        return customerService.queryCustFNameGql(name);
    }

    @PostMapping("/addNationality")
    public CompletableFuture<Customer> addNationality(@RequestParam String name, @RequestParam String nationality) throws JsonProcessingException {
        return customerService.updateCustomerNationality(name, nationality);
    }

    @PostMapping("/addCustAddress")
    public CompletableFuture<Customer> addCustAddress(@RequestParam String name, @RequestBody CustomerAddress customerAddress) throws JsonProcessingException {
        return customerService.updateCustomerAddress(name, customerAddress);
    }

}
