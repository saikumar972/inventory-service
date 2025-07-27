package com.inventory.service;

import com.inventory.entity.InventoryEntity;
import com.inventory.entity.InventoryUpdateResponse;
import com.inventory.exception.ProductException;
import com.inventory.repo.InventoryRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
@Log4j2
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

    public List<InventoryEntity> getAllInventories() {
        return inventoryRepo.findAll();
    }

    public List<InventoryEntity> findProductsByName(List<String> products){
        List<InventoryEntity> inventoryList=products.stream()
                .map(this::findByProductName)
                .toList();
        return inventoryList;
    }
    @Transactional
    public InventoryUpdateResponse updateInventory(String product, double consumedQuantity){
        Object resultObj = inventoryRepo.getQuantityAndPriceByProductName(product)
                .orElseThrow(() -> new ProductException("Invalid product name"));
        Object[] result = (Object[]) resultObj;
        double presentQuantity = ((Number) result[0]).doubleValue();
        double priceOfProduct = ((Number) result[1]).doubleValue();
        InventoryUpdateResponse inventoryUpdateResponse=checkAvailability(product, consumedQuantity);
        int rowsUpdated= inventoryRepo.updateQuantityByProductName(presentQuantity-consumedQuantity,product);
        log.info("InventoryService :: updated the inventory and number of rows impacted were {} ",rowsUpdated);
        return inventoryUpdateResponse;
    }

    public InventoryUpdateResponse checkAvailability(String product, double consumedQuantity){
        log.info("InventoryService :: checking inventory");
        //write a single SQL query to get quantity and price
        InventoryUpdateResponse inventoryUpdateResponse=null;
        Object resultObj = inventoryRepo.getQuantityAndPriceByProductName(product)
                .orElseThrow(() -> new ProductException("Invalid product name"));
        Object[] result = (Object[]) resultObj;
        double presentQuantity = ((Number) result[0]).doubleValue();
        log.info("InventoryService :: present quantity {}",presentQuantity);
        double priceOfProduct = ((Number) result[1]).doubleValue();
        log.info("InventoryService :: product price {}",priceOfProduct);
        if(consumedQuantity<=0){
            throw new ProductException("consumed quantity should be greater than zero");
        }
        if(consumedQuantity>presentQuantity){
            log.info("InventoryService :: consumed quantity is greater than the present quantity : {}",consumedQuantity);
            throw new ProductException("The expected Quantity not present in the stock try less");
        }else{
            double totalPriceOfProducts=priceOfProduct*consumedQuantity;
            inventoryUpdateResponse=new InventoryUpdateResponse();
            inventoryUpdateResponse.setAmountPurchased(totalPriceOfProducts);
            inventoryUpdateResponse.setProduct(product);
            inventoryUpdateResponse.setQuantityConsumed(consumedQuantity);
        }
        log.info("InventoryService :: all checks done");
        return inventoryUpdateResponse;
    }

}

