package com.inventory.contrller;

import com.inventory.entity.InventoryEntity;
import com.inventory.entity.InventoryUpdateResponse;
import com.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    InventoryService inventoryService;
    @PostMapping("/add")
    public ResponseEntity<List<InventoryEntity>> inventoryList(@RequestBody List<InventoryEntity> inventoryEntities){
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.addInventories(inventoryEntities));
    }
    @GetMapping("/findAll")
    public List<InventoryEntity> getAllInventories(){
        return inventoryService.getAllInventories();
    }
    @GetMapping("/name/{product}")
    public ResponseEntity<InventoryEntity> inventoryList(@PathVariable String product){
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.findByProductName(product));
    }
    @PostMapping("/products")
    public ResponseEntity<List<InventoryEntity>> inventoryListByName(@RequestBody List<String> products){
        return ResponseEntity.status(HttpStatus.OK).body(inventoryService.findProductsByName(products));
    }

    @PutMapping("/updateInventory")
    public ResponseEntity<InventoryUpdateResponse> updateInventory(@RequestParam String productName,@RequestParam double quantity){
        InventoryUpdateResponse updatedInventory=inventoryService.updateInventory(productName,quantity);
        return ResponseEntity.status(HttpStatus.OK).body(updatedInventory);
    }
}
