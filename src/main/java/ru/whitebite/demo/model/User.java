package ru.whitebite.demo.model;

import ru.whitebite.demo.model.audit.DateAudit;
import ru.whitebite.demo.validation.annotation.NullOrNotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "USER")
@Getter
@Setter
@ToString
@AllArgsConstructor
public class User extends DateAudit {

  @Id
  @Column(name = "USER_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(name = "USERNAME", unique = true)
  @NullOrNotBlank(message = "Username can not be blank")
  private String username;

  @Column(name = "PASSWORD")
  @NotNull(message = "Password cannot be null")
  private String password;

  @Column(name = "IS_ACTIVE", nullable = false)
  private Boolean active;

  @Basic
  @Column
  private BigDecimal amount;

  @Basic
  @Column
  private String currency;

  @Fetch(FetchMode.JOIN)
  @ManyToMany( cascade = {CascadeType.MERGE,CascadeType.PERSIST})
  @JoinTable(name = "user_log",
      joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "log_id"))
  private List<Log> logs = new ArrayList<>();

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "USER_AUTHORITY", joinColumns = {
      @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")}, inverseJoinColumns = {
      @JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID")})
  private Set<Role> roles = new HashSet<>();

  public User() {

    super();
    currency = "USD";
    amount = new BigDecimal("8.0");
  }

  public User(User user) {
    id = user.getId();
    username = user.getUsername();
    password = user.getPassword();
    active = user.getActive();
    roles = user.getRoles();

  }

  public void addRole(Role role) {
    roles.add(role);
    role.getUserList().add(this);
  }

  public void addRoles(Set<Role> roles) {
    roles.forEach(this::addRole);
  }


  public Money getMoney() {
    return Money.of(CurrencyUnit.of(currency), amount);
  }

  public void setMoney(Money money) {
    this.amount = money.getAmount();
    this.currency = money.getCurrencyUnit().getCode();
  }

}
