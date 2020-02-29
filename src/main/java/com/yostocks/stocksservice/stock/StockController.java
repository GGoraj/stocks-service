package com.yostocks.stocksservice.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/")
public class StockController {

    private StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }


    @Async
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CompletableFuture findStockById(@PathVariable Long id) {
        if (id <= 0) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(Collections.singletonMap("response", "incorrect id"), HttpStatus.NOT_FOUND));
        }
        Optional<Stock> s = stockService.findStockById(id);
        if (s.isPresent()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(s, HttpStatus.OK));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>(Collections.singletonMap("response", "no stock found"), HttpStatus.NOT_FOUND));
        }
    }

    @Async
    @GetMapping("/all")
    public CompletableFuture getAllStocks() {

        List<Stock> list = stockService.findAll();
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(Collections.singletonMap("response", "no stock found"), HttpStatus.NOT_FOUND));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>(list, HttpStatus.OK));
        }
    }

    @Async
    @PostMapping("/history/single")
    public CompletableFuture getSingleStockHistory(@RequestBody GetHistoryOfSingleStockRequestModel requestModel) {

        List<StockHistoryModel> stockHistory = stockService.getSingleStockHistory(requestModel.getName(),
                requestModel.getYear(),
                requestModel.getSearchType());

        if (stockHistory.isEmpty()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(Collections.singletonMap("response", "no history of chosen stock"), HttpStatus.NOT_FOUND));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>(stockHistory, HttpStatus.OK));
        }

    }

    @Async
    @PostMapping("/history/all")
    public CompletableFuture getAllStockHistory(@RequestBody GetHistoryOfAllStocksRequestModel requestModel) {

        List<List<StockHistoryModel>> historiesOfStocks = stockService.getAllStockHistory(requestModel.getNames(),
                requestModel.getYear(),
                requestModel.getSearchType());
        if (historiesOfStocks.isEmpty()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(Collections.singletonMap("response", "no history to show"), HttpStatus.NOT_FOUND));
        } else {
            return CompletableFuture.completedFuture(new ResponseEntity<>(historiesOfStocks, HttpStatus.OK));
        }

    }

    @Async
    @PostMapping("/price")
    public CompletableFuture getCurrentPriceOfStock(@RequestBody GetCurrentStockPriceRequestModel model) {

        double result = stockService.getCurrentPriceOfStock(model.getSymbol());

        return CompletableFuture.completedFuture(new ResponseEntity<>(Collections.singletonMap("price", result), HttpStatus.OK));
    }

}
