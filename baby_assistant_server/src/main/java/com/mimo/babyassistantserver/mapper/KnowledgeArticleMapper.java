package com.mimo.babyassistantserver.mapper;

import java.util.List;

import com.mimo.babyassistantserver.entity.KnowledgeArticle;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** Database access for reviewed AI knowledge articles. */
@Mapper
public interface KnowledgeArticleMapper {
    @Select("""
            SELECT id, slug, title, category, min_age_months, max_age_months,
                   keywords, content, source_name, source_url, created_at
            FROM knowledge_articles
            WHERE min_age_months <= #{ageMonths}
              AND (max_age_months IS NULL OR max_age_months >= #{ageMonths})
            ORDER BY min_age_months ASC, created_at ASC
            """)
    List<KnowledgeArticle> selectForAge(@Param("ageMonths") int ageMonths);

    @Select("""
            SELECT id, slug, title, category, min_age_months, max_age_months,
                   keywords, content, source_name, source_url, created_at
            FROM knowledge_articles WHERE slug = #{slug}
            """)
    KnowledgeArticle selectBySlug(@Param("slug") String slug);
}