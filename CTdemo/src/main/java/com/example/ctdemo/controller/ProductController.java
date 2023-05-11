package com.example.ctdemo.controller;

import com.commercetools.api.models.graph_ql.GraphQLResponse;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductPagedQueryResponse;
import com.example.ctdemo.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Product queryProductWIthId(@RequestParam String id) {
        return productService.query(id);
    }

    @GetMapping("/sku")
    public CompletableFuture<ProductPagedQueryResponse> queryProductWithSku(@RequestParam String sku) {
        return productService.queryWithSku(sku);
    }

    @GetMapping("/graphql/sku")
    public CompletableFuture<GraphQLResponse> queryProductWithSkuGraphql(@RequestParam String sku) {
        return productService.queryWithSkuGraphql(sku);
    }

}
