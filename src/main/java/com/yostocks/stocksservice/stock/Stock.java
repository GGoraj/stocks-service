package com.yostocks.stocksservice.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yostocks.stocksservice.fraction.Fraction;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLock;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.LockModeType.PESSIMISTIC_READ;

/**
 * stocks are displayed in 'Stocks' android's view
 * user chooses one to buy and chooses fraction of it
 */
@Entity
@DynamicUpdate
@Getter
@Setter
@Table(name = "stocks")
//@NamedQuery(name="findBySymbol", query= "select s from stocks s where s.symbol = :symbol limit 1", lockMode=PESSIMISTIC_READ)
public class Stock implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    protected Long id;

    @Column(name = "symbol")
    private String symbol;

    // value expresses how many stocks Yostocks hold. Type is String because of safety of the calculations
    @Column(name = "percent_left")
    private double percent_left;

    @Column(name = "description")
    private String description;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * how to handle pictures ? of brands ?
     */
    /*@Lob
    @Nullable
    @Column(name = "image", columnDefinition="BLOB")
    private byte[] image;*/

    /*@JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stock")
    private List<Fraction> fractions = new ArrayList<>();*/

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "stocks", cascade = { CascadeType.REFRESH })
    Set<Fraction> fractions = new HashSet<>();



    public Stock() {
    }

    public Stock(String symbol, double percent_left, String description) {
        this.symbol = symbol;
        this.percent_left = percent_left;
        this.description = description;
        //this.image = image;
    }

    public Stock(Long id, String symbol, double percent_left, String description) {
        this.id = id;
        this.symbol = symbol;
        this.percent_left = percent_left;
        this.description = description;

    }





    @Override
    public String toString() {
        return "{" +
                    "id" +":" + id +
                    ", symbol" +":" + symbol + '\'' +
                    ", percent_left" +":" + percent_left +
                    ", description" +":" + description +
                '}';
    }
}