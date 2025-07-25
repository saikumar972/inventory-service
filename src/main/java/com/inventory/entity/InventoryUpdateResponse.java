package com.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryUpdateResponse {
    private String product;
    private double amountPurchased;
    private double quantityConsumed;
}
