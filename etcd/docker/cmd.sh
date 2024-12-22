#!/bin/bash

# 定义变量
ETCDCTL_API=3
ETCDCTL_CMD=etcdctl
ETCD_USER=etcd
ETCD_PASSWORD=etcd

ETCD_ROOT_USER="root"
ETCD_ROOT_PASSWORD="root"
ETCD_ROOT_ROLE="root-role"

ETCD_ENDPOINTS="--endpoints=127.0.0.1:12379,127.0.0.1:22379,127.0.0.1:32379 --user $ETCD_ROOT_USER:$ETCD_ROOT_PASSWORD"

# 显示帮助信息
show_help() {
    echo "Usage: $0 {start|disable|enable|create_user|list_users|create_role|create_root_user|grant_permissions|info}"
    echo
    echo "Options:"
    echo "  start                    Start the ETCD service"
    echo "  disable                  Disable ETCD authentication"
    echo "  enable                   Enable ETCD authentication"
    echo "  create_user [username] [password]    Create an ETCD user"
    echo "  list_users               List all ETCD users"
    echo "  create_role [role_name]  Create an ETCD role (default: root-role)"
    echo "  create_root_user         Create the ROOT user with predefined credentials"
    echo "  grant_permissions [role_name] [permission_type] [key]"
    echo "                          Grant permissions (r/w/rw) to a role for a specific key"
    echo "  info             List all ETCD cluster members"
    echo
    echo "Permission Types:"
    echo "  r                        Read permission"
    echo "  w                        Write permission"
    echo "  rw                       Read-Write permission"
    echo
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 create_user myuser mypassword"
    echo "  $0 create_role myrole"
    echo "  $0 create_root_user"
    echo "  $0 grant_permissions myrole r /mykey"
    echo "  $0 list_members"
}

# 启动 ETCD 服务
start() {
    echo "Starting ETCD service..."
    mkdir -p etcd-node{1,2,3}
    echo "Creating network etcd-cluster..."
    docker network create --driver bridge --subnet 172.20.0.0/16 --gateway 172.20.0.1 etcd-cluster
    echo "Starting etcd-node1..."
    docker-compose up
    echo "ETCD service started!"
}

# 禁用 ETCD 权限（使用 root 权限禁用认证）
disable_etcd_permissions() {
    echo "Disabling ETCD authentication..."
    $ETCDCTL_CMD $ETCD_ENDPOINTS auth disable

    if [[ $? -eq 0 ]]; then
        echo "ETCD authentication disabled successfully!"
    else
        echo "Failed to disable ETCD authentication."
        exit 1
    fi
}

# 启用 ETCD 鉴权
enable_etcd_permissions() {
    echo "Enabling ETCD authentication..."
    $ETCDCTL_CMD $ETCD_ENDPOINTS auth enable

    if [[ $? -eq 0 ]]; then
        echo "ETCD authentication enabled successfully!"
    else
        echo "Failed to enable ETCD authentication."
        exit 1
    fi
}

# 创建 ETCD 用户
create_etcd_user() {
    local USERNAME=${1:-$ETCD_USER}
    local PASSWORD=${1:-$ETCD_PASSWORD}

    echo "Creating ETCD user with username: $USERNAME and password: $PASSWORD..."
    $ETCDCTL_CMD $ETCD_ENDPOINTS user add $USERNAME:$PASSWORD

    if [[ $? -eq 0 ]]; then
        echo "ETCD user $USERNAME created successfully!"
    else
        echo "Failed to create ETCD user $USERNAME."
        exit 1
    fi
}

# 打印当前 ETCD 用户列表
list_etcd_users() {
    echo "Listing all ETCD users..."
    $ETCDCTL_CMD $ETCD_ENDPOINTS user list

    if [[ $? -eq 0 ]]; then
        echo "ETCD users listed successfully!"
    else
        echo "Failed to list ETCD users."
        exit 1
    fi
}

