package com.gupaoedu.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/11 14:51
 **/
public class Shop  implements Serializable {
    String shopId;
    String shopName;
    String orgCode;
    List<String> businScope;

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public List<String> getBusinScope() {
        return businScope;
    }

    public void setBusinScope(List<String> businScope) {
        this.businScope = businScope;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "shopId='" + shopId + '\'' +
                ", shopName='" + shopName + '\'' +
                ", orgCode='" + orgCode + '\'' +
                ", businScope=" + businScope +
                '}';
    }
}
