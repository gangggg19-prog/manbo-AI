package com.mimo.babyassistantserver.mapper;

import java.util.UUID;

import com.mimo.babyassistantserver.entity.FamilyChatRoom;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** Database access for one baby's private family room. */
@Mapper
public interface FamilyChatRoomMapper {
    @Insert("""
            INSERT INTO family_chat_rooms (id, baby_id, title, created_at)
            VALUES (#{id}, #{babyId}, #{title}, #{createdAt})
            """)
    int insert(FamilyChatRoom room);

    @Select("""
            SELECT id, baby_id, title, created_at
            FROM family_chat_rooms WHERE id = #{id}
            """)
    FamilyChatRoom selectById(@Param("id") UUID id);

    @Select("""
            SELECT id, baby_id, title, created_at
            FROM family_chat_rooms WHERE baby_id = #{babyId}
            """)
    FamilyChatRoom selectByBabyId(@Param("babyId") UUID babyId);
}