package com.demo.service.impl;

import com.demo.entity.Account;
import com.demo.mapper.AccountMapper;
import com.demo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    /**
     * 事务回滚演示开关，默认 false（对应 application.yml 的 crm.trade.demo-fail）。
     * 打开后，扣款 update 会正常执行，随后抛异常回滚，方便观察 @Transactional 的效果。
     * 生产环境必须保持 false，否则所有转账都会失败。
     */
    @Value("${crm.trade.demo-fail:false}")
    private boolean demoFail;

    @Override
    // Exception 是父类，RuntimeException、SQLException 都是子类
    // 默认只有 RuntimeException 及其子类会回滚，所以这里显式指定 rollbackFor = Exception.class
    @Transactional(rollbackFor = Exception.class)
    public void trade(int from, int to, BigDecimal money) {
        if (money == null || money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("转账金额必须大于 0");
        }

        Account account1 = accountMapper.selectById(from);
        Account account2 = accountMapper.selectById(to);
        if (account1 == null || account2 == null) {
            throw new IllegalArgumentException("账户不存在");
        }

        // BigDecimal 比较大小用 compareTo，不能用 == 或 equals（equals 会比较 scale）
        if (account1.getBalance().compareTo(money) < 0) {
            throw new RuntimeException("余额不足");
        }

        account1.setBalance(account1.getBalance().subtract(money));
        account2.setBalance(account2.getBalance().add(money));

        accountMapper.update(account1);

        // 演示事务回滚：仅在开关打开时抛出，此时上面已执行的 update 会被一并回滚。
        // ArithmeticException 属于 RuntimeException，默认规则下就会回滚；
        // 想验证 rollbackFor 的作用，可改成抛受检异常，例如：
        // throw new java.sql.SQLException("模拟数据库异常");
        if (demoFail) {
            throw new ArithmeticException("演示事务回滚：模拟扣款后发生异常");
        }

        accountMapper.update(account2);
    }
}
