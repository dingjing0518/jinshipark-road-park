package com.jinshipark.cms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jinshipark.cms.bo.SearchBO;
import com.jinshipark.cms.mapper.*;
import com.jinshipark.cms.model.*;
import com.jinshipark.cms.service.LincensePlateService;
import com.jinshipark.cms.utils.JinshiparkJSONResult;
import com.jinshipark.cms.utils.KeyUtils;
import com.jinshipark.cms.utils.PagedGridResult;
import com.jinshipark.cms.utils.PayUtils;
import com.jinshipark.cms.vo.LincensePlateVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LincensePlateServiceImpl implements LincensePlateService {
    public static final Logger logger = LoggerFactory.getLogger(LincensePlateServiceImpl.class);
    @Autowired
    private LincensePlateMapper lincensePlateMapper;
    @Autowired
    private JinshiParkSettingMapper jinshiParkSettingMapper;
    @Autowired
    private LincensePlateHistoryMapper lincensePlateHistoryMapper;
    @Autowired
    private JinshiparkInrecordRoadMapper jinshiparkInrecordRoadMapper;
    @Autowired
    private JinshiparkOutrecordRoadMapper jinshiparkOutrecordRoadMapper;

    @Override
    public PagedGridResult searchLincensePlate(SearchBO searchBO) throws ParseException {
        List<LincensePlateVO> lincensePlateVOS = new ArrayList<>();
        LincensePlateExample example = new LincensePlateExample();
        LincensePlateExample.Criteria criteria = example.createCriteria();
        example.setOrderByClause("lp_create_time desc");
        criteria.andLpParkingNameEqualTo(searchBO.getParkId());
        criteria.andLpCreateTimeGreaterThan(searchBO.getDateForStartTime());
        criteria.andLpCreateTimeLessThanOrEqualTo(searchBO.getDateForEndTime());
        criteria.andLpLincensePlateIdCarLike("%" + searchBO.getKeyWord() + "%");
        if (StringUtils.isNotBlank(searchBO.getLpOrderState())) {
            criteria.andLpOrderStateEqualTo(searchBO.getLpOrderState());
        }
        // ??????
        PageHelper.startPage(searchBO.getPageNum(), searchBO.getPageSize());
        List<LincensePlate> lincensePlates = lincensePlateMapper.selectByExample(example);
        for (LincensePlate lincensePlate : lincensePlates) {
            LincensePlateVO lincensePlateVO = new LincensePlateVO();
            BeanUtils.copyProperties(lincensePlate, lincensePlateVO);
            lincensePlateVOS.add(lincensePlateVO);
        }
        PageInfo<?> pageList = new PageInfo<>(lincensePlates);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(searchBO.getPageNum());
        grid.setRows(lincensePlateVOS);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }

    @Override
    public JinshiparkJSONResult saveLincensePlateForRoad(String carNum, String parkId, String httpPath, String userName) {
        //????????????????????????????????????????????????
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        if (jinshiparkInrecordRoadList.size() > 0) {
            JinshiparkOutrecordRoad jinshiparkOutrecordRoad = new JinshiparkOutrecordRoad();
            BeanUtils.copyProperties(jinshiparkInrecordRoadList.get(0), jinshiparkOutrecordRoad);
            jinshiparkOutrecordRoad.setLpId(null);
            int count = jinshiparkOutrecordRoadMapper.insertSelective(jinshiparkOutrecordRoad);
            if (count > 0) {
                jinshiparkInrecordRoadMapper.deleteByExample(example);
            }
        }
        String orderId = KeyUtils.getOrderIdByPlate(carNum, parkId);
        JinshiparkInrecordRoad jinshiparkInrecordRoad = new JinshiparkInrecordRoad();
        jinshiparkInrecordRoad.setLpLincensePlateIdCar(carNum);//?????????
        jinshiparkInrecordRoad.setLpServiceTypeCar("");
        jinshiparkInrecordRoad.setLpInboundTime(new Date());//????????????
        jinshiparkInrecordRoad.setLpCreateTime(new Date());//????????????
        jinshiparkInrecordRoad.setLpOrderId(orderId);//??????Id
        jinshiparkInrecordRoad.setLpLincenseType("0");//???????????? ??????0
//        lincensePlate.setLpCarType();//????????????
//        lincensePlate.setLpInboundCname();//????????????
        jinshiparkInrecordRoad.setLpParkingName(parkId);//????????????
        jinshiparkInrecordRoad.setLpAgentName(parkId.substring(0, 6));//????????????
        jinshiparkInrecordRoad.setLpOrderState("?????????");//????????????
        jinshiparkInrecordRoad.setLpLgId(0);//?????????id ??????0
        jinshiparkInrecordRoad.setLpLgType(0);//?????????????????? ??????0
        jinshiparkInrecordRoad.setInpicpath(httpPath);
        jinshiparkInrecordRoad.setLoginname(userName);
        int result = jinshiparkInrecordRoadMapper.insertSelective(jinshiparkInrecordRoad);
        if (result > 0) {
            return JinshiparkJSONResult.ok("????????????");
        }
        return JinshiparkJSONResult.errorMsg("????????????");
    }

    @Override
    public JinshiparkJSONResult getLincensePlateForRoad(String carNum, String parkId, String httpPath) {
        //?????????????????????????????????
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        //?????????????????????
        if (jinshiparkInrecordRoadList.size() == 0) {
            return JinshiparkJSONResult.errorMsg("??????????????????");
        }

        JinshiparkInrecordRoad jinshiparkInrecordRoad = jinshiparkInrecordRoadList.get(0);
        JinshiparkInrecordRoad inrecordRoad = new JinshiparkInrecordRoad();
        //???????????????????????????
        logger.error("jinshiparkInrecordRoad.getLpParkingName():{}", jinshiparkInrecordRoad.getLpParkingName());
        if (!jinshiparkInrecordRoad.getLpParkingName().equals(parkId)) {
            return JinshiparkJSONResult.errorMsg("???????????????????????????");
        }

        BeanUtils.copyProperties(jinshiparkInrecordRoad, inrecordRoad);
        Integer dateOften = PayUtils.getDateOften(new Date(), inrecordRoad.getLpInboundTime());
        String lpParkingName = inrecordRoad.getLpParkingName();

        String lpLincenseTypeStr = inrecordRoad.getLpLincenseType();
        Integer lpLincenseType = null;
        if (lpLincenseTypeStr == null || lpLincenseTypeStr.equals("")) {
            lpLincenseType = 0;
        } else {
            lpLincenseType = Integer.valueOf(lpLincenseTypeStr);
        }
        JinshiParkSettingExample jinshiParkSettingExample = new JinshiParkSettingExample();
        JinshiParkSettingExample.Criteria settingExampleCriteria = jinshiParkSettingExample.createCriteria();
        settingExampleCriteria.andJpsParkIdEqualTo(lpParkingName);
        settingExampleCriteria.andJpsCarTypeEqualTo(lpLincenseType);
        List<JinshiParkSetting> jinshiParkSettings = jinshiParkSettingMapper.selectByExample(jinshiParkSettingExample);
        //????????????????????????????????????
        JinshiparkInrecordRoad update = new JinshiparkInrecordRoad();
        update.setOutpicpath(httpPath);
        jinshiparkInrecordRoadMapper.updateByExampleSelective(update, example);
        //??????????????????
        if (dateOften <= jinshiParkSettings.get(0).getJpsFreeTime() || jinshiparkInrecordRoad.getLpLgType() == 1) {
            inrecordRoad.setLpDepartureTime(new Date());
            inrecordRoad.setLpParkingOften(String.valueOf(dateOften));
            inrecordRoad.setLpParkingCost("0");
            return JinshiparkJSONResult.ok(inrecordRoad);
        }
        Date tempDate = new Date();
        Double rent = PayUtils.getRent(tempDate, inrecordRoad.getLpInboundTime(), jinshiParkSettings.get(0));
        inrecordRoad.setLpDepartureTime(new Date());
        inrecordRoad.setLpParkingOften(String.valueOf(dateOften));
        inrecordRoad.setLpParkingCost(new DecimalFormat("0").format(rent));
        return JinshiparkJSONResult.ok(inrecordRoad);
    }

    @Override
    public JinshiparkJSONResult getLincensePlateForRoad(String carNum, String parkId) {
        //?????????????????????????????????
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        //?????????????????????
        if (jinshiparkInrecordRoadList.size() == 0) {
            return JinshiparkJSONResult.errorMsg("??????????????????");
        }

        JinshiparkInrecordRoad jinshiparkInrecordRoad = jinshiparkInrecordRoadList.get(0);
        JinshiparkInrecordRoad inrecordRoad = new JinshiparkInrecordRoad();
        //???????????????????????????
        logger.error("lincensePlate.getLpParkingName():{}", jinshiparkInrecordRoad.getLpParkingName());
        if (!jinshiparkInrecordRoad.getLpParkingName().equals(parkId)) {
            return JinshiparkJSONResult.errorMsg("???????????????????????????");
        }
        BeanUtils.copyProperties(jinshiparkInrecordRoad, inrecordRoad);
        Integer dateOften = PayUtils.getDateOften(new Date(), inrecordRoad.getLpInboundTime());
        String lpParkingName = inrecordRoad.getLpParkingName();

        String lpLincenseTypeStr = inrecordRoad.getLpLincenseType();
        Integer lpLincenseType = null;
        if (lpLincenseTypeStr == null || lpLincenseTypeStr.equals("")) {
            lpLincenseType = 0;
        } else {
            lpLincenseType = Integer.valueOf(lpLincenseTypeStr);
        }
        JinshiParkSettingExample jinshiParkSettingExample = new JinshiParkSettingExample();
        JinshiParkSettingExample.Criteria settingExampleCriteria = jinshiParkSettingExample.createCriteria();
        settingExampleCriteria.andJpsParkIdEqualTo(lpParkingName);
        settingExampleCriteria.andJpsCarTypeEqualTo(lpLincenseType);
        List<JinshiParkSetting> jinshiParkSettings = jinshiParkSettingMapper.selectByExample(jinshiParkSettingExample);

        //??????????????????
        if (dateOften <= jinshiParkSettings.get(0).getJpsFreeTime()) {
            inrecordRoad.setLpDepartureTime(new Date());
            inrecordRoad.setLpParkingOften(String.valueOf(dateOften));
            inrecordRoad.setLpParkingCost("0");
            return JinshiparkJSONResult.ok(inrecordRoad);
        }
        Date tempDate = new Date();
        Double rent = PayUtils.getRent(tempDate, inrecordRoad.getLpInboundTime(), jinshiParkSettings.get(0));
        inrecordRoad.setLpDepartureTime(new Date());
        inrecordRoad.setLpParkingOften(String.valueOf(dateOften));
        inrecordRoad.setLpParkingCost(new DecimalFormat("0").format(rent));
        return JinshiparkJSONResult.ok(inrecordRoad);
    }

    @Override
    public JinshiparkJSONResult departure(String parkId, String carNum, String dateOften, Integer payNum, Integer realNum) {
        //?????????????????????????????????
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        //?????????????????????
        if (jinshiparkInrecordRoadList.size() == 0) {
            return JinshiparkJSONResult.errorMsg("??????????????????");
        }

        JinshiparkInrecordRoad jinshiparkInrecordRoad = jinshiparkInrecordRoadList.get(0);
        //???????????????????????????
        logger.error("lincensePlate.getLpParkingName():{}", jinshiparkInrecordRoad.getLpParkingName());
        if (!jinshiparkInrecordRoad.getLpParkingName().equals(parkId)) {
            return JinshiparkJSONResult.errorMsg("???????????????????????????");
        }
        JinshiparkInrecordRoad inrecordRoad = new JinshiparkInrecordRoad();
        inrecordRoad.setLpDepartureTime(new Date());//????????????
        if (payNum == 0) {
            inrecordRoad.setLpPaymentType("?????????????????????");//????????????
        } else {
            inrecordRoad.setLpOrderState("????????????");//????????????
            inrecordRoad.setLpPaymentType("??????????????????");
        }
        inrecordRoad.setLpParkingRealCost(String.valueOf(realNum));//????????????
        inrecordRoad.setLpParkingCost(String.valueOf(payNum));
        inrecordRoad.setLpParkingOften(dateOften);
        int result = jinshiparkInrecordRoadMapper.updateByExampleSelective(inrecordRoad, example);
        if (result > 0) {
            List<JinshiparkInrecordRoad> inrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
            JinshiparkInrecordRoad inrecord = inrecordRoadList.get(0);
            //3.???????????????????????????????????????????????????
            JinshiparkOutrecordRoad jinshiparkOutrecordRoad = new JinshiparkOutrecordRoad();
            BeanUtils.copyProperties(inrecord, jinshiparkOutrecordRoad);
            jinshiparkOutrecordRoad.setLpId(null);
            int count = jinshiparkOutrecordRoadMapper.insertSelective(jinshiparkOutrecordRoad);
            if (count > 0) {
                jinshiparkInrecordRoadMapper.deleteByExample(example);
                return JinshiparkJSONResult.ok("????????????");
            }
        }
        return JinshiparkJSONResult.errorMsg("????????????");
    }
}
