package com.yostocks.stocksservice;

import com.yostocks.stocksservice.fraction.BuyFractionRequestModel;
import com.yostocks.stocksservice.fraction.FractionService;
import com.yostocks.stocksservice.fraction.SellFractionRequestModel;
import com.yostocks.stocksservice.stock.IStockRepository;
import com.yostocks.stocksservice.stock.Stock;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DatabaseInitializer {

    private IStockRepository stockRepo;
    private FractionService fractionService;
    public DatabaseInitializer(IStockRepository stockRepo, FractionService fractionService) {

        this.stockRepo = stockRepo;
        this.fractionService = fractionService;
        init();
    }

    private void init() {
        // initialize stocks in database
        stockRepo.save(new Stock("GOOGL",100, "Lorem Ipsum Ipsum initial"));
        stockRepo.save(new Stock("TSLA", 100, "Lorem Ipsum Ipsum initial"));
        stockRepo.save(new Stock("SPOT", 100, "Lorem Ipsum Ipsum initial"));
        stockRepo.save(new Stock("MAERSK-B", 100, "Lorem Ipsum Ipsum initial"));
        stockRepo.save(new Stock("ADBE", 100, "Lorem Ipsum Ipsum initial")); // Adobe
        stockRepo.save(new Stock("BMW", 100, "Lorem Ipsum Ipsum initial"));
        stockRepo.save(new Stock("LEXUS", 100, "Lorem Ipsum Ipsum initial"));
        stockRepo.save(new Stock("UBI", 100, "Lorem Ipsum Ipsum initial"));

        /*fractionService.buyFraction(new BuyFractionRequestModel("GOOGL", 1L, 350));
        fractionService.buyFraction(new BuyFractionRequestModel("TSLA", 1L, 350));
        fractionService.buyFraction(new BuyFractionRequestModel("SPOT", 1L, 350));
        fractionService.buyFraction(new BuyFractionRequestModel("GOOGL", 1L, 250));

        fractionService.sellFractionByPercentage(new SellFractionRequestModel("GOOGL", 1L, 5 ));
        fractionService.sellFractionByPercentage(new SellFractionRequestModel("GOOGL", 1L, 1 ));
        fractionService.sellFractionByPercentage(new SellFractionRequestModel("SPOT", 1L, 1 ));
        fractionService.sellFractionByPercentage(new SellFractionRequestModel("TSLA", 1L, 1 ));
        fractionService.sellFractionByPercentage(new SellFractionRequestModel("TSLA", 1L, 3 ));

        fractionService.buyFraction(new BuyFractionRequestModel("GOOGL", 1L, 350));
        fractionService.buyFraction(new BuyFractionRequestModel("TSLA", 1L, 350));
        fractionService.buyFraction(new BuyFractionRequestModel("SPOT", 1L, 350));
        fractionService.buyFraction(new BuyFractionRequestModel("GOOGL", 1L, 250));*/





    }
}
