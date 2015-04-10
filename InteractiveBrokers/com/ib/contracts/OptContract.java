/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.contracts;

import com.ib.client.Types.SecType;
import com.onenow.execution.Contract;

public class OptContract extends Contract {
    public OptContract(String symbol, String expiry, double strike, String right) {
        this(symbol, "SMART", expiry, strike, right);
    }

    public OptContract(String symbol, String exchange, String expiry, double strike, String right) {
        symbol(symbol);
        secType(SecType.OPT.name());
        exchange(exchange);
        currency("USD");
        expiry(expiry);
        strike(strike);
        right(right);
    }
}
