-- 事务演示用的账户表（在 crm 库中执行）
create table if not exists account
(
    id       int primary key auto_increment,
    username varchar(50)    not null,
    balance  decimal(12, 2) not null default 0.00
) engine = InnoDB
  default charset = utf8mb4;

-- 初始数据：张三 100 元，李四 0 元
insert into account (id, username, balance)
values (1, '张三', 100.00),
       (2, '李四', 0.00)
on duplicate key update username = values(username);
