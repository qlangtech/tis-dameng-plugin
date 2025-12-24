让我给你一个完整的检查清单和配置步骤：

1. 检查数据库归档模式

在达梦数据库控制台执行以下 SQL：

-- 检查是否启用归档模式
SELECT PARA_NAME, PARA_VALUE
FROM V$DM_ARCH_INI
WHERE PARA_NAME = 'ARCH_MODE';

-- 或者查看数据库状态
SELECT * FROM V$DATABASE;

预期结果：ARCH_MODE 应该是 1（启用）或 Y

2. 检查归档日志配置

-- 查看归档日志配置
SELECT * FROM V$DM_ARCH_INI;

-- 查看归档日志目录
SELECT * FROM V$ARCH_DEST;

-- 查看归档日志文件
SELECT * FROM V$ARCH_FILE;

3. 检查重做日志（Redo Log）

-- 查看在线重做日志
SELECT * FROM V$RLOG;

-- 或
SELECT * FROM V$LOG;

4. 检查 LogMiner 相关权限

-- 检查当前用户权限
SELECT * FROM DBA_SYS_PRIVS WHERE GRANTEE = USER;

-- 检查是否有 LOGMINER 相关权限
SELECT * FROM DBA_TAB_PRIVS
WHERE GRANTEE = USER
AND TABLE_NAME LIKE '%LOGMNR%';

配置步骤（如果未启用）

如果检查发现数据库未启用归档模式，需要进行以下配置：

1. 启用归档模式

-- 1. 关闭数据库（需要 SYSDBA 权限）
SHUTDOWN IMMEDIATE;

-- 2. 以 MOUNT 模式启动
STARTUP MOUNT;

-- 3. 启用归档模式
ALTER DATABASE ARCHIVELOG;

-- 4. 打开数据库
ALTER DATABASE OPEN;

-- 5. 验证归档模式已启用
SELECT * FROM V$DATABASE;

2. 配置归档日志路径

-- 设置归档日志目录（修改为实际路径）
ALTER SYSTEM SET ARCH_DEST = '/path/to/archivelog/dir';

-- 或者修改 dm.ini 配置文件
-- ARCH_DEST = /path/to/archivelog/dir
-- ARCH_MODE = 1

3. 启用补充日志（Supplemental Logging）

达梦数据库的 CDC 需要启用补充日志：

-- 启用最小补充日志
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;

-- 或启用全部列补充日志（推荐用于 CDC）
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;

-- 验证补充日志状态
SELECT SUPPLEMENTAL_LOG_DATA_MIN, SUPPLEMENTAL_LOG_DATA_ALL
FROM V$DATABASE;

4. 手动触发日志切换（生成归档日志）

-- 手动切换日志文件，生成归档日志
ALTER SYSTEM SWITCH LOGFILE;

-- 再次检查归档日志
SELECT * FROM V$ARCH_FILE;
