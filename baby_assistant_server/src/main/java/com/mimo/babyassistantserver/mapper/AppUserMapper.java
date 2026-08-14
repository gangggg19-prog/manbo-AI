package com.mimo.babyassistantserver.mapper;

import java.util.UUID;

import com.mimo.babyassistantserver.entity.AppUser;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** SQL boundary for Manbo sign-in accounts. */
@Mapper
public interface AppUserMapper {
    @Insert("""
            INSERT INTO app_users
                (id, username, display_name, password_hash, created_at)
            VALUES
                (#{id}, #{username}, #{displayName}, #{passwordHash}, #{createdAt})
            """)
    int insert(AppUser user);

    @Select("""
            SELECT id, username, display_name, password_hash, created_at
            FROM app_users
            WHERE LOWER(username) = LOWER(#{username})
            """)
    AppUser selectByUsername(@Param("username") String username);

    @Select("""
            SELECT id, username, display_name, password_hash, created_at
            FROM app_users
            WHERE id = #{id}
            """)
    AppUser selectById(@Param("id") UUID id);
}