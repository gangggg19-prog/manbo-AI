package com.mimo.babyassistantserver.mapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.CareRecord;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 育儿记录的数据访问层。
 * 查询使用 [当天开始, 次日开始) 的时间范围，避免 23:59:59 精度边界问题。
 */
@Mapper
public interface CareRecordMapper {
    @Insert("""
            INSERT INTO care_records (id, baby_id, record_type, recorded_at, amount_ml, created_at, updated_at)
            VALUES (#{id}, #{babyId}, #{type}, #{recordedAt}, #{amountMl}, #{createdAt}, #{updatedAt})
            """)
    int insert(CareRecord record);

    @Select("""
            SELECT id, baby_id, record_type AS type, recorded_at, amount_ml, created_at, updated_at
            FROM care_records
            WHERE id = #{id}
            """)
    CareRecord selectById(@Param("id") UUID id);

    @Select("""
            SELECT id, baby_id, record_type AS type, recorded_at, amount_ml, created_at, updated_at
            FROM care_records
            WHERE baby_id = #{babyId}
              AND recorded_at >= #{startInclusive}
              AND recorded_at < #{endExclusive}
            ORDER BY recorded_at DESC
            """)
    List<CareRecord> selectByBabyIdAndRecordedAtBetween(
            @Param("babyId") UUID babyId,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive);

    @Update("""
            UPDATE care_records
            SET record_type = #{type}, recorded_at = #{recordedAt}, amount_ml = #{amountMl}, updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(CareRecord record);

    @Delete("DELETE FROM care_records WHERE id = #{id}")
    int deleteById(@Param("id") UUID id);
}