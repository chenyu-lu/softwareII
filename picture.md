erDiagram
    %% 用户模块
    user {
        bigint user_id PK
        varchar student_id
        varchar name
        varchar phone
        varchar email
        tinyint auth_status
        int credit_score
        datetime register_time
    }
    user_credit {
        bigint credit_id PK
        bigint user_id FK
        int credit_score
        tinyint credit_level
        int evaluation_count
        int violation_count
        datetime update_time
    }
    campus_auth {
        bigint auth_id PK
        bigint user_id FK
        varchar student_id
        varchar id_card
        varchar student_card_url
        tinyint auth_status
        datetime audit_time
    }

    %% 需求模块
    demand_type {
        int type_id PK
        varchar type_name
        int parent_id
        int sort
        tinyint status
    }
    demand {
        bigint demand_id PK
        bigint publisher_id FK
        int type_id FK
        varchar title
        text description
        varchar location
        decimal reward
        tinyint status
        datetime deadline
        datetime publish_time
    }
    demand_tag {
        int tag_id PK
        varchar tag_name
        varchar category
        int use_count
    }
    demand_tag_relation {
        bigint id PK
        bigint demand_id FK
        int tag_id FK
    }

    %% 订单&进度
    order_info {
        bigint order_id PK
        bigint demand_id FK
        bigint publisher_id FK
        bigint provider_id FK
        tinyint status
        decimal reward_amount
        datetime create_time
    }
    order_progress {
        bigint progress_id PK
        bigint order_id FK
        tinyint status
        varchar description
        bigint operator_id FK
        datetime operate_time
    }

    %% 交易&担保
    transaction {
        bigint transaction_id PK
        bigint order_id FK
        bigint payer_id FK
        bigint payee_id FK
        decimal amount
        tinyint type
        tinyint status
        datetime create_time
    }
    guarantee {
        bigint guarantee_id PK
        bigint transaction_id FK
        decimal amount
        tinyint status
        datetime create_time
    }

    %% 匹配模块
    match_rule {
        int rule_id PK
        varchar rule_name
        tinyint rule_type
        int priority
        tinyint status
    }
    match_result {
        bigint match_id PK
        bigint demand_id FK
        bigint user_id FK
        int rule_id FK
        int match_score
        tinyint status
    }

    %% 消息、评价、售后
    message {
        bigint message_id PK
        bigint sender_id FK
        bigint receiver_id FK
        bigint order_id FK
        text content
        tinyint is_read
        datetime send_time
    }
    review {
        bigint review_id PK
        bigint order_id FK
        bigint reviewer_id FK
        bigint target_user_id FK
        tinyint rating
        text content
        tinyint is_anonymous
    }
    after_sale {
        bigint after_sale_id PK
        bigint order_id FK
        bigint applicant_id FK
        tinyint status
        datetime apply_time
    }

    %% 关系映射
    user ||--|| user_credit : "一对一"
    user ||--|| campus_auth : "一对一"
    user ||--o{ demand : "一对多"
    user ||--o{ order_info : "一对多"
    user ||--o{ message : "一对多"
    user ||--o{ review : "一对多"
    user ||--o{ transaction : "一对多"
    user ||--o{ match_result : "一对多"

    demand_type ||--o{ demand : "一对多"
    demand ||--o{ demand_tag_relation : "一对多"
    demand_tag ||--o{ demand_tag_relation : "一对多"
    demand ||--o{ match_result : "一对多"
    demand ||--|| order_info : "一对一"

    order_info ||--o{ order_progress : "一对多"
    order_info ||--|| transaction : "一对一"
    order_info ||--|| review : "一对一"
    order_info ||--o{ message : "一对多"
    order_info ||--o{ after_sale : "一对多"

    transaction ||--|| guarantee : "一对一"
    match_rule ||--o{ match_result : "一对多"

