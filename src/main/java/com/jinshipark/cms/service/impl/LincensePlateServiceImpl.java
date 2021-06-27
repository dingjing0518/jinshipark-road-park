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
        // 分页
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
        //判断车牌号是否在道路在场记录表里
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
        jinshiparkInrecordRoad.setLpLincensePlateIdCar(carNum);//车牌号
        jinshiparkInrecordRoad.setLpServiceTypeCar("");
        jinshiparkInrecordRoad.setLpInboundTime(new Date());//入场时间
        jinshiparkInrecordRoad.setLpCreateTime(new Date());//创建时间
        jinshiparkInrecordRoad.setLpOrderId(orderId);//订单Id
        jinshiparkInrecordRoad.setLpLincenseType("0");//车辆类型 默认0
//        lincensePlate.setLpCarType();//区域名称
//        lincensePlate.setLpInboundCname();//入场口名
        jinshiparkInrecordRoad.setLpParkingName(parkId);//停车场名
        jinshiparkInrecordRoad.setLpAgentName(parkId.substring(0, 6));//代理商名
        jinshiparkInrecordRoad.setLpOrderState("未付款");//订单状态
        jinshiparkInrecordRoad.setLpLgId(0);//车牌组id 默认0
        jinshiparkInrecordRoad.setLpLgType(0);//车牌收费类型 默认0
        jinshiparkInrecordRoad.setInpicpath(httpPath);
        jinshiparkInrecordRoad.setLoginname(userName);
        int result = jinshiparkInrecordRoadMapper.insertSelective(jinshiparkInrecordRoad);
        if (result > 0) {
            return JinshiparkJSONResult.ok("入场成功");
        }
        return JinshiparkJSONResult.errorMsg("入场失败");
    }

    @Override
    public JinshiparkJSONResult getLincensePlateForRoad(String carNum, String parkId, String httpPath) {
        //根据车牌查询在场记录表
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        //是否为在场车辆
        if (jinshiparkInrecordRoadList.size() == 0) {
            return JinshiparkJSONResult.errorMsg("未查询到车辆");
        }

        JinshiparkInrecordRoad jinshiparkInrecordRoad = jinshiparkInrecordRoadList.get(0);
        JinshiparkInrecordRoad inrecordRoad = new JinshiparkInrecordRoad();
        //判断是否在该区域内
        logger.error("jinshiparkInrecordRoad.getLpParkingName():{}", jinshiparkInrecordRoad.getLpParkingName());
        if (!jinshiparkInrecordRoad.getLpParkingName().equals(parkId)) {
            return JinshiparkJSONResult.errorMsg("车辆不在该停车场内");
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
        //更新在场记录表的出场照片
        JinshiparkInrecordRoad update = new JinshiparkInrecordRoad();
        update.setOutpicpath(httpPath);
        jinshiparkInrecordRoadMapper.updateByExampleSelective(update, example);
        //免费时间之内
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
        //根据车牌查询在场记录表
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        //是否为在场车辆
        if (jinshiparkInrecordRoadList.size() == 0) {
            return JinshiparkJSONResult.errorMsg("未查询到车辆");
        }

        JinshiparkInrecordRoad jinshiparkInrecordRoad = jinshiparkInrecordRoadList.get(0);
        JinshiparkInrecordRoad inrecordRoad = new JinshiparkInrecordRoad();
        //判断是否在该区域内
        logger.error("lincensePlate.getLpParkingName():{}", jinshiparkInrecordRoad.getLpParkingName());
        if (!jinshiparkInrecordRoad.getLpParkingName().equals(parkId)) {
            return JinshiparkJSONResult.errorMsg("车辆不在该停车场内");
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

        //免费时间之内
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
        //根据车牌查询在场记录表
        JinshiparkInrecordRoadExample example = new JinshiparkInrecordRoadExample();
        JinshiparkInrecordRoadExample.Criteria criteria = example.createCriteria();
        criteria.andLpLincensePlateIdCarEqualTo(carNum);
        List<JinshiparkInrecordRoad> jinshiparkInrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
        //是否为在场车辆
        if (jinshiparkInrecordRoadList.size() == 0) {
            return JinshiparkJSONResult.errorMsg("未查询到车辆");
        }

        JinshiparkInrecordRoad jinshiparkInrecordRoad = jinshiparkInrecordRoadList.get(0);
        //判断是否在该区域内
        logger.error("lincensePlate.getLpParkingName():{}", jinshiparkInrecordRoad.getLpParkingName());
        if (!jinshiparkInrecordRoad.getLpParkingName().equals(parkId)) {
            return JinshiparkJSONResult.errorMsg("车辆不在该停车场内");
        }
        JinshiparkInrecordRoad inrecordRoad = new JinshiparkInrecordRoad();
        inrecordRoad.setLpDepartureTime(new Date());//离场时间
        if (payNum == 0) {
            inrecordRoad.setLpPaymentType("免费时长内出场");//支付方式
        } else {
            inrecordRoad.setLpOrderState("支付成功");//订单状态
            inrecordRoad.setLpPaymentType("人工收费出场");
        }
        inrecordRoad.setLpParkingRealCost(String.valueOf(realNum));//实付金额
        inrecordRoad.setLpParkingCost(String.valueOf(payNum));
        inrecordRoad.setLpParkingOften(dateOften);
        int result = jinshiparkInrecordRoadMapper.updateByExampleSelective(inrecordRoad, example);
        if (result > 0) {
            List<JinshiparkInrecordRoad> inrecordRoadList = jinshiparkInrecordRoadMapper.selectByExample(example);
            JinshiparkInrecordRoad inrecord = inrecordRoadList.get(0);
            //3.移动在场记录表里数据到在场历史表里
            JinshiparkOutrecordRoad jinshiparkOutrecordRoad = new JinshiparkOutrecordRoad();
            BeanUtils.copyProperties(inrecord, jinshiparkOutrecordRoad);
            jinshiparkOutrecordRoad.setLpId(null);
            int count = jinshiparkOutrecordRoadMapper.insertSelective(jinshiparkOutrecordRoad);
            if (count > 0) {
                jinshiparkInrecordRoadMapper.deleteByExample(example);
                return JinshiparkJSONResult.ok("出场成功");
            }
        }
        return JinshiparkJSONResult.errorMsg("出场失败");
    }
}
