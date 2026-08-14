package com.mimo.babyassistantserver.mapper;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.AiMessage;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** Data access for immutable AI messages. */
@Mapper
public interface AiMessageMapper {
    @Insert("""
            INSERT INTO ai_messages (id, conversation_id, message_role, content, source, created_at)
            VALUES (#{id}, #{conversationId}, #{role}, #{content}, #{source}, #{createdAt})
            """)
    int insert(AiMessage message);

    @Select("""
            SELECT id, conversation_id, message_role AS role, content, source, created_at
            FROM (
                SELECT id, conversation_id, message_role, content, source, created_at
                FROM ai_messages
                WHERE conversation_id = #{conversationId}
                ORDER BY created_at DESC
                LIMIT #{limit}
            ) recent
            ORDER BY created_at ASC
            """)
    List<AiMessage> selectRecentByConversationId(
            @Param("conversationId") UUID conversationId,
            @Param("limit") int limit);
}