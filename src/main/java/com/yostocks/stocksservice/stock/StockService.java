package com.yostocks.stocksservice.stock;

import com.yostocks.stocksservice.network.currency_converter.CurrencyExchangeService;
import com.yostocks.stocksservice.network.yahoofinance.YahooFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    private IStockRepository stockRepository;
    private YahooFinanceService yahooFinanceService;
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    public StockService(IStockRepository stockRepository, YahooFinanceService yahooFinanceService,
                        CurrencyExchangeService currencyExchangeService) {
        this.stockRepository = stockRepository;
        this.yahooFinanceService = yahooFinanceService;
        this.currencyExchangeService = currencyExchangeService;
    }

    public Optional<Stock> findStockById(Long id) {
        Optional<Stock> s = stockRepository.findById(id);
        return s;
    }


    public List<Stock> findAll() {

        List<Stock> list = null;
        list = (List<Stock>) stockRepository.findAll();
        return list;
    }


    public List<StockHistoryModel> getSingleStockHistory(String name, int year, String searchType) {

        List<StockHistoryModel> stockHistory = null;
        stockHistory = yahooFinanceService.getFullHistoryOfSingleStock(name, year, searchType);
        return stockHistory;
    }


    public List<List<StockHistoryModel>> getAllStockHistory(String[] names, int year, String searchType) {

        List<List<StockHistoryModel>> historiesOfStocks = null;
        historiesOfStocks = yahooFinanceService.getFullHistoryOfAllStocks(names, year, searchType);

        return historiesOfStocks;
    }

    public double getCurrentPriceOfStock(String symbol) {

        double priceDKK = yahooFinanceService.getCurrentPriceOfSingleStock(symbol);
        BigDecimal priceBD = new BigDecimal(priceDKK).setScale(2, RoundingMode.CEILING);

        return priceBD.doubleValue();

    }

}
