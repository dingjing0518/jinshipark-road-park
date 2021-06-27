package com.jinshipark.cms.mapper;

import com.jinshipark.cms.model.JinshiparkInrecordRoad;
import com.jinshipark.cms.model.JinshiparkInrecordRoadExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface JinshiparkInrecordRoadMapper {
    long countByExample(JinshiparkInrecordRoadExample example);

    int deleteByExample(JinshiparkInrecordRoadExample example);

    int deleteByPrimaryKey(Integer lpId);

    int insert(JinshiparkInrecordRoad record);

    int insertSelective(JinshiparkInrecordRoad record);

    List<JinshiparkInrecordRoad> selectByExample(JinshiparkInrecordRoadExample example);

    JinshiparkInrecordRoad selectByPrimaryKey(Integer lpId);

    int updateByExampleSelective(@Param("record") JinshiparkInrecordRoad record, @Param("example") JinshiparkInrecordRoadExample example);

    int updateByExample(@Param("record") JinshiparkInrecordRoad record, @Param("example") JinshiparkInrecordRoadExample example);

    int updateByPrimaryKeySelective(JinshiparkInrecordRoad record);

    int updateByPrimaryKey(JinshiparkInrecordRoad record);
}