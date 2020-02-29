package com.yostocks.stocksservice.fraction;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface IFractionRepository extends CrudRepository<Fraction, Long> {

    // which expects 'User' property in the project
    @Query(value = "SELECT * from Fractions f where f.user_id = ?1", nativeQuery = true)
    Collection<Fraction> findAllByUserId(Long user_id);

    @Query(value = "select f.* from fractions f inner join fractions_stocks fs on f.user_id = :user_id and fs.stock_id = (select s.id from stocks s where s.symbol = :symbol LIMIT 1) limit 1", nativeQuery = true)
    //@Query(value = "select f.* from fractions f where f.id = (select fs.fraction_id from stocks s inner join fractions_stocks fs on s.id = fs.stock_id inner join fractions f on f.user_id = :user_id limit 1)", nativeQuery = true)
    Fraction findFractionByStockSymbolAndUserId(Long user_id, String symbol);


    @Query(value = "select f.* from fractions f inner join fractions_stocks fs on f.user_id = :user_id and fs.stock_id = (select s.id from stocks s where s.symbol = :symbol and s.percent_left < 100 LIMIT 1) limit 1", nativeQuery = true)
        //@Query(value = "select f.* from fractions f where f.id = (select fs.fraction_id from stocks s inner join fractions_stocks fs on s.id = fs.stock_id inner join fractions f on f.user_id = :user_id limit 1)", nativeQuery = true)
    Fraction findFractionByStockSymbolAndUserIAndStockPercentBelowHundered(Long user_id, String symbol);

    @Query(value = "select s.symbol from stocks s where s.id = (select fs.stock_id from fractions_stocks fs where fs.fraction_id = :fraction_id limit 1);", nativeQuery = true)
    String findStockSymbolOfFraction(Long fraction_id);


    @Query(value = "select f.percent from fractions f where f.id = :fraction_id", nativeQuery = true)
    Double findFractionPercentById(Long fraction_id);



}