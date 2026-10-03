#!/bin/bash

# ============================================
#              MAIL SERVER
# ============================================

# Đặt tiêu đề cửa sổ terminal
echo -ne "\033]0;MAIL SERVER\007"

# Thư mục chứa project
PROJECT_DIR="/media/sf_MailServer"

# Kiểm tra thư mục tồn tại
if [ ! -d "$PROJECT_DIR" ]; then
    echo "LỖI: Không tìm thấy thư mục $PROJECT_DIR"
    read -p "Nhấn Enter để thoát..."
    exit 1
fi

cd "$PROJECT_DIR" || exit 1

echo "========================================"
echo "          MAIL SERVER SYSTEM"
echo "========================================"
echo ""

# Chạy server
mvn exec:java -Dexec.mainClass=com.mailserver.Main

# Giữ cửa sổ mở sau khi chương trình kết thúc
echo ""
read -p "Nhấn Enter để thoát..."