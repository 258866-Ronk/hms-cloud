-- ==========================================================================
-- 微服务架构医院挂号与问诊系统 —— 数据库初始化脚本
-- 遵循"每个微服务独立数据库"原则，共 5 个库：
--   hms_user       用户/认证服务
--   hms_department 科室服务
--   hms_schedule   排班服务
--   hms_register   挂号服务
--   hms_record     病历服务
-- 执行方式： mysql -uroot -p12345678 < init.sql
-- ==========================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------- 用户库
DROP DATABASE IF EXISTS hms_user;
CREATE DATABASE hms_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hms_user;

CREATE TABLE t_user (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username     VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号（医生为工号）',
    password     VARCHAR(100) NOT NULL COMMENT 'MD5 密码',
    real_name    VARCHAR(50)  NOT NULL COMMENT '姓名',
    role         VARCHAR(20)  NOT NULL DEFAULT 'PATIENT' COMMENT 'PATIENT/DOCTOR/ADMIN',
    phone        VARCHAR(20)  COMMENT '手机号',
    id_card      VARCHAR(30)  COMMENT '身份证号',
    gender       TINYINT      DEFAULT 1 COMMENT '1-男 2-女',
    age          INT          COMMENT '年龄',
    dept_code    VARCHAR(20)  COMMENT '所属科室编码',
    title        VARCHAR(30)  COMMENT '职称（医生）',
    specialty    VARCHAR(200) COMMENT '擅长领域（医生）',
    intro        VARCHAR(500) COMMENT '个人简介',
    status       TINYINT      DEFAULT 1 COMMENT '1-启用 0-停用',
    create_time  DATETIME     COMMENT '创建时间',
    update_time  DATETIME     COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 密码统一为 123456（MD5：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO t_user (username, password, real_name, role, phone, gender, age, dept_code, title, specialty, status, create_time) VALUES
('admin',    'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN',  '13800000000', 1, 35, NULL,      NULL,       NULL,                       1, NOW()),
('patient1', 'e10adc3949ba59abbe56e057f20f883e', '张伟',      'PATIENT','13900000001', 1, 32, NULL,      NULL,       NULL,                       1, NOW()),
('patient2', 'e10adc3949ba59abbe56e057f20f883e', '李静',      'PATIENT','13900000002', 2, 28, NULL,      NULL,       NULL,                       1, NOW()),
('patient3', 'e10adc3949ba59abbe56e057f20f883e', '王强',      'PATIENT','13900000003', 1, 45, NULL,      NULL,       NULL,                       1, NOW()),
('D001',     'e10adc3949ba59abbe56e057f20f883e', '张建国',    'DOCTOR', '13700000001', 1, 52, 'DEPT001', '主任医师', '高血压、冠心病、心律失常', 1, NOW()),
('D002',     'e10adc3949ba59abbe56e057f20f883e', '刘晓芳',    'DOCTOR', '13700000002', 2, 41, 'DEPT001', '副主任医师','糖尿病、甲状腺疾病',      1, NOW()),
('D003',     'e10adc3949ba59abbe56e057f20f883e', '陈志远',    'DOCTOR', '13700000003', 1, 46, 'DEPT002', '主任医师', '普外科、腹腔镜手术',       1, NOW()),
('D004',     'e10adc3949ba59abbe56e057f20f883e', '赵敏',      'DOCTOR', '13700000004', 2, 36, 'DEPT003', '主治医师', '小儿呼吸道感染、哮喘',     1, NOW()),
('D005',     'e10adc3949ba59abbe56e057f20f883e', '孙丽华',    'DOCTOR', '13700000005', 2, 44, 'DEPT004', '副主任医师','妇科炎症、孕期保健',      1, NOW()),
('D006',     'e10adc3949ba59abbe56e057f20f883e', '周文斌',    'DOCTOR', '13700000006', 1, 50, 'DEPT005', '主任医师', '骨折创伤、关节置换',       1, NOW()),
('D007',     'e10adc3949ba59abbe56e057f20f883e', '吴海燕',    'DOCTOR', '13700000007', 2, 39, 'DEPT006', '主治医师', '白内障、眼底疾病',         1, NOW()),
('D008',     'e10adc3949ba59abbe56e057f20f883e', '郑一鸣',    'DOCTOR', '13700000008', 1, 43, 'DEPT007', '副主任医师','口腔正畸、种植牙',        1, NOW());

-- ---------------------------------------------------------------- 科室库
DROP DATABASE IF EXISTS hms_department;
CREATE DATABASE hms_department DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hms_department;

CREATE TABLE t_department (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_code    VARCHAR(20) NOT NULL UNIQUE COMMENT '科室编码',
    dept_name    VARCHAR(50) NOT NULL COMMENT '科室名称',
    parent_code  VARCHAR(20) DEFAULT '' COMMENT '上级科室编码',
    location     VARCHAR(100) COMMENT '门诊位置',
    description  VARCHAR(500) COMMENT '科室介绍',
    sort         INT DEFAULT 0 COMMENT '排序号',
    status       TINYINT DEFAULT 1 COMMENT '1-启用 0-停用',
    create_time  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '科室表';

CREATE TABLE t_doctor (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_code  VARCHAR(20) NOT NULL UNIQUE COMMENT '医生工号（与用户表 username 对应）',
    doctor_name  VARCHAR(50) NOT NULL COMMENT '医生姓名',
    dept_code    VARCHAR(20) NOT NULL COMMENT '所属科室编码',
    title        VARCHAR(30) COMMENT '职称',
    specialty    VARCHAR(200) COMMENT '擅长领域',
    intro        VARCHAR(500) COMMENT '医生简介',
    reg_fee      DECIMAL(8, 2) DEFAULT 10.00 COMMENT '挂号费',
    gender       TINYINT DEFAULT 1 COMMENT '1-男 2-女',
    status       TINYINT DEFAULT 1,
    create_time  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医生表';

INSERT INTO t_department (dept_code, dept_name, parent_code, location, description, sort, status, create_time) VALUES
('DEPT001','心血管内科','','门诊楼 2 层 A 区','诊治高血压、冠心病、心力衰竭等心血管疾病', 1, 1, NOW()),
('DEPT002','普通外科',  '','门诊楼 3 层 B 区','开展普外科常见病、多发病的诊治与手术',     2, 1, NOW()),
('DEPT003','儿科',      '','门诊楼 1 层 C 区','儿童呼吸、消化、生长发育等疾病诊治',       3, 1, NOW()),
('DEPT004','妇产科',    '','门诊楼 4 层 A 区','妇科炎症、孕期保健与高危妊娠管理',         4, 1, NOW()),
('DEPT005','骨科',      '','门诊楼 3 层 D 区','骨折创伤、关节置换与运动医学',             5, 1, NOW()),
('DEPT006','眼科',      '','门诊楼 2 层 E 区','白内障、青光眼及眼底疾病诊治',             6, 1, NOW()),
('DEPT007','口腔科',    '','门诊楼 1 层 F 区','口腔正畸、种植与牙体牙髓治疗',             7, 1, NOW()),
('DEPT008','皮肤科',    '','门诊楼 2 层 G 区','湿疹、银屑病、痤疮等皮肤病诊治',           8, 1, NOW());

INSERT INTO t_doctor (doctor_code, doctor_name, dept_code, title, specialty, intro, reg_fee, gender, status, create_time) VALUES
('D001','张建国','DEPT001','主任医师','高血压、冠心病、心律失常',   '从事心血管内科临床工作 28 年', 20.00, 1, 1, NOW()),
('D002','刘晓芳','DEPT001','副主任医师','糖尿病、甲状腺疾病',       '医学博士，硕士研究生导师',     15.00, 2, 1, NOW()),
('D003','陈志远','DEPT002','主任医师','普外科、腹腔镜手术',         '完成腹腔镜手术逾 5000 例',     20.00, 1, 1, NOW()),
('D004','赵敏',  'DEPT003','主治医师','小儿呼吸道感染、哮喘',       '儿科临床工作 12 年',           10.00, 2, 1, NOW()),
('D005','孙丽华','DEPT004','副主任医师','妇科炎症、孕期保健',       '妇产科临床工作 18 年',         15.00, 2, 1, NOW()),
('D006','周文斌','DEPT005','主任医师','骨折创伤、关节置换',         '骨科临床工作 25 年',           20.00, 1, 1, NOW()),
('D007','吴海燕','DEPT006','主治医师','白内障、眼底疾病',           '眼科临床工作 10 年',           10.00, 2, 1, NOW()),
('D008','郑一鸣','DEPT007','副主任医师','口腔正畸、种植牙',         '完成种植手术逾 3000 例',       15.00, 1, 1, NOW());

-- ---------------------------------------------------------------- 排班库
DROP DATABASE IF EXISTS hms_schedule;
CREATE DATABASE hms_schedule DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hms_schedule;

CREATE TABLE t_schedule (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_code   VARCHAR(20) NOT NULL COMMENT '医生工号',
    doctor_name   VARCHAR(50) NOT NULL COMMENT '医生姓名（冗余，减少跨库查询）',
    dept_code     VARCHAR(20) NOT NULL COMMENT '科室编码',
    dept_name     VARCHAR(50) NOT NULL COMMENT '科室名称（冗余）',
    work_date     DATE NOT NULL COMMENT '出诊日期',
    period        VARCHAR(10) NOT NULL COMMENT 'AM-上午 PM-下午',
    total_num     INT NOT NULL DEFAULT 20 COMMENT '号源总数',
    remaining_num INT NOT NULL DEFAULT 20 COMMENT '剩余号源',
    reg_fee       DECIMAL(8, 2) DEFAULT 10.00 COMMENT '挂号费',
    room          VARCHAR(50) COMMENT '诊室',
    status        TINYINT DEFAULT 1 COMMENT '1-正常 0-停诊',
    version       INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time   DATETIME,
    update_time   DATETIME,
    INDEX idx_dept_date (dept_code, work_date),
    INDEX idx_doctor_date (doctor_code, work_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医生排班与号源表';

-- 未来 7 天排班（AM/PM），使用相对日期保证演示时始终有效
INSERT INTO t_schedule (doctor_code, doctor_name, dept_code, dept_name, work_date, period, total_num, remaining_num, reg_fee, room, status, version, create_time, update_time)
SELECT d.doctor_code, d.doctor_name, d.dept_code, dept.dept_name,
       DATE_ADD(CURDATE(), INTERVAL n.day_offset DAY), p.period, p.total_num, p.total_num,
       d.reg_fee, CONCAT(dept.dept_code, '-', p.room), 1, 0, NOW(), NOW()
FROM hms_department.t_doctor d
JOIN hms_department.t_department dept ON dept.dept_code = d.dept_code
CROSS JOIN (SELECT 0 AS day_offset UNION SELECT 1 UNION SELECT 2 UNION SELECT 3
            UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) n
CROSS JOIN (SELECT 'AM' AS period, 20 AS total_num, '101' AS room
            UNION SELECT 'PM', 15, '102') p;

-- ---------------------------------------------------------------- 挂号库
DROP DATABASE IF EXISTS hms_register;
CREATE DATABASE hms_register DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hms_register;

CREATE TABLE t_registration (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    reg_no          VARCHAR(40) NOT NULL UNIQUE COMMENT '挂号单号',
    patient_id      BIGINT NOT NULL COMMENT '患者ID（用户服务）',
    patient_name    VARCHAR(50) NOT NULL COMMENT '患者姓名（冗余）',
    doctor_code     VARCHAR(20) NOT NULL COMMENT '医生工号',
    doctor_name     VARCHAR(50) NOT NULL COMMENT '医生姓名（冗余）',
    dept_code       VARCHAR(20) NOT NULL COMMENT '科室编码',
    dept_name       VARCHAR(50) NOT NULL COMMENT '科室名称（冗余）',
    schedule_id     BIGINT NOT NULL COMMENT '排班ID（排班服务）',
    work_date       VARCHAR(10) NOT NULL COMMENT '就诊日期',
    period          VARCHAR(10) NOT NULL COMMENT 'AM/PM',
    queue_no        INT NOT NULL DEFAULT 1 COMMENT '就诊序号',
    fee             DECIMAL(8, 2) DEFAULT 10.00 COMMENT '挂号费',
    room            VARCHAR(50) COMMENT '诊室',
    status          TINYINT DEFAULT 0 COMMENT '0-待就诊 1-就诊中 2-已完成 3-已退号',
    chief_complaint VARCHAR(500) COMMENT '主诉',
    remark          VARCHAR(200) COMMENT '备注',
    create_time     DATETIME,
    update_time     DATETIME,
    INDEX idx_patient (patient_id),
    INDEX idx_doctor_date (doctor_code, work_date),
    INDEX idx_schedule (schedule_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '挂号记录表';

-- 示例挂号数据（取前 3 条排班）
INSERT INTO t_registration (reg_no, patient_id, patient_name, doctor_code, doctor_name, dept_code, dept_name,
                            schedule_id, work_date, period, queue_no, fee, room, status, chief_complaint, remark, create_time, update_time)
SELECT CONCAT('GH', DATE_FORMAT(s.work_date, '%Y%m%d'), s.id, '001'),
       2, '张伟', s.doctor_code, s.doctor_name, s.dept_code, s.dept_name,
       s.id, s.work_date, s.period, 1, s.reg_fee, s.room, 0, '近期头晕、血压偏高', s.dept_name, NOW(), NOW()
FROM hms_schedule.t_schedule s WHERE s.id = 1;

INSERT INTO t_registration (reg_no, patient_id, patient_name, doctor_code, doctor_name, dept_code, dept_name,
                            schedule_id, work_date, period, queue_no, fee, room, status, chief_complaint, remark, create_time, update_time)
SELECT CONCAT('GH', DATE_FORMAT(s.work_date, '%Y%m%d'), s.id, '002'),
       3, '李静', s.doctor_code, s.doctor_name, s.dept_code, s.dept_name,
       s.id, s.work_date, s.period, 2, s.reg_fee, s.room, 2, '儿童咳嗽发热两日', s.dept_name, NOW(), NOW()
FROM hms_schedule.t_schedule s WHERE s.id = 5;

-- 同步扣减示例数据占用的号源
UPDATE hms_schedule.t_schedule SET remaining_num = remaining_num - 1 WHERE id IN (1, 5);

-- ---------------------------------------------------------------- 病历库
DROP DATABASE IF EXISTS hms_record;
CREATE DATABASE hms_record DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hms_record;

CREATE TABLE t_medical_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_no       VARCHAR(40) NOT NULL UNIQUE COMMENT '病历编号',
    reg_id          BIGINT NOT NULL COMMENT '挂号单ID（挂号服务）',
    patient_id      BIGINT NOT NULL COMMENT '患者ID',
    patient_name    VARCHAR(50) COMMENT '患者姓名',
    doctor_code     VARCHAR(20) COMMENT '医生工号',
    doctor_name     VARCHAR(50) COMMENT '医生姓名',
    dept_code       VARCHAR(20) COMMENT '科室编码',
    dept_name       VARCHAR(50) COMMENT '科室名称',
    chief_complaint VARCHAR(500) COMMENT '主诉',
    diagnosis       VARCHAR(500) COMMENT '诊断结果',
    prescription    VARCHAR(1000) COMMENT '处方',
    advice          VARCHAR(500) COMMENT '医嘱',
    status          TINYINT DEFAULT 0 COMMENT '0-问诊中 1-已完成',
    visit_time      DATETIME COMMENT '就诊时间',
    create_time     DATETIME,
    update_time     DATETIME,
    INDEX idx_patient (patient_id),
    INDEX idx_reg (reg_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '电子病历表';

CREATE TABLE t_consultation (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id   BIGINT COMMENT '病历ID',
    reg_id      BIGINT COMMENT '挂号单ID',
    patient_id  BIGINT COMMENT '患者ID',
    doctor_code VARCHAR(20) COMMENT '医生工号',
    sender      VARCHAR(10) COMMENT 'PATIENT/DOCTOR',
    content     VARCHAR(1000) COMMENT '对话内容',
    create_time DATETIME,
    INDEX idx_reg (reg_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '在线问诊对话记录表';

INSERT INTO t_medical_record (record_no, reg_id, patient_id, patient_name, doctor_code, doctor_name,
                              dept_code, dept_name, chief_complaint, diagnosis, prescription, advice,
                              status, visit_time, create_time, update_time)
VALUES ('MR202600000001', 2, 3, '李静', 'D004', '赵敏', 'DEPT003', '儿科',
        '咳嗽伴发热 2 天，最高 38.6℃',
        '急性上呼吸道感染',
        '小儿氨酚黄那敏颗粒 1 袋 tid×3 天；布洛芬混悬液 5ml 发热时服用',
        '多饮水、注意休息，体温超过 38.5℃ 及时服药，3 日无好转复诊',
        1, NOW(), NOW(), NOW());

INSERT INTO t_consultation (record_id, reg_id, patient_id, doctor_code, sender, content, create_time) VALUES
(1, 2, 3, 'D004', 'PATIENT', '医生您好，孩子这两天一直咳嗽，还有点发烧。', NOW()),
(1, 2, 3, 'D004', 'DOCTOR',  '体温最高多少度？有没有流鼻涕、喉咙痛？', NOW()),
(1, 2, 3, 'D004', 'PATIENT', '最高 38.6 度，有点流清鼻涕，吃饭还可以。', NOW()),
(1, 2, 3, 'D004', 'DOCTOR',  '考虑急性上呼吸道感染，先按处方用药观察 3 天。', NOW());

SET FOREIGN_KEY_CHECKS = 1;
