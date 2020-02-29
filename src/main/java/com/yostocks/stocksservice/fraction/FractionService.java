package com.yostocks.stocksservice.fraction;

import com.yostocks.stocksservice.network.accounting.AccountingService;
import com.yostocks.stocksservice.network.accounting.TransactionType;
import com.yostocks.stocksservice.network.yahoofinance.YahooFinanceService;
import com.yostocks.stocksservice.stock.IStockRepository;
import com.yostocks.stocksservice.stock.Stock;
import com.yostocks.stocksservice.stock.StockService;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

@Service
public class FractionService {

    private IFractionRepository repoFractions;
    private IStockRepository repoStocks;
    private YahooFinanceService yahooFinanceService;
    private AccountingService accountingService;
    private StockService stockService;




    private EntityManagerFactory emf;

    @Autowired
    public FractionService(IFractionRepository repoFractions,
                           IStockRepository repoStocks,
                           YahooFinanceService yahooFinanceService,
                           AccountingService accountingService,
                           StockService stockService,
                           EntityManagerFactory emf) {

        this.repoFractions = repoFractions;
        this.repoStocks = repoStocks;
        this.yahooFinanceService = yahooFinanceService;
        this.accountingService = accountingService;
        this.stockService = stockService;
        this.emf = emf;
    }


    public double sellFractionByPercentage(SellFractionRequestModel requestModel) {

        // check if percent is valid
        if (requestModel.getPercent() <= 0) {
            return 0;
        }

        /**
         *  NOTE: Each Fraction has to be sold to its parent Stock
         *  this way ensuring order within database: stock percentage is never over 100 in any of the stocks
         */

        // load up the fraction
        Optional<Fraction> f = Optional.ofNullable(repoFractions.findFractionByStockSymbolAndUserIAndStockPercentBelowHundered(requestModel.getUser_id(), requestModel.getStock_symbol()));
        Fraction fraction = null;
        if(!f.isPresent()){
            return -1;
        }
        fraction = f.get();

        // check if request % is <= fraction % (its what user already has)
        double fractionPercent = fraction.getPercent();
        double requestedPercent = requestModel.getPercent();

        // return fraction's current percent if % is insufficient
        if (fractionPercent < requestedPercent) {
            return -2;
        }


        // get current stock price
        double currentStockPrice = yahooFinanceService.getCurrentPriceOfSingleStock(requestModel.getStock_symbol());
        // convert to BigDecimal for more accurate calculations
        BigDecimal stockPrice = BigDecimal.valueOf(currentStockPrice);
        //double currentBalance = accountingService.getBalance(requestModel.getUser_id());

        // calculate value of requested percentage: gain = stockPrice* 100/%
        BigDecimal percentToCashValue = stockPrice.multiply(new BigDecimal(requestedPercent));
        percentToCashValue = percentToCashValue.divide(BigDecimal.valueOf(100));
        percentToCashValue = percentToCashValue.setScale(2, RoundingMode.UP);

        // update balance
        Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        String result = accountingService.updateBalanceAndRegisterTransaction(requestModel.getUser_id(), fraction.getId(),
                TransactionType.SALE, percentToCashValue.doubleValue(),
                timestamp);

        // check if update for accounting-service succeed - update stock record (increase percentage)
        if (result.equals("success")) {
            // subtract request % from existing fraction %
            double newFractionPercent = (BigDecimal.valueOf(fractionPercent).subtract(BigDecimal.valueOf(requestedPercent))).doubleValue();
            fraction.setPercent(newFractionPercent);
            Fraction fractionDb = repoFractions.save(fraction);
            if(fractionDb.equals(null) || fractionDb == null){
                return -3;
            }
            else {
                // fraction update succeded

                //update related stocks
                // keep adding percent to stocks until requestPercent for sale equals 0
                Set<Stock> fractionStocks = repoFractions.findById(fraction.getId()).get().getStocks();
                BigDecimal requestPercentBD = new BigDecimal(requestedPercent).setScale(2, RoundingMode.DOWN);

                while(!requestPercentBD.equals(new BigDecimal(0))){
                    for (Stock stock: fractionStocks
                         ) {
                        BigDecimal stockPercentBD = BigDecimal.valueOf(stock.getPercent_left());
                        BigDecimal available = new BigDecimal(100).subtract(stockPercentBD);

                        if((available.doubleValue() < requestPercentBD.doubleValue()) || (available.equals(requestPercentBD)) ) {
                            // remove stock from joined table
                            repoStocks.deleteFromFractionsStocks(fraction.getId(), stock.getId());

                            // update stock with new value 100
                            Optional<Stock> stockOptional = repoStocks.findById(stock.getId());
                            Stock stockDb = stockOptional.get();
                            stockDb.setPercent_left(100);
                            repoStocks.save(stockDb);

                            // update requestPercentBD ( for while loop condition )
                            requestPercentBD = requestPercentBD.subtract(available);

                        }
                        // available > request
                        else{
                            // update stock with new value 100
                            Optional<Stock> stockOptional = repoStocks.findById(stock.getId());
                            Stock stockDb = stockOptional.get();
                            double newStockPercentLeft = stockPercentBD.add(requestPercentBD).doubleValue();
                            stockDb.setPercent_left(newStockPercentLeft);
                            repoStocks.save(stockDb);

                            // update requestPercent
                            requestPercentBD = new BigDecimal(0);
                        }
                    }
                }
                // update fraction
                Optional<Fraction> fractionFromDb = repoFractions.findById(fraction.getId());
                fraction = fractionFromDb.get();

                // delete fraction that holds 0% of stock
                if(fraction.getPercent() == 0){
                    repoFractions.deleteById(fraction.getId());
                }
                // save fraction > 0%
                else {
                    repoFractions.save(fraction);
                }
            }


            //"stock fractions sold";
            return 1;
        } else {
            // fraction was not sold
            return -3;
        }


    }


