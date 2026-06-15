erDiagram
    user {
        int PK user_id
        string student_id
        string email
        string phone
        int auth_status
        int credit_score
    }
    user_credit {
        int PK credit_id
        int FK user_id
        int credit_score
        int level
    }
    campus_auth {
        int PK auth_id
        int FK user_id
        string id_card
        int auth_status
    }
    demand_type {
        int PK type_id
        string type_name
        int parent_type_id
    }
    demand {
        int PK demand_id
        int FK publisher_id
        int FK type_id
        string title
        decimal reward
        int status
    }
    demand_tag {
        int PK tag_id
        string tag_name
        string category
    }
    demand_tag_relation {
        int PK id
        int FK demand_id
        int FK tag_id
    }
    match_rule {
        int PK rule_id
        string rule_type
        int priority
    }
    match_result {
        int PK match_id
        int FK demand_id
        int FK user_id
        int FK rule_id
        int match_score
        int status
    }
    order {
        int PK order_id
        int FK demand_id
        int FK publisher_id
        int FK provider_id
        int status
        decimal reward_amount
    }
    order_progress {
        int PK progress_id
        int FK order_id
        int status
        datetime operate_time
    }
    message {
        int PK message_id
        int FK sender_id
        int FK receiver_id
        int FK order_id
        string content
        boolean is_read
    }
    review {
        int PK review_id
        int FK order_id
        int FK reviewer_id
        int FK target_user_id
        int rating
        string content
    }
    transaction {
        int PK transaction_id
        int FK order_id
        int FK payer_id
        int FK payee_id
        decimal amount
        int status
    }
    guarantee {
        int PK guarantee_id
        int FK transaction_id
        decimal amount
        int status
    }

    %% 实体关系定义
    user ||--|| user_credit : 1-1
    user ||--|| campus_auth : 1-1
    user ||--o{ demand : 1-N (发布者)
    user ||--o{ match_result : 1-N (被匹配)
    user ||--o{ order : 1-N (发布者/承接者)
    user ||--o{ message : 1-N (发送者/接收者)
    user ||--o{ review : 1-N (评价者/被评价者)
    user ||--o{ transaction : 1-N (付款方/收款方)
    
    demand_type ||--o{ demand : 1-N
    demand ||--o{ demand_tag_relation : 1-N
    demand_tag ||--o{ demand_tag_relation : 1-N
    demand ||--o{ match_result : 1-N
    demand ||--|| order : 1-1
    
    match_rule ||--o{ match_result : 1-N
    
    order ||--o{ order_progress : 1-N
    order ||--o{ message : 1-N
    order ||--|| review : 1-1
    order ||--|| transaction : 1-1
    
    transaction ||--|| guarantee : 1-1
