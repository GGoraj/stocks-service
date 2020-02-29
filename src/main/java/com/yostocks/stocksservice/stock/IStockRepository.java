package com.yostocks.stocksservice.stock;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.util.Collection;

@Repository
public interface IStockRepository extends CrudRepository<Stock, Long> {


   /* @Query("SELECT f from Fraction f WHERE f.stock_ = ?1 and f.user_id = ?1")
    Fraction findByStockIdAndUserId(int stock_id, int user_id);*/

    @Query(value="select * from stocks s where s.symbol = :symbol and s.percent_left > 0 limit 1;", nativeQuery = true)
    Stock findBySymbolAndPercentGreaterThenZero(String symbol);

    // find ids of stocks of the same symbol by stock_id
    @Query(value="select id from stocks where symbol = (select symbol from stocks where id = :stock_id)", nativeQuery = true)
    Collection<Integer> findIdsOfStocksWithExactSameSymbolByStockId(Long stock_id);


    @Query(value="select * from stocks s where s.symbol = :symbol limit 1", nativeQuery = true)
    Stock findBySymbol(String symbol);

    @Modifying
    @Transactional
    @Query(value = "delete from fractions_stocks fs where fs.stock_id= :stock_id and fs.fraction_id= :fraction_id", nativeQuery = true)
    void deleteFromFractionsStocks(Long fraction_id, Long stock_id);

}