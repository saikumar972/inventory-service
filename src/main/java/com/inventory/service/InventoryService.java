package com.inventory.service;

import com.inventory.entity.InventoryEntity;
import com.inventory.entity.InventoryUpdateResponse;
import com.inventory.repo.InventoryRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryRepo inventoryRepo;
    private final ExecutorService executorService= Executors.newFixedThreadPool(4);
    public List<InventoryEntity> addInventories(List<InventoryEntity> inventoryEntityList){
        int totalSize=inventoryEntityList.size();
        int batchSize = (int) Math.ceil((double) totalSize / 4);
        List<List<InventoryEntity>> inventoryBatches=inventoryBatches(inventoryEntityList,batchSize);
        List<CompletableFuture<List<InventoryEntity>>> futures = inventoryBatches.stream()
                .map(batchList -> CompletableFuture.supplyAsync(() -> {
                    long start = System.currentTimeMillis();
                    List<InventoryEntity> saved = addByBatch(batchList);
                    long end = System.currentTimeMillis();
                    System.out.println("Batch processed in " + (end - start) + " ms by " + Thread.currentThread().getName());
                    return saved;
                },executorService)).toList();
        List<InventoryEntity> result = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .toList();
        return result;
    }

    public List<InventoryEntity> addByBatch(List<InventoryEntity> inventoryEntityList){
        return inventoryRepo.saveAll(inventoryEntityList);
    }

    public List<List<InventoryEntity>> inventoryBatches(List<InventoryEntity> inventoryEntityList,int batchSize){
        int totalSize= inventoryEntityList.size();
        List<List<InventoryEntity>> inventoryBatches=new ArrayList<>();
        for(int i=0;i<totalSize;i+=batchSize){
            int end=Math.min(i+batchSize,totalSize);
            inventoryBatches.add(inventoryEntityList.subList(i,end));
        }
        return inventoryBatches;
    }

    public InventoryEntity findByProductName(String product) {
        return inventoryRepo.findByProduct(product).orElseThrow(()->new IllegalArgumentException("product not found"));
    }

    public List<InventoryEntity> findProductsByName(List<String> products){
        List<InventoryEntity>  inventoryList=products.stream()
                .map(this::findByProductName)
                .toList();
        return inventoryList;
    }

    public InventoryUpdateResponse updateInventory(String product, double consumedQuantity){
        double presentQuantity=inventoryRepo.getQuantityByProductName(product);
        //write a single sql query to get quantity and price
        InventoryUpdateResponse inventoryUpdateResponse=new InventoryUpdateResponse();
        if(consumedQuantity>presentQuantity){
            throw new IllegalArgumentException("The expected Quantity not present in the stock try less");
        }else{
            double priceOfProduct=inventoryRepo.getPriceByProductName(product);
            double totalPriceOfProducts=priceOfProduct*consumedQuantity;
            inventoryUpdateResponse.setAmountPurchased(totalPriceOfProducts);
            inventoryUpdateResponse.setProduct(product);
            inventoryUpdateResponse.setQuantityConsumed(consumedQuantity);
        }
        int updateInventory=inventoryRepo.updateQuantityByProductName(presentQuantity-consumedQuantity,product);
        System.out.println("Inventory stock is updated");
        return inventoryUpdateResponse;
    }

    public List<InventoryEntity> getAllInventories() {
        return inventoryRepo.findAll();
    }
}
