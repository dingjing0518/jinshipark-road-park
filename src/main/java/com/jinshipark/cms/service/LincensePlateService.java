package com.jinshipark.cms.service;

import com.jinshipark.cms.bo.SearchBO;
import com.jinshipark.cms.utils.JinshiparkJSONResult;
import com.jinshipark.cms.utils.PagedGridResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;

public interface LincensePlateService {

    PagedGridResult searchLincensePlate(SearchBO searchBO) throws ParseException;

    JinshiparkJSONResult saveLincensePlateForRoad(String carNum, String parkId, String httpPath, String userName);

    JinshiparkJSONResult getLincensePlateForRoad(String carNum, String parkId, String httpPath);

    JinshiparkJSONResult getLincensePlateForRoad(String carNum, String parkId);

    JinshiparkJSONResult departure(String parkId, String carNum, String dateOften, Integer payNum, Integer realNum);
}
