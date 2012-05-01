package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public class PeriodicInterestRateCalculator {

	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	
	public BigDecimal calculateFrom(LoanProductRelatedDetail loanScheduleInfo) {
		
		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		
		Integer paymentPeriodsInOneYear = this.paymentPeriodsInOneYearCalculator.calculate(loanScheduleInfo.getRepaymentPeriodFrequencyType());
		
		BigDecimal divisor = BigDecimal.valueOf(paymentPeriodsInOneYear * 100);
		BigDecimal numberOfPeriods = BigDecimal.valueOf(loanScheduleInfo.getRepayEvery());
		
		return loanScheduleInfo.getAnnualNominalInterestRate().divide(divisor, mc).multiply(numberOfPeriods);
	}

	public Money calculateInterestOn(Money outstandingBalance, BigDecimal periodInterestRateForRepaymentPeriod, 
			int daysInPeriod, 
			LoanProductRelatedDetail loanScheduleInfo) {

		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		Money interestDue = Money.zero(outstandingBalance.getCurrency());
		
		switch (loanScheduleInfo.getInterestCalculationPeriodMethod()) {
		case DAILY:
			BigDecimal dailyInterestRate = loanScheduleInfo.getAnnualNominalInterestRate()
			.divide(BigDecimal.valueOf(Long.valueOf(365)), mc)
			.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
			.multiply(BigDecimal.valueOf(loanScheduleInfo.getRepayEvery()));
	
			BigDecimal equivalentInterestRateForPeriod = dailyInterestRate.multiply(BigDecimal.valueOf(Long.valueOf(daysInPeriod)));
			
			interestDue = outstandingBalance.multiplyRetainScale(equivalentInterestRateForPeriod, RoundingMode.HALF_EVEN);
			break;
		default:
			interestDue = outstandingBalance.multiplyRetainScale(periodInterestRateForRepaymentPeriod, RoundingMode.HALF_EVEN);
			break;
		}
		
		return interestDue;
	}

	public Money calculatePrincipalOn(Money totalDuePerInstallment, Money interestForInstallment, LoanProductRelatedDetail loanScheduleInfo) {
		
		Money principalDue = Money.zero(totalDuePerInstallment.getCurrency());
		
		switch (loanScheduleInfo.getAmortizationMethod()) {
		case EQUAL_PRINCIPAL:
			principalDue = loanScheduleInfo.getPrincipal().dividedBy(loanScheduleInfo.getNumberOfRepayments(), RoundingMode.HALF_EVEN);
			break;
		case EQUAL_INSTALLMENTS:
			principalDue = totalDuePerInstallment.minus(interestForInstallment);
			break;
		}
		
		return principalDue;
	}
}