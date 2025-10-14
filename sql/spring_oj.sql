/*
  OJ Core Schema (MySQL 8.4+)
  Design goals:
  - 正交：题面/数据集/用例/语言配置/提交解耦
  - 可版本化：通过 dataset 版本化测试数据与 checker
  - 去冗余：统计用视图，不落重复字段
  - 可扩展：比赛、讨论、收藏、反应统一
  - 安全索引：常用查询均有覆盖索引
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 基础：用户与权限
-- =========================

DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS auth_tokens;

CREATE TABLE users (
                       id            BIGINT NOT NULL AUTO_INCREMENT,
                       username      VARCHAR(64)  COLLATE utf8mb4_unicode_ci NOT NULL,
                       email         VARCHAR(254) COLLATE utf8mb4_unicode_ci NOT NULL,
                       password_hash VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                       avatar_url    VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                       bio           VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                       status        TINYINT NOT NULL DEFAULT 1, -- 1:active, 0:disabled
                       last_login_at DATETIME DEFAULT NULL,
                       last_login_ip VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                       created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
                       id         BIGINT NOT NULL AUTO_INCREMENT,
                       code       VARCHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL, -- admin, setter, judge, user
                       name       VARCHAR(128) COLLATE utf8mb4_unicode_ci NOT NULL,
                       remark     VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
                             id         BIGINT NOT NULL AUTO_INCREMENT,
                             code       VARCHAR(128) COLLATE utf8mb4_unicode_ci NOT NULL, -- problem.edit, contest.manage, judge.submit, ...
                             name       VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_permissions_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (user_id, role_id),
                            KEY idx_user_roles_role (role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  perm_id BIGINT NOT NULL,
                                  PRIMARY KEY (role_id, perm_id),
                                  KEY idx_role_permissions_perm (perm_id),
                                  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_role_permissions_perm FOREIGN KEY (perm_id) REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 简洁 token 表：不重复冗余字段
CREATE TABLE auth_tokens (
                             id         BIGINT NOT NULL AUTO_INCREMENT,
                             user_id    BIGINT NOT NULL,
                             token      CHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL,   -- 建议存储哈希
                             kind       ENUM('access','refresh','api') NOT NULL,
                             revoked    TINYINT(1) NOT NULL DEFAULT 0,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             expires_at TIMESTAMP NULL DEFAULT NULL,
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_auth_tokens_token (token),
                             KEY idx_auth_tokens_user (user_id, kind, revoked),
                             CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 词表：难度 / 分类 / 标签 / 语言
-- =========================

DROP TABLE IF EXISTS difficulties;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS languages;

CREATE TABLE difficulties (
                              id       TINYINT NOT NULL,
                              code     VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL, -- easy/medium/hard…
                              sort_key TINYINT NOT NULL,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_difficulties_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
                            id   SMALLINT NOT NULL AUTO_INCREMENT,
                            code VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,     -- algorithms, database, concurrency…
                            name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_categories_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tags (
                      id         BIGINT NOT NULL AUTO_INCREMENT,
                      slug       VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL, -- two-pointers, dp, bfs…
                      name       VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_tags_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 支持的编程语言（判题用）
CREATE TABLE languages (
                           id          SMALLINT NOT NULL AUTO_INCREMENT,
                           code        VARCHAR(40) COLLATE utf8mb4_unicode_ci NOT NULL,  -- cpp17, java17, python3.11, go1.22 ...
                           display_name VARCHAR(80) COLLATE utf8mb4_unicode_ci NOT NULL,
                           runtime_image VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL, -- 运行容器镜像标识（可选）
                           is_active   TINYINT(1) NOT NULL DEFAULT 1,
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_languages_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 题目（不含题面文本与测试数据）
-- =========================

DROP TABLE IF EXISTS problems;
DROP TABLE IF EXISTS problem_tags;

CREATE TABLE problems (
                          id              BIGINT NOT NULL AUTO_INCREMENT,
                          slug            VARCHAR(120) COLLATE utf8mb4_unicode_ci NOT NULL,  -- two-sum / 1-two-sum
                          problem_type    ENUM('coding','sql','shell','concurrency','interactive','output-only') DEFAULT 'coding',
                          difficulty_id   TINYINT NOT NULL,
                          category_id     SMALLINT DEFAULT NULL,
                          creator_id      BIGINT DEFAULT NULL,
                          solution_entry  VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL, -- 与语言无关的入口逻辑名
                          time_limit_ms   INT DEFAULT NULL,
                          memory_limit_kb INT DEFAULT NULL,
                          is_public       TINYINT(1) NOT NULL DEFAULT 1,
                          meta_json       JSON DEFAULT NULL,  -- 自由扩展（来源、公司、频次等）
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_problems_slug (slug),
                          KEY idx_problems_visibility (is_public, id),
                          KEY idx_problems_diff (difficulty_id, id),
                          KEY idx_problems_cat (category_id, id),
                          CONSTRAINT fk_problem_difficulty FOREIGN KEY (difficulty_id) REFERENCES difficulties (id),
                          CONSTRAINT fk_problem_category   FOREIGN KEY (category_id)   REFERENCES categories (id),
                          CONSTRAINT fk_problem_creator    FOREIGN KEY (creator_id)    REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE problem_tags (
                              problem_id BIGINT NOT NULL,
                              tag_id     BIGINT NOT NULL,
                              PRIMARY KEY (problem_id, tag_id),
                              KEY idx_pt_tag (tag_id),
                              CONSTRAINT fk_pt_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE,
                              CONSTRAINT fk_pt_tag     FOREIGN KEY (tag_id)     REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 题面文本（i18n）与语言配置
-- =========================

DROP TABLE IF EXISTS problem_statements;
DROP TABLE IF EXISTS problem_language_configs;

CREATE TABLE problem_statements (
                                    id           BIGINT NOT NULL AUTO_INCREMENT,
                                    problem_id   BIGINT NOT NULL,
                                    lang_code    VARCHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL, -- zh-CN, en, zh-TW...
                                    title        VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                    description_md   MEDIUMTEXT COLLATE utf8mb4_unicode_ci NOT NULL,
                                    constraints_md   MEDIUMTEXT COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                    examples_md      MEDIUMTEXT COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_problem_lang (problem_id, lang_code),
                                    CONSTRAINT fk_ps_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE problem_language_configs (
                                          id            BIGINT NOT NULL AUTO_INCREMENT,
                                          problem_id    BIGINT NOT NULL,
                                          language_id   SMALLINT NOT NULL,
                                          function_name VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL, -- 该语言下真实入口名
                                          starter_code  MEDIUMTEXT COLLATE utf8mb4_unicode_ci,
                                          PRIMARY KEY (id),
                                          UNIQUE KEY uk_problem_language (problem_id, language_id),
                                          KEY idx_plc_problem (problem_id),
                                          CONSTRAINT fk_plc_problem  FOREIGN KEY (problem_id)  REFERENCES problems (id)  ON DELETE CASCADE,
                                          CONSTRAINT fk_plc_language FOREIGN KEY (language_id) REFERENCES languages (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 文件存储（统一引用对象存储/本地路径）
-- =========================

DROP TABLE IF EXISTS files;

CREATE TABLE files (
                       id          BIGINT NOT NULL AUTO_INCREMENT,
                       storage_key VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL, -- 如 s3://bucket/key 或 本地路径
                       sha256      CHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL,
                       mime_type   VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                       size_bytes  BIGINT DEFAULT NULL,
                       created_by  BIGINT DEFAULT NULL,
                       created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_files_sha (sha256),
                       KEY idx_files_creator (created_by),
                       CONSTRAINT fk_files_user FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 数据集（dataset）/ 用例 / 检查器
-- =========================
-- dataset 让测试数据/checker可版本化；problem 绑定一个 active_dataset_id

DROP TABLE IF EXISTS datasets;
DROP TABLE IF EXISTS testcase_groups;
DROP TABLE IF EXISTS testcases;

CREATE TABLE datasets (
                          id              BIGINT NOT NULL AUTO_INCREMENT,
                          problem_id      BIGINT NOT NULL,
                          name            VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                          is_active       TINYINT(1) NOT NULL DEFAULT 0, -- 每题最多1个激活，用触发器或应用层保障
                          checker_type    ENUM('text','float','custom') NOT NULL DEFAULT 'text',
                          checker_file_id BIGINT DEFAULT NULL,           -- custom 时引用 files
                          float_abs_tol   DOUBLE DEFAULT NULL,           -- float checker参数
                          float_rel_tol   DOUBLE DEFAULT NULL,
                          created_by      BIGINT DEFAULT NULL,
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          KEY idx_ds_problem (problem_id, is_active),
                          CONSTRAINT fk_ds_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE,
                          CONSTRAINT fk_ds_checker_file FOREIGN KEY (checker_file_id) REFERENCES files (id) ON DELETE SET NULL,
                          CONSTRAINT fk_ds_creator FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 将问题的当前激活数据集直接挂在 problems（避免冗余拷贝测试数据）
ALTER TABLE problems
    ADD COLUMN active_dataset_id BIGINT DEFAULT NULL,
    ADD CONSTRAINT fk_problem_active_dataset
        FOREIGN KEY (active_dataset_id) REFERENCES datasets (id) ON DELETE SET NULL;

CREATE TABLE testcase_groups (
                                 id          BIGINT NOT NULL AUTO_INCREMENT,
                                 dataset_id  BIGINT NOT NULL,
                                 name        VARCHAR(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 is_sample   TINYINT(1) NOT NULL DEFAULT 0,
                                 weight      INT NOT NULL DEFAULT 1,
                                 created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id),
                                 KEY idx_tcg_dataset (dataset_id, is_sample),
                                 CONSTRAINT fk_tcg_dataset FOREIGN KEY (dataset_id) REFERENCES datasets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE testcases (
                           id           BIGINT NOT NULL AUTO_INCREMENT,
                           group_id     BIGINT NOT NULL,
                           order_index  INT NOT NULL DEFAULT 0,
    -- 大/变长输入输出放文件存储，避免 JSON 冗余；小数据可直接 JSON
                           input_file_id   BIGINT DEFAULT NULL,
                           output_file_id  BIGINT DEFAULT NULL,
                           input_json      JSON DEFAULT NULL,
                           output_json     JSON DEFAULT NULL,
                           output_type     VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL, -- e.g. line, json, custom
                           score           INT NOT NULL DEFAULT 10,
                           created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           KEY idx_tc_group_order (group_id, order_index),
                           CONSTRAINT fk_tc_group FOREIGN KEY (group_id) REFERENCES testcase_groups (id) ON DELETE CASCADE,
                           CONSTRAINT fk_tc_input_file  FOREIGN KEY (input_file_id)  REFERENCES files (id) ON DELETE SET NULL,
                           CONSTRAINT fk_tc_output_file FOREIGN KEY (output_file_id) REFERENCES files (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 提交 / 判题细项 / 产物
-- =========================

DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS submission_tests;
DROP TABLE IF EXISTS submission_artifacts;

CREATE TABLE submissions (
                             id             BIGINT NOT NULL AUTO_INCREMENT,
                             user_id        BIGINT NOT NULL,
                             problem_id     BIGINT NOT NULL,
                             dataset_id     BIGINT NOT NULL,                 -- 冻结提交与当时数据集的关系
                             language_id    SMALLINT NOT NULL,
                             source_file_id BIGINT NOT NULL,                 -- 代码存文件
                             code_bytes     INT DEFAULT NULL,                -- 便捷统计
                             verdict        ENUM('PD','AC','WA','TLE','MLE','RE','CE','OLE','PE','IE') NOT NULL DEFAULT 'PD',
                             score          INT DEFAULT NULL,                -- 部分分
                             time_ms        INT DEFAULT NULL,                -- 总体（可存最大/加权）
                             memory_kb      INT DEFAULT NULL,
                             judge_msg      TEXT COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             ip_addr        VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             contest_id     BIGINT DEFAULT NULL,             -- 若来自比赛
                             created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             KEY idx_submissions_user (user_id, created_at),
                             KEY idx_submissions_problem (problem_id, created_at),
                             KEY idx_submissions_contest (contest_id, created_at),
                             KEY idx_submissions_verdict (verdict, created_at),
                             CONSTRAINT fk_sub_user     FOREIGN KEY (user_id)     REFERENCES users (id)      ON DELETE CASCADE,
                             CONSTRAINT fk_sub_problem  FOREIGN KEY (problem_id)  REFERENCES problems (id)   ON DELETE CASCADE,
                             CONSTRAINT fk_sub_dataset  FOREIGN KEY (dataset_id)  REFERENCES datasets (id)   ON DELETE RESTRICT,
                             CONSTRAINT fk_sub_language FOREIGN KEY (language_id) REFERENCES languages (id)  ON DELETE RESTRICT,
                             CONSTRAINT fk_sub_source   FOREIGN KEY (source_file_id) REFERENCES files (id)   ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE submission_tests (
                                  id             BIGINT NOT NULL AUTO_INCREMENT,
                                  submission_id  BIGINT NOT NULL,
                                  testcase_id    BIGINT NOT NULL,
                                  group_id       BIGINT NOT NULL,
                                  verdict        ENUM('AC','WA','TLE','MLE','RE','OLE','PE','SKIP','IE') NOT NULL,
                                  time_ms        INT DEFAULT NULL,
                                  memory_kb      INT DEFAULT NULL,
                                  score          INT DEFAULT NULL,
                                  message        TEXT COLLATE utf8mb4_unicode_ci DEFAULT NULL, -- 首错信息等
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_submission_test (submission_id, testcase_id),
                                  KEY idx_st_group (group_id),
                                  CONSTRAINT fk_st_submission FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_st_testcase   FOREIGN KEY (testcase_id)   REFERENCES testcases (id)   ON DELETE RESTRICT,
                                  CONSTRAINT fk_st_group      FOREIGN KEY (group_id)      REFERENCES testcase_groups (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE submission_artifacts (
                                      id            BIGINT NOT NULL AUTO_INCREMENT,
                                      submission_id BIGINT NOT NULL,
                                      kind          ENUM('compile_log','run_log','stderr','stdout','diff','system') NOT NULL,
                                      file_id       BIGINT NOT NULL,
                                      created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id),
                                      KEY idx_sa_submission (submission_id, kind),
                                      CONSTRAINT fk_sa_submission FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE,
                                      CONSTRAINT fk_sa_file       FOREIGN KEY (file_id)       REFERENCES files (id)       ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 比赛 / 题单 / 参赛
-- =========================

DROP TABLE IF EXISTS contests;
DROP TABLE IF EXISTS contest_problems;
DROP TABLE IF EXISTS contest_participants;

CREATE TABLE contests (
                          id            BIGINT NOT NULL AUTO_INCREMENT,
                          title         VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                          description_md MEDIUMTEXT COLLATE utf8mb4_unicode_ci,
                          kind          ENUM('icpc','oi','ioi','cf','acm','custom') NOT NULL DEFAULT 'icpc',
                          start_time    DATETIME NOT NULL,
                          end_time      DATETIME NOT NULL,
                          is_visible    TINYINT(1) NOT NULL DEFAULT 1,
                          created_by    BIGINT DEFAULT NULL,
                          created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          KEY idx_contest_time (start_time, end_time),
                          CONSTRAINT fk_contest_creator FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contest_problems (
                                  contest_id BIGINT NOT NULL,
                                  problem_id BIGINT NOT NULL,
                                  alias      VARCHAR(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL, -- A/B/C…
                                  points     INT DEFAULT NULL,
                                  order_no   INT DEFAULT 0,
                                  PRIMARY KEY (contest_id, problem_id),
                                  KEY idx_cp_order (contest_id, order_no),
                                  CONSTRAINT fk_cp_contest FOREIGN KEY (contest_id) REFERENCES contests (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_cp_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contest_participants (
                                      contest_id BIGINT NOT NULL,
                                      user_id    BIGINT NOT NULL,
                                      registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (contest_id, user_id),
                                      KEY idx_cpart_user (user_id, contest_id),
                                      CONSTRAINT fk_cpart_contest FOREIGN KEY (contest_id) REFERENCES contests (id) ON DELETE CASCADE,
                                      CONSTRAINT fk_cpart_user    FOREIGN KEY (user_id)    REFERENCES users (id)    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 社交：评论 / 收藏 / 反应
-- =========================

DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS reactions;

CREATE TABLE comments (
                          id           BIGINT NOT NULL AUTO_INCREMENT,
                          entity_type  ENUM('problem','contest','submission') NOT NULL,
                          entity_id    BIGINT NOT NULL,
                          user_id      BIGINT NOT NULL,
                          content_md   MEDIUMTEXT COLLATE utf8mb4_unicode_ci NOT NULL,
                          created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          KEY idx_comments_entity (entity_type, entity_id, created_at),
                          KEY idx_comments_user (user_id, created_at),
                          CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bookmarks (
                           user_id     BIGINT NOT NULL,
                           entity_type ENUM('problem','contest') NOT NULL,
                           entity_id   BIGINT NOT NULL,
                           created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, entity_type, entity_id),
                           KEY idx_bookmarks_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reactions (
                           user_id     BIGINT NOT NULL,
                           entity_type ENUM('problem','comment') NOT NULL,
                           entity_id   BIGINT NOT NULL,
                           kind        ENUM('like','dislike') NOT NULL,
                           created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, entity_type, entity_id, kind),
                           KEY idx_reactions_entity (entity_type, entity_id, kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 判题基础设施（可选但常用）
-- =========================

DROP TABLE IF EXISTS judge_nodes;
DROP TABLE IF EXISTS judge_jobs;

CREATE TABLE judge_nodes (
                             id          BIGINT NOT NULL AUTO_INCREMENT,
                             name        VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                             status      ENUM('online','offline','busy','draining') NOT NULL DEFAULT 'online',
                             runtime_info JSON DEFAULT NULL,        -- 硬件/负载信息
                             last_heartbeat TIMESTAMP NULL DEFAULT NULL,
                             created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_jnode_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE judge_jobs (
                            id            BIGINT NOT NULL AUTO_INCREMENT,
                            submission_id BIGINT NOT NULL,
                            node_id       BIGINT DEFAULT NULL,
                            status        ENUM('queued','running','finished','failed','canceled') NOT NULL DEFAULT 'queued',
                            priority      TINYINT NOT NULL DEFAULT 0,
                            created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            started_at    TIMESTAMP NULL DEFAULT NULL,
                            finished_at   TIMESTAMP NULL DEFAULT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_job_submission (submission_id),
                            KEY idx_jobs_status (status, priority, created_at),
                            CONSTRAINT fk_jobs_submission FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE,
                            CONSTRAINT fk_jobs_node       FOREIGN KEY (node_id)       REFERENCES judge_nodes (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- 统计视图（去冗余：不落地 acceptance_rate 等）
-- =========================

DROP VIEW IF EXISTS vw_problem_stats;
CREATE VIEW vw_problem_stats AS
SELECT
    p.id AS problem_id,
    COUNT(s.id)                           AS submission_count,
    SUM(CASE WHEN s.verdict = 'AC' THEN 1 ELSE 0 END) AS solved_count,
    ROUND(
            CASE WHEN COUNT(s.id)=0
                     THEN NULL
                 ELSE (SUM(CASE WHEN s.verdict='AC' THEN 1 ELSE 0 END) / COUNT(s.id))*100
                END, 2
    ) AS acceptance_rate,
    MAX(s.created_at) AS last_submission_at
FROM problems p
         LEFT JOIN submissions s ON s.problem_id = p.id
GROUP BY p.id;

DROP VIEW IF EXISTS vw_user_problem_best;
CREATE VIEW vw_user_problem_best AS
SELECT
    s.user_id,
    s.problem_id,
    MIN(CASE WHEN s.verdict='AC' THEN s.created_at ELSE NULL END) AS first_ac_time,
    MAX(s.score) AS best_score
FROM submissions s
GROUP BY s.user_id, s.problem_id;

SET FOREIGN_KEY_CHECKS = 1;
