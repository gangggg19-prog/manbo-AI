package com.mimo.babyassistantserver.mapper;

import java.time.Instant;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** Read-only aggregate queries; source records remain the single source of truth. */
@Mapper
public interface DailySummaryMapper {
    @Select("""
            SELECT COALESCE(SUM(amount_ml), 0)::INTEGER
            FROM care_records
            WHERE baby_id = #{babyId}
              AND record_type = 'FEEDING'
              AND recorded_at >= #{startInclusive}
              AND recorded_at < #{endExclusive}
            """)
    int selectFeedingMl(
            @Param("babyId") UUID babyId,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive);

    @Select("""
            SELECT COUNT(*)::INTEGER
            FROM care_records
            WHERE baby_id = #{babyId}
              AND record_type = 'DIAPER'
              AND recorded_at >= #{startInclusive}
              AND recorded_at < #{endExclusive}
            """)
    int selectDiaperCount(
            @Param("babyId") UUID babyId,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive);

    @Select("""
            SELECT COALESCE(
                SUM(
                    GREATEST(
                        0,
                        EXTRACT(EPOCH FROM (
                            LEAST(COALESCE(ended_at, #{now}), #{endExclusive})
                            - GREATEST(started_at, #{startInclusive})
                        )) / 60
                    )
                ),
                0
            )::INTEGER
            FROM sleep_sessions
            WHERE baby_id = #{babyId}
              AND started_at < #{endExclusive}
              AND (ended_at IS NULL OR ended_at > #{startInclusive})
            """)
    int selectSleepMinutes(
            @Param("babyId") UUID babyId,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive,
            @Param("now") Instant now);

    @Select("SELECT EXISTS(SELECT 1 FROM sleep_sessions WHERE baby_id = #{babyId} AND ended_at IS NULL)")
    boolean hasActiveSleepSession(@Param("babyId") UUID babyId);
}