    // returns key value pair <stock_id, percent_invested> for requested user_id
    public Collection<Fraction> getAllFractionsByUserId(Long user_id) {

        Collection<Fraction> userFractions;
        userFractions = repoFractions.findAllByUserId(user_id);
        return userFractions;

    }

    public HashMap<Long, String> getStockSymbolsByAllFractions(Collection<Fraction> fractions) {

        HashMap<Long, String> fractionToStockSymbolMap = new HashMap<>();

        for (Fraction f : fractions
        ) {

            String stockSymbol = repoFractions.findStockSymbolOfFraction(f.getId());
            fractionToStockSymbolMap.put(f.getId(), stockSymbol);
        }

        return fractionToStockSymbolMap;

      /*  Fraction f = repoFractions.findById(fractions.get(0).getId()).get();
            EntityManager em = emf.createEntityManager();
            f = em.merge(f);
            f.getStocks().forEach(stock -> {
                System.out.println(stock.getSymbol());
            });

            em.close();*/
    }


    /**
     * This method assigns percentage ownership(of User) - fraction to stock.
     * - calculate 1% provision and subtracts it from user's requested amount
     * - check current stock market price
     * in each step we try to update stock as fast as possible
     * in order to make whole procedure in shortest time possible to avoid the resource lock (Positive Lock)
     *
     * @return
     */


