
package ru.whitebite.demo.service;

import ru.whitebite.demo.exception.BankTransactionException;
import ru.whitebite.demo.model.Log;
import ru.whitebite.demo.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.joda.money.Money;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static ru.whitebite.demo.util.Constants.CURRENCY_UNIT;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final UserService userService;
  private static final Logger logger = Logger.getLogger(PaymentService.class);

  // MANDATORY: Transaction must be created before.
  @Transactional(propagation = Propagation.MANDATORY)
  public void minusAmount(Long id, Money amount)
      throws NotFoundException, BankTransactionException {

    User user = userService.findById(id).orElseThrow(NotFoundException::new);
    Money balance = user.getMoney();
    if (!balance.isGreaterThan(amount)) {
      throw new BankTransactionException(
          "The money in the account '" + id + "' is not enough (" + balance.getAmount() + " "
              + balance.getCurrencyUnit().getCode() + ")");
    }
    user.setMoney(balance.minus(amount));
    Log log = new Log();
    log.init_pay(balance.getAmount(),user.getAmount(),balance.getCurrencyUnit().getCode());
    user.getLogs().add(log);
  }


  // Do not catch BankTransactionException in this method.
  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = BankTransactionException.class)
  public void withdrawMoney(Long fromAccountId, double amount)
      throws NotFoundException, BankTransactionException {
    minusAmount(fromAccountId, Money.of(CURRENCY_UNIT, amount));
  }


}
