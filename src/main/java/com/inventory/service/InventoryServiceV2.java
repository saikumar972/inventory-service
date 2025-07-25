package com.inventory.service;

import com.inventory.entity.InventoryEntity;
import com.inventory.repo.InventoryRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
public class InventoryServiceV2 {
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
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // Collect results from all batches
        List<InventoryEntity> result = futures.stream()
                .flatMap(future -> future.join().stream())
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

}
