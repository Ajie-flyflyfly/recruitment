package com.iurac.recruit.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iurac.recruit.entity.*;
import com.iurac.recruit.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * 这段代码是一个Spring组件，实现了ServletContextAware接口。
 * 这允许它在应用启动时获取ServletContext，并使用它来存储从不同服务中获取的数据列表。
 * 这些列表随后通过ServletContext属性在整个Web应用程序中提供给其他组件。
 * 这个类在应用启动时很好地将数据预加载到了ServletContext中，使其在整个应用程序中可用。
 * */
@Component
public class SystemInitLoader implements ServletContextAware {

    @Autowired
    DicTypeService dicTypeService;
    @Autowired
    DicValueService dicValueService;
    @Autowired
    ProvinceService provinceService;
    @Autowired
    CityService cityService;
    @Autowired
    TypeService typeService;
    @Autowired
    BusinessService businessService;

    //当应用启动时，Spring会调用此方法。
    //该方法通过调用三个私有方法来初始化ServletContext
    @Override
    public void setServletContext(ServletContext servletContext) {
        initProvinceCity(servletContext);
        initBusinessType(servletContext);
        initDictionary(servletContext);
    }

    //岗位类型Type
    //这个方法首先从typeService中获取所有类型的列表，然后将这个列表存储到ServletContext中，键为"typeList"。
    // 然后，对于每一个类型，它都会从businessService中获取这个类型的所有业务的列表，
    // 然后将这个列表存储到ServletContext中，键为"businessInType"+type.getId()

    // 初始化省份和城市数据
    private void initBusinessType(ServletContext servletContext) {
        // 从provinceService获取所有省份的列表
        List<Type> typeList = typeService.list();
        // 将省份列表存储到ServletContext中，键为"provinceList"
        servletContext.setAttribute("typeList",typeList);
        // 对于每一个省份
        typeList.forEach(type -> {
            // 创建一个查询条件，用于查询这个省份的所有城市
            QueryWrapper<Business> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type_id",type.getId());
            // 从cityService获取这个省份的所有城市的列表
            List<Business> businessList = businessService.list(queryWrapper);
            // 将城市列表存储到ServletContext中，键为"cityInProvince"+province.getId()
            servletContext.setAttribute("businessInType"+type.getId(),businessList);
        });
    }

    //这个方法首先从provinceService中获取所有省份的列表，然后将这个列表存储到ServletContext中，键为"provinceList"。
    // 然后，对于每一个省份，它都会从cityService中获取这个省份的所有城市的列表，
    // 然后将这个列表存储到ServletContext中，键为"cityInProvince"+province.getId()。
    private void initProvinceCity(ServletContext servletContext) {
        List<Province> provinceList = provinceService.list();
        servletContext.setAttribute("provinceList",provinceList);
        provinceList.forEach(province -> {
            QueryWrapper<City> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("province_id",province.getId()).orderByAsc("city_no");
            List<City> cityList = cityService.list(queryWrapper);
            servletContext.setAttribute("cityInProvince"+province.getId(),cityList);
        });
    }

    //这个方法从dicTypeService中获取所有字典类型的列表，
    // 然后对于每一个字典类型，它都会从dicValueService中获取这个类型的所有字典值的列表，
    // 然后将这个列表存储到ServletContext中，键为type.getType()+"List"。
    private void initDictionary(ServletContext servletContext) {
        List<DicType> dicTypeList = dicTypeService.list();
        dicTypeList.forEach(type -> {
            QueryWrapper<DicValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type_id",type.getId()).orderByAsc("order_no");
            List<DicValue> dicValueList = dicValueService.list(queryWrapper);
            servletContext.setAttribute(type.getType()+"List",dicValueList);
        });
    }
    //这样，当应用启动时，ServletContext就会被初始化，包含了所有省份、城市、类型、业务和字典值的列表。
    // 这些数据可以在整个应用中被访问和使用，
    // 例如在Thymeleaf模板中通过${application.provinceList}这样的表达式来访问省份列表。
}
