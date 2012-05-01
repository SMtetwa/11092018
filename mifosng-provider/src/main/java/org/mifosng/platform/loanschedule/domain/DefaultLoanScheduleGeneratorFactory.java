package org.mifosng.platform.loanschedule.domain;

import org.mifosng.platform.loan.domain.InterestMethod;

public class DefaultLoanScheduleGeneratorFactory implements
		LoanScheduleGeneratorFactory {

	@Override
	public LoanScheduleGenerator create(final InterestMethod interestMethod) {

		LoanScheduleGenerator loanScheduleGenerator = null;

		switch (interestMethod) {
		case FLAT:
			loanScheduleGenerator = new FlatLoanScheduleGenerator();
			break;
		case DECLINING_BALANCE:
			loanScheduleGenerator = new DecliningBalanceMethodLoanScheduleGenerator();
			break;
		case INVALID:
			break;
		}
		
		return loanScheduleGenerator;
	}
}