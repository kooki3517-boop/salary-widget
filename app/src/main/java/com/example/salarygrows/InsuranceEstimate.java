package com.example.salarygrows;

/** Illustrative employee-side payroll deductions. Actual premiums depend on eligibility,
 * official contribution base, caps, exemptions, and annual rate changes. */
public final class InsuranceEstimate {
    private InsuranceEstimate() {}
    // 2026 employee shares; editable in the UI for future changes.
    public static final double PENSION = 0.0475;
    public static final double HEALTH = 0.03595;
    public static final double LONG_TERM_CARE_ON_HEALTH = 0.1314;
    public static final double EMPLOYMENT = 0.009;
    public static long round10(double value) { return Math.round(value / 10.0) * 10L; }
    public static long[] calculate(long gross, boolean pension, boolean health, boolean employment) {
        long p = pension ? round10(gross * PENSION) : 0;
        long h = health ? round10(gross * HEALTH) : 0;
        long care = health ? round10(h * LONG_TERM_CARE_ON_HEALTH) : 0;
        long e = employment ? round10(gross * EMPLOYMENT) : 0;
        return new long[]{p,h,care,e};
    }
}
