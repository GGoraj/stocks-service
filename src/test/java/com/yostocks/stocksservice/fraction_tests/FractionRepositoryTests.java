package com.yostocks.stocksservice.fraction_tests;

import com.yostocks.stocksservice.fraction.Fraction;
import com.yostocks.stocksservice.fraction.IFractionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FractionRepositoryTests {

    @Autowired
    IFractionRepository repo;

    @Test
    public void buyFraction(){
        Fraction fraction = new Fraction(1L,366);
        Fraction response = repo.save(fraction);
        assert (!response.equals(null));
    }

    @Test
    void findAllFractionsByUserId(){
      /*  // assume user id = 1
        ArrayList<Fraction> fractions = (ArrayList<Fraction>) repo.findAllByStockId(1L);
        assertTrue(!fractions.isEmpty());*/
    }

    @Test
    void findFractionsByStockIdAndUserId(){
       /* ArrayList<Fraction> fractions = (ArrayList<Fraction>) repo.findAllByStockIdAndUserId(1L,1L);
        assertTrue(!fractions.isEmpty());*/
    }

}
