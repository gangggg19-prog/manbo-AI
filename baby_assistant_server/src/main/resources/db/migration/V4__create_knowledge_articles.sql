CREATE TABLE knowledge_articles (
    id UUID PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(48) NOT NULL,
    min_age_months INTEGER NOT NULL CHECK (min_age_months >= 0),
    max_age_months INTEGER,
    keywords TEXT NOT NULL,
    content TEXT NOT NULL,
    source_name VARCHAR(160) NOT NULL,
    source_url TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_knowledge_articles_age
    ON knowledge_articles (min_age_months, max_age_months);

INSERT INTO knowledge_articles (
    id, slug, title, category, min_age_months, max_age_months,
    keywords, content, source_name, source_url, created_at
) VALUES
(
    '92022f2b-1f82-45ef-8384-1677f3171a01',
    'six-month-complementary-feeding',
    '约 6 月龄开始添加辅食：基础原则',
    'COMPLEMENTARY_FEEDING',
    6, 8,
    '辅食,添加辅食,六个月,6个月,吃什么,食物,母乳,配方奶',
    '一般在约 6 月龄开始，在母乳或配方奶之外添加辅食。开始时从少量、质地适合吞咽的食物做起，逐步增加种类和质地；母乳或配方奶仍应继续提供。早产儿或有特殊健康情况时，应先咨询儿科医生或当地专业人员。',
    'WHO - Complementary feeding',
    'https://www.who.int/health-topics/complementary-feeding',
    NOW()
),
(
    '92022f2b-1f82-45ef-8384-1677f3171a02',
    'introducing-food-allergens',
    '添加辅食时观察食物过敏反应',
    'FOOD_ALLERGY',
    6, 23,
    '过敏,食物过敏,辅食,花生,鸡蛋,牛奶,反应,皮疹,呕吐',
    '开始添加辅食后，可能引起过敏反应的食物可一次只引入一种，并从少量开始，便于观察反应。出现呼吸困难、面部或嘴唇肿胀、反复呕吐等急性症状，应立即寻求医疗帮助。',
    'NHS - Baby food allergies',
    'https://www.nhs.uk/best-start-in-life/baby/weaning/safe-weaning/food-allergies/',
    NOW()
)
ON CONFLICT (slug) DO NOTHING;