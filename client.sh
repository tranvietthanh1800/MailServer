#!/bin/bash

# ============================================
#           MAIL SYSTEM LAUNCHER
# ============================================

TITLE="MAIL SYSTEM LAUNCHER"
PROJECT_DIR="/media/sf_MailServer"

# Kiểm tra thư mục tồn tại
if [ ! -d "$PROJECT_DIR" ]; then
    echo "LỖI: Không tìm thấy thư mục $PROJECT_DIR"
    read -p "Nhấn Enter để thoát..."
    exit 1
fi

cd "$PROJECT_DIR" || exit 1

echo "========================================"
echo "             MAIL SYSTEM"
echo "========================================"
echo ""

# Hàm mở terminal mới chạy lệnh
run_in_terminal() {
    local title="$1"
    local cmd="$2"

    if command -v gnome-terminal >/dev/null 2>&1; then
        gnome-terminal --title="$title" -- bash -c "$cmd; exec bash"
    elif command -v konsole >/dev/null 2>&1; then
        konsole --title "$title" -e bash -c "$cmd; exec bash" &
    elif command -v xfce4-terminal >/dev/null 2>&1; then
        xfce4-terminal --title="$title" -e "bash -c '$cmd; exec bash'" &
    elif command -v xterm >/dev/null 2>&1; then
        xterm -T "$title" -e bash -c "$cmd; exec bash" &
    else
        echo "Không tìm thấy terminal GUI. Chạy thủ công:"
        echo "  $cmd"
        return 1
    fi
    return 0
}

# echo "[1/2] Đang khởi động Mail Server..."
# run_in_terminal "MAIL SERVER" \
#     "cd '$PROJECT_DIR' && mvn exec:java -Dexec.mainClass=com.mailserver.server.ServerGUI"

# echo "Đang chờ server khởi động..."
# sleep 2

echo "[2/2] Đang khởi động Mail Client..."
run_in_terminal "MAIL CLIENT" \
    "cd '$PROJECT_DIR' && mvn exec:java -Dexec.mainClass=com.mailserver.client.MailClientGUI"

echo ""
echo "Đã mở cả hai cửa sổ."
read -p "Nhấn Enter để thoát..."