    public String buyFraction(BuyFractionRequestModel buyFractionRequestModel) {

        //check if user's balance is > then request amount
        double balance = accountingService.getBalance(buyFractionRequestModel.getUser_id());
        if(balance < buyFractionRequestModel.getAmount()){
            return "insufficient credit";
        }

        // check if stock symbol referres to the available stock
        Optional<Stock> optionalStock = Optional.ofNullable(repoStocks.findBySymbol(buyFractionRequestModel.getStock_symbol()));
        if(!optionalStock.isPresent()) return "stock does not exist";

        // calculate provision and amount at client's disposal
        BigDecimal amount = new BigDecimal(buyFractionRequestModel.getAmount());
        BigDecimal one_hundred_percent = new BigDecimal(100);
        BigDecimal provision = amount.divide(one_hundred_percent, 3, RoundingMode.DOWN);
        // remaining amount
        BigDecimal amountAfterProvision = amount.subtract(provision);

        // check the actual stock price with YahooFinance Api
        String stock_symbol = buyFractionRequestModel.getStock_symbol();
        Double double_marketPrice = yahooFinanceService.getCurrentPriceOfSingleStock(stock_symbol);

        // in case the yahoo finance service was not returning anything
        if(double_marketPrice == null){
            return null;
        }
        // wrap market_price with BigDecimal for precise calculations
        BigDecimal market_price = new BigDecimal(double_marketPrice).setScale(2, RoundingMode.CEILING);

        // calculate user's fraction percentage
        BigDecimal requestPercent = amountAfterProvision.divide(market_price, 2).multiply(one_hundred_percent);


        return createFraction(amountAfterProvision, requestPercent, buyFractionRequestModel.getUser_id(), buyFractionRequestModel.getStock_symbol());
    }

    @Transactional(rollbackOn = {ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class, StaleObjectStateException.class})
    public String createFraction(BigDecimal amountAfterProvision, BigDecimal requestPercent, Long user_id, String stockSymbol) throws ObjectOptimisticLockingFailureException, OptimisticLockingFailureException {

        Long createdFractionId = 0L;

        while (!requestPercent.equals(new BigDecimal(0))) {
/**
 * select fs.stock_id, fs.fraction_id from stocks s inner join fractions_stocks fs on s.id = fs.stock_id inner join fractions f on f.user_id = 1;
 */
            // check if there is any stock of required symbol available
            // if yes - use it
            // if no - create a new one
            Optional<Stock> stockOptional = Optional.ofNullable(repoStocks.findBySymbolAndPercentGreaterThenZero(stockSymbol));
            Stock stockFromDb = null;
            if (stockOptional.isPresent()) {

                stockFromDb = stockOptional.get();

            } else {

                stockFromDb = repoStocks.save(new Stock(stockSymbol, 100, "Lorem ipsum Description"));

            }

            //save available percentage
            BigDecimal stockFromDbPercentLeft = BigDecimal.valueOf(stockFromDb.getPercent_left());

            //check if user bought the stock already
            Optional<Fraction> fractionOptional = Optional.ofNullable(repoFractions.findFractionByStockSymbolAndUserId(user_id, stockSymbol));
            Fraction fraction = null;

            // if currentStock% <= requestPercent (insufficient to fulfill the requirement)
            // use all % there is in stockFromDb
            if (stockFromDbPercentLeft.doubleValue() <= requestPercent.doubleValue()) {

                // update value of chosen stock
                stockFromDb.setPercent_left(0);
                repoStocks.save(stockFromDb);

                // user owns the fraction
                if (fractionOptional.isPresent()) {

                    // increase percent
                    fraction = fractionOptional.get();
                    BigDecimal fractionPercent = BigDecimal.valueOf(fraction.getPercent());
                    fractionPercent = fractionPercent.add(stockFromDbPercentLeft);
                    fraction.setPercent(fractionPercent.doubleValue());

                    // add another stock to fraction
                    Set<Stock> fractionStocks = fraction.getStocks();
                    /**
                     *              exception :
                     */
                    //stockFromDb.setVersion(null);
                    fractionStocks.add(stockFromDb);
                    fraction.setStocks(fractionStocks);
                    Fraction f =  repoFractions.save(fraction);
                    f.setVersion(f.getVersion()-1);
                    repoFractions.save(f);
                }

                // user haven't bought the stock yet, so we create a new fraction
                else {

                    // create new fraction
                    fraction = new Fraction(user_id, stockFromDbPercentLeft.doubleValue());
                    Set<Stock> stocks = new HashSet<>();
                    stocks.add(stockFromDb);

                    fraction.setStocks(stocks);
                    repoFractions.save(fraction);

                }
                requestPercent = requestPercent.subtract(stockFromDbPercentLeft);

            }

            // currentStock% is > requestPercent (fully sufficient to fulfill requirement)
            else {
                // update the stock
                BigDecimal newPercentLeft = stockFromDbPercentLeft.subtract(requestPercent);
                stockFromDb.setPercent_left(newPercentLeft.doubleValue());
                repoStocks.save(stockFromDb);

                // if user invested in that stock already
                if (fractionOptional.isPresent()) {

                    // increase percent
                    fraction = fractionOptional.get();
                    BigDecimal fractionPercent = BigDecimal.valueOf(fraction.getPercent());
                    fractionPercent = fractionPercent.add(requestPercent);
                    fraction.setPercent(fractionPercent.doubleValue());

                    // add another stock to fraction
                    Set<Stock> fractionStocks = fraction.getStocks();
                    /**
                     *
                     */
                    //stockFromDb.setVersion(null);
                    fractionStocks.add(stockFromDb);
                    fraction.setStocks(fractionStocks);
                    repoFractions.save(fraction);



                }

                // user haven't bought the stock yet, so we create a new fraction
                else {

                    // create new fraction
                    fraction = new Fraction(user_id, requestPercent.doubleValue());
                    Set<Stock> stocks = new HashSet<>();
                    stocks.add(stockFromDb);

                    fraction.setStocks(stocks);
                    repoFractions.save(fraction);

                }

                // get the fractionId used to create record of the transaction
                createdFractionId = fraction.getId();
                requestPercent = new BigDecimal(0);

            }
        }

        // update user balance in accounting-service
        // updateBalance automatic registers transaction
        Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        accountingService.updateBalanceAndRegisterTransaction(user_id, createdFractionId, TransactionType.PURCHASE, amountAfterProvision.doubleValue(), timestamp);

        return "investment accepted";
    }



