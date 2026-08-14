package com.mimo.babyassistantserver.mapper;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.FamilyInvite;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** Atomic database operations for short-lived family invite codes. */
@Mapper
public interface FamilyInviteMapper {
    @Insert("""
            INSERT INTO family_invites
                (id, baby_id, invite_code, created_by, expires_at,
                 used_by, used_at, created_at)
            VALUES
                (#{id}, #{babyId}, #{inviteCode}, #{createdBy}, #{expiresAt},
                 #{usedBy}, #{usedAt}, #{createdAt})
            """)
    int insert(FamilyInvite invite);

    @Select("""
            SELECT id, baby_id, invite_code, created_by, expires_at,
                   used_by, used_at, created_at
            FROM family_invites
            WHERE invite_code = #{inviteCode}
            """)
    FamilyInvite selectByCode(@Param("inviteCode") String inviteCode);

    /**
     * The WHERE clause makes invitation consumption atomic: only the first
     * unexpired request can update the row.
     */
    @Update("""
            UPDATE family_invites
            SET used_by = #{userId}, used_at = #{usedAt}
            WHERE id = #{inviteId}
              AND used_at IS NULL
              AND expires_at > #{usedAt}
            """)
    int markUsed(
            @Param("inviteId") UUID inviteId,
            @Param("userId") UUID userId,
            @Param("usedAt") Instant usedAt);
}