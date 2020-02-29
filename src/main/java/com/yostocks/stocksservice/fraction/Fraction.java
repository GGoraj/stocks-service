package com.yostocks.stocksservice.fraction;

import com.yostocks.stocksservice.stock.Stock;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OptimisticLock;
import org.springframework.context.annotation.Bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Fraction is a fraction of a Stock.
 * User chooses to buy a chosen Fraction
 * And This entity refers to the chosen fraction.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fractions")
public class Fraction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long user_id;

    @Column(name = "percent")
    private double percent;

    @Version
    @Column(name = "version")
    private long version;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "fractions_stocks",
            joinColumns = @JoinColumn(name = "fraction_id", insertable = true, updatable = true),
            inverseJoinColumns = @JoinColumn(name = "stock_id", insertable = true, updatable = true))

    Set<Stock> stocks = new HashSet<>();


    // constructor
    public Fraction(Long user_id, double percent) {

        this.user_id = user_id;
        this.percent = percent;
    }


}