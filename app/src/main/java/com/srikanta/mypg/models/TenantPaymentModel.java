package com.srikanta.mypg.models;

import com.srikanta.mypg.helpers.revenue.MonthHelper;

public class TenantPaymentModel {

    // Firebase month key (yyyy-MM)
    private String month;

    // Amounts
    private int rentPaid;
    private int depositPaid;

    // Time
    private String paidOn;        // ddMMyyyyHHmm (stored)
    private String paidOnDisplay; // dd MMM yyyy (UI)

    // Required empty constructor
    public TenantPaymentModel() {}

    public TenantPaymentModel(
            String month,
            int rentPaid,
            int depositPaid,
            String paidOn,
            String paidOnDisplay
    ) {
        this.month = month;
        this.rentPaid = rentPaid;
        this.depositPaid = depositPaid;
        this.paidOn = paidOn;
        this.paidOnDisplay = paidOnDisplay;
    }

    // -------- GETTERS --------

    public String getMonth() {
        return month;
    }

    public int getRentPaid() {
        return rentPaid;
    }

    public int getDepositPaid() {
        return depositPaid;
    }

    public String getPaidOn() {
        return paidOn;
    }

    public String getPaidOnDisplay() {
        return paidOnDisplay;
    }

    // -------- HELPERS (VERY USEFUL) --------

    public boolean hasRent() {
        return rentPaid > 0;
    }

    public boolean hasDeposit() {
        return depositPaid > 0;
    }

    public int getTotalPaid() {
        return rentPaid + depositPaid;
    }

    public String getMonthText() {
        return MonthHelper.getMonthText(month);
    }

}
