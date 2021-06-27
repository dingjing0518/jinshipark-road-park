package com.jinshipark.cms.controller;

import com.jinshipark.cms.bo.LincensePlateBO;
import com.jinshipark.cms.bo.SearchBO;
import com.jinshipark.cms.config.UploadProperties;
import com.jinshipark.cms.service.LincensePlateService;
import com.jinshipark.cms.utils.JinshiparkJSONResult;
import com.jinshipark.cms.utils.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

@Controller
@RequestMapping("/lincensePlate")
public class LincensePlateController {

    @Resource
    private UploadProperties uploadProperties;

    @Autowired
    private LincensePlateService lincensePlateService;

    @CrossOrigin
    @RequestMapping(value = "/searchLincensePlate", method = RequestMethod.POST)
    @ResponseBody
    public JinshiparkJSONResult searchLincensePlate(@RequestBody SearchBO searchBO) throws ParseException {
        return JinshiparkJSONResult.ok(lincensePlateService.searchLincensePlate(searchBO));
    }

    /**
     * 车辆进场
     *
     * @param file     照片
     * @param carNum   车牌号
     * @param parkId   车场Id
     * @param userName 登录用户
     * @return 结果集
     */
    @CrossOrigin
    @RequestMapping(value = "/saveLincensePlateForRoad", method = RequestMethod.POST)
    @ResponseBody
    public JinshiparkJSONResult saveLincensePlateForRoad(@RequestParam(value = "file", required = true) MultipartFile file,
                                                         @RequestParam(value = "carNum", required = true) String carNum,
                                                         @RequestParam(value = "parkId", required = true) String parkId,
                                                         @RequestParam(value = "userName", required = true) String userName) {

//         //获取文件名
//        String fileName = file.getOriginalFilename();
//         //获取文件的后缀名
//        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //获取文件上传后的文件名
        String uploadFileName = KeyUtils.getOrderIdByPlate(carNum, parkId) + ".jpg";
        // 文件上传后的路径
        File dest = new File(uploadProperties.getFilePath() + parkId + "\\inpicpath\\" + uploadFileName);
        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            //文件访问路径
            String httpPath = uploadProperties.getHttpPrefix() + parkId + "/inpicpath/" + uploadFileName;
            return lincensePlateService.saveLincensePlateForRoad(carNum, parkId, httpPath, userName);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JinshiparkJSONResult.errorMsg("系统异常");
    }

    /**
     * 车辆详情
     *
     * @param file   照片
     * @param carNum 车牌号
     * @param parkId 车场Id
     * @return 结果集
     */
    @CrossOrigin
    @RequestMapping(value = "getLincensePlateForRoad", method = RequestMethod.POST)
    @ResponseBody
    public JinshiparkJSONResult getLincensePlateForRoad(@RequestParam(value = "file", required = true) MultipartFile file,
                                                        @RequestParam(value = "carNum", required = true) String carNum,
                                                        @RequestParam(value = "parkId", required = true) String parkId) {

//        // 获取文件名
//        String fileName = file.getOriginalFilename();
//        // 获取文件的后缀名
//        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //获取文件上传后的文件名
        String uploadFileName = KeyUtils.getOrderIdByPlate(carNum, parkId) + ".jpg";
        // 文件上传后的路径
        File dest = new File(uploadProperties.getFilePath() + parkId + "\\outpicpath\\" + uploadFileName);
        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            //文件访问路径
            String httpPath = uploadProperties.getHttpPrefix() + parkId + "/inpicpath/" + uploadFileName;
            return lincensePlateService.getLincensePlateForRoad(carNum, parkId, httpPath);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JinshiparkJSONResult.errorMsg("系统异常");
    }

    /**
     * 人工出场
     *
     * @param parkId    车场Id
     * @param carNum    车牌号
     * @param dateOften 停车时长
     * @param payNum    应收金额
     * @param realNum   实收金额
     * @return 结果集
     */
    @CrossOrigin
    @RequestMapping(value = "departure", method = RequestMethod.POST)
    @ResponseBody
    public JinshiparkJSONResult departure(@RequestParam(value = "parkId", required = true) String parkId,
                                          @RequestParam(value = "carNum", required = true) String carNum,
                                          @RequestParam(value = "dateOften", required = true) String dateOften,
                                          @RequestParam(value = "payNum", required = true) Integer payNum,
                                          @RequestParam(value = "realNum", required = true) Integer realNum) {
        return lincensePlateService.departure(parkId, carNum, dateOften, payNum, realNum);
    }

    @CrossOrigin
    @RequestMapping(value = "lincensePlateInfo", method = RequestMethod.POST)
    @ResponseBody
    public JinshiparkJSONResult lincensePlateInfo(@RequestBody LincensePlateBO lincensePlateBO) {
        return lincensePlateService.getLincensePlateForRoad(lincensePlateBO.getLpLincensePlateIdCar(), lincensePlateBO.getLpParkingName());
    }
}
