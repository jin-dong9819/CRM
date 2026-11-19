package com.demo.test;

import com.demo.mapper.AccountMapper;
import com.demo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 事务回滚演示：通过 properties 打开 crm.trade.demo-fail 开关，
 * AccountServiceImpl#trade 在扣款 update 之后抛 ArithmeticException，
 * 观察两边余额是否都回到转账前的状态。
 * <p>
 * 注意：预期内的异常必须用 assertThrows 接住，否则会冒出测试方法，Maven 判定为 Error。
 * 这里也不能给测试方法加 @Transactional —— 那样会和服务的事务合并成一个，
 * 异常只会把大事务标记为 rollback-only，断言时读到的仍是未回滚的中间值。
 */
@SpringBootTest(properties = "crm.trade.demo-fail=true")
public class AccountRollbackTest {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    @Test
    public void test1() {
        BigDecimal beforeFrom = accountMapper.selectById(1).getBalance();
        BigDecimal beforeTo = accountMapper.selectById(2).getBalance();

        assertThrows(ArithmeticException.class,
                () -> accountService.trade(1, 2, new BigDecimal("20")));

        // 回滚验证：两边余额都必须保持原样
        assertEquals(0, beforeFrom.compareTo(accountMapper.selectById(1).getBalance()),
                "转出账户余额应已回滚");
        assertEquals(0, beforeTo.compareTo(accountMapper.selectById(2).getBalance()),
                "转入账户余额应已回滚");
    }
}
