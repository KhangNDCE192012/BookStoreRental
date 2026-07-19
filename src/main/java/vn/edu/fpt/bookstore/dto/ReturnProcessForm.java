package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import vn.edu.fpt.bookstore.entity.enums.ReturnCondition;

import java.math.BigDecimal;

public class ReturnProcessForm {
    @NotNull
    private ReturnCondition conditionAfter;
    @DecimalMin("0.00")
    private BigDecimal damageFee = BigDecimal.ZERO;
    private String notes;

    public ReturnCondition getConditionAfter() { return conditionAfter; }
    public void setConditionAfter(ReturnCondition conditionAfter) { this.conditionAfter = conditionAfter; }
    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
