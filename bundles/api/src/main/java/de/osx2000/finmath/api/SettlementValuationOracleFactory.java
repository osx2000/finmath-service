package de.osx2000.finmath.api;

import net.finmath.smartcontract.contract.SmartDerivativeContractMargining;

public interface SettlementValuationOracleFactory {
    SmartDerivativeContractMargining getSettlementValuationOracle(String contactUID);
}
