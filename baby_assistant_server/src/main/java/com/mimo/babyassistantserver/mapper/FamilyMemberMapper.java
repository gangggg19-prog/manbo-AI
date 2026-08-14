package com.mimo.babyassistantserver.mapper;

import java.util.UUID;

import com.mimo.babyassistantserver.entity.FamilyMember;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** SQL boundary for access to a baby's private family space. */
@Mapper
public interface FamilyMemberMapper {
    @Insert("""
            INSERT INTO family_members
                (id, baby_id, user_id, member_role, joined_at)
            VALUES
                (#{id}, #{babyId}, #{userId}, #{memberRole}, #{joinedAt})
            """)
    int insert(FamilyMember member);

    @Select("""
            SELECT id, baby_id, user_id, member_role, joined_at
            FROM family_members
            WHERE baby_id = #{babyId} AND user_id = #{userId}
            """)
    FamilyMember selectByBabyAndUser(
            @Param("babyId") UUID babyId,
            @Param("userId") UUID userId);

    @Select("""
            SELECT COUNT(*)
            FROM family_members
            WHERE baby_id = #{babyId}
            """)
    int countByBabyId(@Param("babyId") UUID babyId);
}