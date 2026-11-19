package com.demo.service;

import java.math.BigDecimal;

public interface AccountService {
    public void trade(int from, int to, BigDecimal money);
}
