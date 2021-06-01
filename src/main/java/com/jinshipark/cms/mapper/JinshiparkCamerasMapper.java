package com.jinshipark.cms.mapper;

import com.jinshipark.cms.model.JinshiparkCameras;
import com.jinshipark.cms.model.JinshiparkCamerasExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface JinshiparkCamerasMapper {
    long countByExample(JinshiparkCamerasExample example);

    int deleteByExample(JinshiparkCamerasExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(JinshiparkCameras record);

    int insertSelective(JinshiparkCameras record);

    List<JinshiparkCameras> selectByExample(JinshiparkCamerasExample example);

    JinshiparkCameras selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") JinshiparkCameras record, @Param("example") JinshiparkCamerasExample example);

    int updateByExample(@Param("record") JinshiparkCameras record, @Param("example") JinshiparkCamerasExample example);

    int updateByPrimaryKeySelective(JinshiparkCameras record);

    int updateByPrimaryKey(JinshiparkCameras record);
}