    public Double getCashGainOfSingleFraction(Long fraction_id){

        Optional<Fraction> optionalFraction = repoFractions.findById(fraction_id);
        if(!optionalFraction.isPresent()){
            return null;
        }
        Fraction fraction = optionalFraction.get();
        BigDecimal fractionPercent = BigDecimal.valueOf(fraction.getPercent());
        String symbol = repoFractions.findStockSymbolOfFraction(fraction_id);
        BigDecimal currentPrice = BigDecimal.valueOf(yahooFinanceService.getCurrentPriceOfSingleStock(symbol)).setScale(2, RoundingMode.FLOOR);

        BigDecimal gain = fractionPercent.multiply(currentPrice).divide(new BigDecimal(100)).setScale(2, RoundingMode.FLOOR);

        return gain.doubleValue();
    }

    public double getCashGainOfAllFractions(Long user_id){

        BigDecimal overAllGain = new BigDecimal(0);
        ArrayList<Fraction> allUserFractions = (ArrayList<Fraction>) repoFractions.findAllByUserId(user_id);
        for (Fraction f: allUserFractions
             ) {
            BigDecimal fractionPercent = BigDecimal.valueOf(f.getPercent());
            String symbol = repoFractions.findStockSymbolOfFraction(f.getId());
            BigDecimal currentPrice = BigDecimal.valueOf(yahooFinanceService.getCurrentPriceOfSingleStock(symbol)).setScale(2, RoundingMode.FLOOR);
            BigDecimal gain = fractionPercent.multiply(currentPrice).divide(new BigDecimal(100)).setScale(2, RoundingMode.FLOOR);
            overAllGain = overAllGain.add(gain);

        }

        return overAllGain.doubleValue();
    }

    public Double getFractionPercent(Long fraction_id) {

        Double percent = null;


        percent = repoFractions.findFractionPercentById(fraction_id);
        if(percent == null){
            return -1.0;
        }
        else{
            return percent;
        }

    }


}
