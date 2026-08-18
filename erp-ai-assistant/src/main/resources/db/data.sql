-- Day16 学习假数据种子（可重复执行）
INSERT IGNORE INTO learning_item(item_code, name, spec) VALUES
('ITEM-A001', 'A001 原材料', '规格-学习假数据'),
('A001', 'A001 原材料', '规格-学习假数据（别名编码）');

INSERT IGNORE INTO learning_inventory(item_code, warehouse, qty) VALUES
('ITEM-A001', '原料仓', 120),
('ITEM-A001', '成品仓', 0),
('A001', '原料仓', 120),
('A001', '成品仓', 0);

INSERT IGNORE INTO learning_period(company, period, status) VALUES
('主公司', '2026-08', 'OPEN'),
('主公司', '2026-07', 'CLOSED');
