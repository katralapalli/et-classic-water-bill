package com.et.etclassic.dto;

public class UpdateTenantRequest {
    private String tenantName;
    private String tenantPhone;

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }
}
