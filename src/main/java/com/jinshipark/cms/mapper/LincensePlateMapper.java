package com.jinshipark.cms.mapper;

import com.jinshipark.cms.model.LincensePlate;
import com.jinshipark.cms.model.LincensePlateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LincensePlateMapper {
    long countByExample(LincensePlateExample example);

    int deleteByExample(LincensePlateExample example);

    int deleteByPrimaryKey(Integer lpId);

    int insert(LincensePlate record);

    int insertSelective(LincensePlate record);

    List<LincensePlate> selectByExample(LincensePlateExample example);

    LincensePlate selectByPrimaryKey(Integer lpId);

    int updateByExampleSelective(@Param("record") LincensePlate record, @Param("example") LincensePlateExample example);

    int updateByExample(@Param("record") LincensePlate record, @Param("example") LincensePlateExample example);

    int updateByPrimaryKeySelective(LincensePlate record);

    int updateByPrimaryKey(LincensePlate record);
}