package com.yostocks.stocksservice.stock_tests;

import com.yostocks.stocksservice.stock.IStockRepository;
import com.yostocks.stocksservice.stock.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class StockRepositoryTests {

    @Autowired
    IStockRepository stockRepo;

    @Test
    void saveOneStock() {
        Stock stock = new Stock("google", 3.14, "Description Lorem ipsum");
        Stock returned = stockRepo.save(stock);
        assert ((stock.toString()).equals(returned.toString()));
    }

    @Test
    void getAllStocks() {
        List<Stock> stockList = (List<Stock>) stockRepo.findAll();
        assert (!stockList.isEmpty());
    }

    @Test
    void getStockById() {
        Optional<Stock> stock = stockRepo.findById(1L);
        assert (!stock.equals(null));
    }

   /* @Test
    void optimisticLockException() {

        Optional<Stock> optStock = stockRepo.findById(1);
        if (!optStock.isPresent()) {
            stockRepo.save(new Stock("stock_name", new BigDecimal( 7654323456.23), "description"));
        }
        optStock = stockRepo.findById(1);
        Stock stock = optStock.get();
        stock.setPercent_left(new BigDecimal(34567.23));
        stockRepo.save(stock);

        //here the optimistic locking fail exception
        stock.setPercent_left(new BigDecimal(345367.23));
        stockRepo.save(stock);

    }*/


}
