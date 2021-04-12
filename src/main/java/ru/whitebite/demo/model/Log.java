package ru.whitebite.demo.model;

import ru.whitebite.demo.model.audit.DateAudit;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "LOG")
@Getter
@Setter
public class Log extends DateAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToMany(mappedBy = "logs")
  private List<User> users;
  private BigDecimal amountFrom;
  private BigDecimal amountTo;
  private String currency;
  private String transaction_message;

  public void init_pay(BigDecimal amountFrom, BigDecimal amountTo, String currency) {
    this.amountFrom = amountFrom;
    this.amountTo = amountTo;
    this.currency = currency;
  }

}
