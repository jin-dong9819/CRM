package com.demo.test;

import com.demo.mapper.AccountMapper;
import com.demo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 正常转账：开关 crm.trade.demo-fail 默认 false，走完整的两条 update，转账成功。
 * <p>
 * 方法上的 @Transactional 是"测试用"的：让整个测试跑在一个会回滚的事务里，
 * 断言做完后数据自动还原，不会污染数据库，可以反复执行。
 */
@SpringBootTest
public class AccountTest {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    @Test
    @Transactional
    public void test1() {
        BigDecimal money = new BigDecimal("20");
        BigDecimal beforeFrom = accountMapper.selectById(1).getBalance();
        BigDecimal beforeTo = accountMapper.selectById(2).getBalance();

        accountService.trade(1, 2, money);

        assertEquals(0, beforeFrom.subtract(money).compareTo(accountMapper.selectById(1).getBalance()),
                "转出账户应减少 " + money);
        assertEquals(0, beforeTo.add(money).compareTo(accountMapper.selectById(2).getBalance()),
                "转入账户应增加 " + money);
    }
}
