#!/bin/bash

echo "========================================"
echo "初始化 Syh Blog 数据库"
echo "========================================"
echo ""

export PGPASSWORD=qwer1234

echo "正在连接 PostgreSQL 数据库..."
echo "数据库: syh_blog"
echo "用户: syh"
echo ""

psql -h localhost -p 5432 -U syh -d syh_blog -f "src/main/resources/schema.sql"

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "数据库初始化成功！"
    echo "========================================"
else
    echo ""
    echo "========================================"
    echo "数据库初始化失败！"
    echo "请检查 PostgreSQL 是否正在运行"
    echo "========================================"
    exit 1
fi

unset PGPASSWORD
