package com.example.ctdemo.model.customer;

import java.util.List;

public class CustomerResultResponseModel {
    private String results;
    private List<CustomerResult> customerResultList;

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public List<CustomerResult> getCustomerResultList() {
        return customerResultList;
    }

    public void setCustomerResultList(List<CustomerResult> customerResultList) {
        this.customerResultList = customerResultList;
    }
}
