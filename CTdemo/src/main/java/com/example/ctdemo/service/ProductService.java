package com.example.ctdemo.service;

import com.commercetools.api.client.ByProjectKeyRequestBuilder;
import com.commercetools.api.models.graph_ql.GraphQLRequest;
import com.commercetools.api.models.graph_ql.GraphQLResponse;
import com.commercetools.api.models.product.Product;
import com.commercetools.api.models.product.ProductPagedQueryResponse;
import io.vrap.rmf.base.client.ApiHttpResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ProductService {
    private final ByProjectKeyRequestBuilder byProjectKeyRequestBuilder;

    public ProductService(ByProjectKeyRequestBuilder byProjectKeyRequestBuilder) {
        this.byProjectKeyRequestBuilder = byProjectKeyRequestBuilder;
    }

    public Product query(String id) {
        return byProjectKeyRequestBuilder.products().withId(id).get().executeBlocking().getBody();
        //return "Test Service - "+id;

    }

    public CompletableFuture<ProductPagedQueryResponse> queryWithSku(String sku) {
        return byProjectKeyRequestBuilder.products()
                .get()
                .withWhere("masterData(staged(masterVariant(sku = :sku))) or masterData(staged(variants(sku = :sku)))")
                .addPredicateVar("sku", sku)
                .execute().thenApply(ApiHttpResponse::getBody);
    }

    public CompletableFuture<GraphQLResponse> queryWithConstantSkuGraphql(String sku){
        GraphQLRequest gRequest = GraphQLRequest.builder()
                .query("query ReturnAProductSearched {\n" +
                        "  products(where: \"masterData(staged(masterVariant(sku=\\\"M0E20000000E52X\\\")))\"){\n" +
                        "    results{\n" +
                        "      skus\n" +
                        "    }\n" +
                        "  }  \n" +
                        "}")
                .build();
        return byProjectKeyRequestBuilder.graphql().post(gRequest).execute().thenApply(ApiHttpResponse::getBody);
    }

    public CompletableFuture<GraphQLResponse> queryWithSkuGraphql(String sku){
        GraphQLRequest gRequest = GraphQLRequest.builder()
                .query("query ReturnAProductSearched($productFilter:String) {\n" +
                           "  products(where: $productFilter){\n" +
                        "    results{\n" +
                        "      skus\n" +
                        "    }\n" +
                        "  }  \n" +
                        "}")
                .variables(builder -> builder.addValue("productFilter","masterData(staged(masterVariant(sku=\""+sku+"\")))"))
                .build();
        return byProjectKeyRequestBuilder.graphql().post(gRequest).execute().thenApply(ApiHttpResponse::getBody);
    }
}
