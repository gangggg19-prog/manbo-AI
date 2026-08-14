package com.mimo.babyassistantserver.mapper;

import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.BabyProfile;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 宝宝档案的数据访问层。
 * MyBatis 将这里的注解 SQL 映射为数据库操作，避免业务层散落 SQL。
 */
@Mapper
public interface BabyProfileMapper {
    @Insert("""
            INSERT INTO baby_profiles (id, display_name, birth_date, created_at, updated_at)
            VALUES (#{id}, #{displayName}, #{birthDate}, #{createdAt}, #{updatedAt})
            """)
    int insert(BabyProfile profile);

    @Select("""
            SELECT id, display_name, birth_date, created_at, updated_at
            FROM baby_profiles
            WHERE id = #{id}
            """)
    BabyProfile selectById(@Param("id") UUID id);

    @Select("""
            SELECT id, display_name, birth_date, created_at, updated_at
            FROM baby_profiles
            ORDER BY created_at ASC
            """)
    List<BabyProfile> selectAll();
}