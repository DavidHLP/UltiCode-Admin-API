-- Users Table
CREATE TABLE `user` (
  `user_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `avatar` VARCHAR(255),
  `introduction` VARCHAR(255) DEFAULT '用户未填写',
  `address` VARCHAR(255) DEFAULT '用户未填写',
  `status` INT DEFAULT 1, -- 1 for active, 0 for inactive
  `last_login_ip` VARCHAR(50),
  `last_login` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
);

-- Roles Table
CREATE TABLE `role` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `role_name` VARCHAR(255) NOT NULL UNIQUE,
  `status` INT,
  `remark` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User-Role Junction Table
CREATE TABLE `user_role` (
  `user_id` BIGINT,
  `role_id` BIGINT,
  PRIMARY KEY (`user_id`, `role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`),
  FOREIGN KEY (`role_id`) REFERENCES `role`(`id`)
);

-- Tokens Table
CREATE TABLE `token` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `token` VARCHAR(255) NOT NULL,
    `token_type` VARCHAR(50) NOT NULL,
    `expired` BOOLEAN NOT NULL,
    `revoked` BOOLEAN NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
);