# 创建 ETCD 角色，默认角色为 root-role
create_etcd_role() {
    local ROLE_NAME=${1:-$ETCD_ROOT_ROLE}

    echo "Creating ETCD role with name: $ROLE_NAME..."
    $ETCDCTL_CMD $ETCD_ENDPOINTS role add $ROLE_NAME

    if [[ $? -eq 0 ]]; then
        echo "ETCD role $ROLE_NAME created successfully!"
    else
        echo "Failed to create ETCD role $ROLE_NAME."
        exit 1
    fi
}

# 为角色添加权限，支持 r、w 和 rw
grant_etcd_role_permissions() {
    local ROLE_NAME=$1
    local PERMISSION_TYPE=$2
    local KEY=${3:-"/"}

    if [ -z "$PERMISSION_TYPE" ] || [ -z "$KEY" ]; then
        echo "Permission type (r/w/rw) and key are required!"
        exit 1
    fi

    case $PERMISSION_TYPE in
        r)
            echo "Granting read permission to role $ROLE_NAME for key $KEY..."
            $ETCDCTL_CMD $ETCD_ENDPOINTS role grant $ROLE_NAME --read $KEY
            ;;
        w)
            echo "Granting write permission to role $ROLE_NAME for key $KEY..."
            $ETCDCTL_CMD $ETCD_ENDPOINTS role grant $ROLE_NAME --write $KEY
            ;;
        rw)
            echo "Granting read-write permission to role $ROLE_NAME for key $KEY..."
            $ETCDCTL_CMD $ETCD_ENDPOINTS role grant $ROLE_NAME --read $KEY --write $KEY
            ;;
        *)
            echo "Invalid permission type. Use 'r' for read, 'w' for write, or 'rw' for read-write."
            exit 1
            ;;
    esac

    if [[ $? -eq 0 ]]; then
        echo "Permission granted successfully!"
    else
        echo "Failed to grant permission."
        exit 1
    fi
}

# 创建 ROOT 用户
create_root_user() {
    echo "Creating ROOT user with predefined credentials..."
    $ETCDCTL_CMD $ETCD_ENDPOINTS user add $ETCD_ROOT_USER:$ETCD_ROOT_PASSWORD

    if [[ $? -eq 0 ]]; then
        echo "ROOT user created successfully!"
    else
        echo "Failed to create ROOT user."
        exit 1
    fi
}

# 输出 ETCD 集群的节点信息及 leader 信息
list_etcd_members() {
    echo "Fetching ETCD cluster members..."
    # 获取集群成员信息
    $ETCDCTL_CMD $ETCD_ENDPOINTS member list

    if [[ $? -eq 0 ]]; then
        echo "ETCD cluster members listed successfully!"
    else
        echo "Failed to fetch ETCD cluster members."
        exit 1
    fi

    echo "Fetching ETCD endpoint statuses to identify leader..."
    # 获取 endpoint 状态信息以显示 leader
    $ETCDCTL_CMD $ETCD_ENDPOINTS endpoint status --write-out=table

    if [[ $? -eq 0 ]]; then
        echo "ETCD endpoint statuses fetched successfully!"
    else
        echo "Failed to fetch ETCD endpoint statuses."
        exit 1
    fi
}

# 根据传入的参数调用对应的方法
case $1 in
    start)
        start
        ;;
    disable)
        disable_etcd_permissions
        ;;
    enable)
        enable_etcd_permissions
        ;;
    create_user)
        create_etcd_user $2
        ;;
    list_users)
        list_etcd_users
        ;;
    create_role)
        create_etcd_role $2
        ;;
    create_root_user)
        create_root_user
        ;;
    grant_permissions)
        grant_etcd_role_permissions $2 $3 $4
        ;;
    info)
        list_etcd_members
        ;;
    -h|--help)
        show_help
        ;;
    *)
        show_help
        exit 1
        ;;
esac