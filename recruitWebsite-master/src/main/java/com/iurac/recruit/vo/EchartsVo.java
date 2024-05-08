package com.iurac.recruit.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class EchartsVo {
    private int total;
    private List<Map<String ,Object>> dataList;

//    public int getTotal() {
//        return total;
//    }
//
//    public void setTotal(int total) {
//        this.total = total;
//    }
//
//    public List<Map<String, Object>> getDataList() {
//        return dataList;
//    }
//
//    public void setDataList(List<Map<String, Object>> dataList) {
//        this.dataList = dataList;
//    }
}
