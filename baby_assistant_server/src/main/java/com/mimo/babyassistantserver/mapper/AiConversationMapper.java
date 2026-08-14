package com.mimo.babyassistantserver.mapper;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.AiConversation;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** Data access for AI conversation session headers. */
@Mapper
public interface AiConversationMapper {
    @Insert("""
            INSERT INTO ai_conversations (id, baby_id, created_at, updated_at)
            VALUES (#{id}, #{babyId}, #{createdAt}, #{updatedAt})
            """)
    int insert(AiConversation conversation);

    @Select("""
            SELECT id, baby_id, created_at, updated_at
            FROM ai_conversations WHERE id = #{id}
            """)
    AiConversation selectById(@Param("id") UUID id);

    /** Moves a conversation to the top when a new user/assistant message is saved. */
    @Update("""
            UPDATE ai_conversations SET updated_at = #{updatedAt} WHERE id = #{id}
            """)
    int touchUpdatedAt(@Param("id") UUID id, @Param("updatedAt") Instant updatedAt);

    @Select("""
            SELECT id, baby_id, created_at, updated_at
            FROM ai_conversations
            WHERE baby_id = #{babyId}
            ORDER BY updated_at DESC
            LIMIT 1
            """)
    AiConversation selectLatestByBabyId(@Param("babyId") UUID babyId);
}