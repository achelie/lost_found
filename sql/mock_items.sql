USE campus_lost_found;

-- 先确保admin1用户存在
INSERT IGNORE INTO `user` (`username`, `password`, `nickname`, `role`, `avatar`) VALUES
('admin1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试管理员', 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin1');

SET @admin_id = (SELECT id FROM `user` WHERE username = 'admin1');

-- 50条失物信息 (type=0)
INSERT INTO `item` (`user_id`, `type`, `title`, `category`, `location`, `item_time`, `description`, `contact`, `images`, `status`, `created_at`) VALUES
(@admin_id, 0, '丢失黑色钱包', '证件', '图书馆二楼自习室', '2026-04-10 14:30:00', '黑色皮质钱包，内有身份证、银行卡若干，现金约200元，对我非常重要', '13800138001', 'https://picsum.photos/400/300?random=1', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '苹果AirPods Pro耳机丢失', '电子产品', '三食堂门口', '2026-04-11 12:15:00', '白色充电盒，有轻微划痕，耳机名字是小明的AirPods', '13800138002', 'https://picsum.photos/400/300?random=2', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '学生证遗失', '证件', '教学楼A座302教室', '2026-04-12 09:45:00', '计算机学院2022级，学号202201001，姓名张三，有卡套', '13800138003', 'https://picsum.photos/400/300?random=3', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '蓝色运动外套丢失', '衣物', '操场看台上', '2026-04-09 18:00:00', '耐克蓝色外套，L码，左袖口有磨损痕迹', '13800138004', 'https://picsum.photos/400/300?random=4', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, 'U盘丢失', '电子产品', '图书馆电子阅览室', '2026-04-08 16:20:00', '闪迪64G红色U盘，里面有毕业论文和课程设计资料', '13800138005', 'https://picsum.photos/400/300?random=5', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '银色华为Mate40手机', '电子产品', '校车上', '2026-04-13 07:50:00', '银色机身，背面有小熊贴纸，锁屏是猫咪壁纸', '13800138006', 'https://picsum.photos/400/300?random=6', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '高等数学教材丢失', '书籍', '教学楼B座105', '2026-04-07 15:30:00', '同济大学第七版，书上有大量笔记，写了我的名字', '13800138007', 'https://picsum.photos/400/300?random=7', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '粉色保温杯', '其他', '一食堂二楼', '2026-04-14 11:40:00', '膳魔师粉色500ml保温杯，杯底有轻微凹陷', '13800138008', 'https://picsum.photos/400/300?random=8', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '身份证丢失', '证件', '超市收银台', '2026-04-15 20:10:00', '身份证姓名李四，出生日期1999年，地址XX市XX区', '13800138009', 'https://picsum.photos/400/300?random=9', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '黑色双肩包', '其他', '宿舍楼下', '2026-04-06 22:00:00', '小米黑色双肩包，里面有课本、笔记本和雨伞一把', '13800138010', 'https://picsum.photos/400/300?random=10', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '小米手环7', '电子产品', '体育场跑道', '2026-04-05 06:30:00', '黑色小米手环7，表带略有磨损，屏幕有细微划痕', '13800138011', 'https://picsum.photos/400/300?random=11', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '眼镜丢失', '其他', '教学楼走廊长椅', '2026-04-16 10:00:00', '黑框眼镜，400度左右，棕色眼镜盒', '13800138012', 'https://picsum.photos/400/300?random=12', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '机械键盘', '电子产品', '实验室', '2026-04-04 17:00:00', 'IKBC黑色机械键盘，青轴，空格键有磨损', '13800138013', 'https://picsum.photos/400/300?random=13', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '银行卡一张', '证件', 'ATM机旁', '2026-04-03 14:00:00', '建设银行储蓄卡，尾号4321', '13800138014', 'https://picsum.photos/400/300?random=14', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '灰色针织围巾', '衣物', '校车座位上', '2026-04-02 18:30:00', '灰色毛线围巾，手工编织，末端有流苏', '13800138015', 'https://picsum.photos/400/300?random=15', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '英语四六级词汇书', '书籍', '自习室', '2026-04-01 21:00:00', '新东方绿宝书，书内有划线笔记', '13800138016', 'https://picsum.photos/400/300?random=16', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '蓝牙耳机', '电子产品', '篮球场边', '2026-03-31 19:00:00', '索尼WF-1000XM4，黑色充电盒', '13800138017', 'https://picsum.photos/400/300?random=17', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '学生卡', '证件', '澡堂门口', '2026-03-30 21:30:00', '校园一卡通，卡号202202001，余额还有120元', '13800138018', 'https://picsum.photos/400/300?random=18', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '白色运动鞋', '衣物', '体育馆', '2026-03-29 16:00:00', '耐克白鞋42码，鞋边有污渍', '13800138019', 'https://picsum.photos/400/300?random=19', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '笔记本电脑充电器', '电子产品', '宿舍楼下长椅', '2026-03-28 12:00:00', '联想笔记本Type-C充电器，65W功率', '13800138020', 'https://picsum.photos/400/300?random=20', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '考研复习资料', '书籍', '考研自习室', '2026-04-10 08:00:00', '数学英语政治全套复习资料，标注很详细', '13800138021', 'https://picsum.photos/400/300?random=21', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '黑色雨伞', '其他', '教学楼门口', '2026-04-11 17:00:00', '天堂伞自动折叠伞，伞柄有裂纹', '13800138022', 'https://picsum.photos/400/300?random=22', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '驾驶证', '证件', '停车场', '2026-04-12 15:00:00', 'C1驾驶证，姓名王五，有效期还有3年', '13800138023', 'https://picsum.photos/400/300?random=23', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '红色运动手环', '电子产品', '健身房', '2026-04-13 20:00:00', '小米手环红色表带，电量不足', '13800138024', 'https://picsum.photos/400/300?random=24', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '专业课本', '书籍', '阶梯教室', '2026-04-14 12:30:00', '计算机组成原理，唐朔飞版', '13800138025', 'https://picsum.photos/400/300?random=25', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '钱包', '证件', '快递站', '2026-04-15 19:00:00', '棕色帆布钱包，内有身份证和饭卡', '13800138026', 'https://picsum.photos/400/300?random=26', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '鼠标', '电子产品', '机房', '2026-04-09 10:00:00', '罗技G102黑色游戏鼠标', '13800138027', 'https://picsum.photos/400/300?random=27', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '羽绒服', '衣物', '图书馆储物柜', '2026-04-08 20:30:00', '波司登黑色羽绒服M码', '13800138028', 'https://picsum.photos/400/300?random=28', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '钢笔', '其他', '教室课桌', '2026-04-07 16:00:00', '凌美狩猎者黑色钢笔，笔帽有划痕', '13800138029', 'https://picsum.photos/400/300?random=29', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '移动硬盘', '电子产品', '实验室门口', '2026-04-06 14:30:00', '西部数据1TB蓝色移动硬盘', '13800138030', 'https://picsum.photos/400/300?random=30', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '公交卡', '证件', '公交站牌', '2026-04-05 08:45:00', '市民卡，还有80多元余额', '13800138031', 'https://picsum.photos/400/300?random=31', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '帽子', '衣物', '操场', '2026-04-04 17:30:00', '黑色棒球帽，正面有NY标志', '13800138032', 'https://picsum.photos/400/300?random=32', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '论文打印稿', '书籍', '打印店', '2026-04-03 11:20:00', '毕业设计论文终稿，共80页', '13800138033', 'https://picsum.photos/400/300?random=33', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '计算器', '电子产品', '考场门口', '2026-04-02 11:50:00', '卡西欧科学计算器，考试用', '13800138034', 'https://picsum.photos/400/300?random=34', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR),
(@admin_id, 0, '钥匙串', '其他', '宿舍楼楼梯', '2026-04-01 23:00:00', '一串钥匙，有三个宿舍钥匙和指甲刀', '13800138035', 'https://picsum.photos/400/300?random=35', 1, NOW() - INTERVAL FLOOR(RAND() * 72) HOUR);
