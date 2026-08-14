package com.mimo.babyassistantserver.mapper;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.FamilyChatMessage;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** Database access for private family chat messages. */
@Mapper
public interface FamilyChatMessageMapper {
    @Insert("""
            INSERT INTO family_chat_messages
                (id, room_id, sender_user_id, sender_name, content, sent_at)
            VALUES
                (#{id}, #{roomId}, #{senderUserId}, #{senderName}, #{content}, #{sentAt})
            """)
    int insert(FamilyChatMessage message);

    @Select("""
            SELECT id, room_id, sender_user_id, sender_name, content, sent_at
            FROM family_chat_messages
            WHERE room_id = #{roomId}
            ORDER BY sent_at ASC
            """)
    List<FamilyChatMessage> selectByRoomId(@Param("roomId") UUID roomId);
}