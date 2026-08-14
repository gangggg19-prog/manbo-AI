package com.mimo.babyassistantserver.mapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.SleepSession;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** SQL mapping for sleep sessions. */
@Mapper
public interface SleepSessionMapper {
    @Insert("""
            INSERT INTO sleep_sessions (id, baby_id, started_at, ended_at, created_at, updated_at)
            VALUES (#{id}, #{babyId}, #{startedAt}, #{endedAt}, #{createdAt}, #{updatedAt})
            """)
    int insert(SleepSession session);

    @Select("""
            SELECT id, baby_id, started_at, ended_at, created_at, updated_at
            FROM sleep_sessions
            WHERE id = #{id}
            """)
    SleepSession selectById(@Param("id") UUID id);

    @Select("""
            SELECT id, baby_id, started_at, ended_at, created_at, updated_at
            FROM sleep_sessions
            WHERE baby_id = #{babyId} AND ended_at IS NULL
            """)
    SleepSession selectActiveByBabyId(@Param("babyId") UUID babyId);

    @Select("""
            SELECT id, baby_id, started_at, ended_at, created_at, updated_at
            FROM sleep_sessions
            WHERE baby_id = #{babyId}
              AND started_at < #{endExclusive}
              AND (ended_at IS NULL OR ended_at > #{startInclusive})
            ORDER BY started_at DESC
            """)
    List<SleepSession> selectOverlappingDay(
            @Param("babyId") UUID babyId,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive);

    @Update("""
            UPDATE sleep_sessions
            SET ended_at = #{endedAt}, updated_at = #{updatedAt}
            WHERE id = #{id} AND ended_at IS NULL
            """)
    int finish(SleepSession session);

    @Delete("DELETE FROM sleep_sessions WHERE id = #{id}")
    int deleteById(@Param("id") UUID id);
}