CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$ DECLARE ITALY_GROUP_ID UUID;

BACHELOR_GROUP_ID UUID;

PAYMENT_1_ID UUID;

PAYMENT_2_ID UUID;

PAYMENT_3_ID UUID;

PAYMENT_4_ID UUID;

PAYMENT_5_ID UUID;

PAYMENT_6_ID UUID;

PAYMENT_7_ID UUID;

PAYMENT_8_ID UUID;

PAYMENT_9_ID UUID;

PAYMENT_10_ID UUID;

PAYMENT_11_ID UUID;

PAYMENT_12_ID UUID;

PAYMENT_13_ID UUID;

PAYMENT_14_ID UUID;

PAYMENT_15_ID UUID;

BEGIN -- Insert into the group table and return the generated id
INSERT INTO
    USERS (
        id,
        created_at,
        balance,
        conf,
        email,
        password,
        username,
        validated,
        version,
        created_by
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'johntesting@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'johntesting',
        't',
        0,
        NULL
    ),
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'marytester@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'marytester',
        't',
        0,
        NULL
    ),
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'andrewtest@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'andrewtest',
        't',
        0,
        NULL
    ),
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'sarahruns@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'sarahruns',
        't',
        0,
        NULL
    ),
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'danieldev@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'danieldev',
        't',
        0,
        NULL
    ),
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'emilytest@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'emilytest',
        't',
        0,
        NULL
    ),
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        0,
        '{"currency":"euro"}',
        'bentesting@gmail.com',
        '$2a$10$7dYXyZIjNBtKoHTe17./9.jsRZBjgElazE5Gp8tY2k.mef4O7memC',
        'bentesting',
        't',
        0,
        NULL
    );

INSERT INTO
    GROUPS (
        id,
        created_at,
        active,
        description,
        "name",
        "version",
        created_by
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        true,
        'Group for organizing the expenses from the Italy friends trip',
        'Italy trip',
        0,
        (
            SELECT
                id
            FROM
                USERS
            WHERE
                username = 'johntesting'
        )
    ) RETURNING id INTO ITALY_GROUP_ID;

INSERT INTO
    GROUPS (
        id,
        created_at,
        active,
        description,
        "name",
        "version",
        created_by
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        true,
        'Group to manage the Andrew bachelor party',
        'Bachelor party',
        0,
        (
            SELECT
                id
            FROM
                USERS
            WHERE
                username = 'marytester'
        )
    ) RETURNING id INTO BACHELOR_GROUP_ID;

INSERT INTO
    public.group_users (join_date, group_id, user_id)
VALUES
    (
        CURRENT_TIMESTAMP - INTERVAL '7' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '6' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '5' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '4' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'andrewtest'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '3' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '2' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '1' day,
        ITALY_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        )
    ),
    (
        CURRENT_TIMESTAMP,
        BACHELOR_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        CURRENT_TIMESTAMP - INTERVAL '1' day,
        (
            SELECT
                id
            FROM
                groups
            WHERE
                name = 'Bachelor party'
                AND created_by = (
                    SELECT
                        id
                    FROM
                        users
                    WHERE
                        username = 'marytester'
                )
        ),
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    );

INSERT INTO
    requests (id, created_at, created_by, group_id, user_id)
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        ),
        BACHELOR_GROUP_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        )
    );

INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '100' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        ),
        ITALY_GROUP_ID,
        'Dinner at Rossini restaurant'
    ) RETURNING id INTO PAYMENT_1_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        35,
        PAYMENT_1_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        35,
        PAYMENT_1_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        )
    );

-- Payment 2: Lunch at a cafe (2 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '200' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'andrewtest'
        ),
        ITALY_GROUP_ID,
        'Lunch at a cafe'
    ) RETURNING id INTO PAYMENT_2_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        20,
        PAYMENT_2_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    ),
    (
        20,
        PAYMENT_2_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    );

-- Payment 3: Train tickets from Rome to Florence (3 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '300' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        ),
        ITALY_GROUP_ID,
        'Train tickets from Rome to Florence'
    ) RETURNING id INTO PAYMENT_3_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        50,
        PAYMENT_3_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        )
    ),
    (
        50,
        PAYMENT_3_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        50,
        PAYMENT_3_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'andrewtest'
        )
    );

-- Payment 4: Museum tickets in Florence (4 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '400' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        ),
        ITALY_GROUP_ID,
        'Museum tickets in Florence'
    ) RETURNING id INTO PAYMENT_4_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        15,
        PAYMENT_4_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        )
    ),
    (
        15,
        PAYMENT_4_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    ),
    (
        15,
        PAYMENT_4_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    ),
    (
        15,
        PAYMENT_4_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    );

-- Payment 5: Pizza dinner in Naples (1 user)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '500' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        ),
        ITALY_GROUP_ID,
        'Pizza dinner in Naples'
    ) RETURNING id INTO PAYMENT_5_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        20,
        PAYMENT_5_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        )
    );

-- Payment 6: Hotel room in Rome (2 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '600' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        ),
        ITALY_GROUP_ID,
        'Hotel room in Rome'
    ) RETURNING id INTO PAYMENT_6_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        100,
        PAYMENT_6_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        100,
        PAYMENT_6_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    );

-- Payment 7: Gondola ride in Venice (3 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '700' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        ),
        ITALY_GROUP_ID,
        'Gondola ride in Venice'
    ) RETURNING id INTO PAYMENT_7_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        40,
        PAYMENT_7_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    ),
    (
        40,
        PAYMENT_7_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'andrewtest'
        )
    ),
    (
        40,
        PAYMENT_7_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        )
    );

-- Payment 8: Wine tasting in Tuscany (4 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '800' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        ),
        ITALY_GROUP_ID,
        'Wine tasting in Tuscany'
    ) RETURNING id INTO PAYMENT_8_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        30,
        PAYMENT_8_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        )
    ),
    (
        30,
        PAYMENT_8_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        30,
        PAYMENT_8_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    ),
    (
        30,
        PAYMENT_8_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        )
    );

-- Payment 9: Dinner at a fancy restaurant in Milan (3 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '900' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        ),
        ITALY_GROUP_ID,
        'Dinner at a fancy restaurant in Milan'
    ) RETURNING id INTO PAYMENT_9_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        60,
        PAYMENT_9_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'danieldev'
        )
    ),
    (
        60,
        PAYMENT_9_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        )
    ),
    (
        60,
        PAYMENT_9_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    );

-- Payment 10: Gelato in Rome (1 user)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '1000' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        ),
        ITALY_GROUP_ID,
        'Gelato in Rome'
    ) RETURNING id INTO PAYMENT_10_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        10,
        PAYMENT_10_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        )
    );

-- Payment 11: Taxi fare in Venice (2 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '1100' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        ),
        ITALY_GROUP_ID,
        'Taxi fare in Venice'
    ) RETURNING id INTO PAYMENT_11_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        30,
        PAYMENT_11_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    ),
    (
        30,
        PAYMENT_11_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'andrewtest'
        )
    );

-- Payment 12: Tickets to the Colosseum (3 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '1200' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        ),
        ITALY_GROUP_ID,
        'Tickets to the Colosseum'
    ) RETURNING id INTO PAYMENT_12_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        20,
        PAYMENT_12_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'johntesting'
        )
    ),
    (
        20,
        PAYMENT_12_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        )
    ),
    (
        20,
        PAYMENT_12_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    );

-- Payment 13: Pasta making class in Florence (4 users)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '1300' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        ),
        ITALY_GROUP_ID,
        'Pasta making class in Florence'
    ) RETURNING id INTO PAYMENT_13_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        35,
        PAYMENT_13_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'marytester'
        )
    ),
    (
        35,
        PAYMENT_13_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'bentesting'
        )
    ),
    (
        35,
        PAYMENT_13_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'andrewtest'
        )
    ),
    (
        35,
        PAYMENT_13_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    );

-- Payment 14: Cappuccinos at a cafe (1 user)
INSERT INTO
    payments (
        id,
        created_at,
        created_by,
        group_id,
        description
    )
VALUES
    (
        uuid_generate_v4(),
        CURRENT_TIMESTAMP - INTERVAL '1400' minute,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'emilytest'
        ),
        ITALY_GROUP_ID,
        'Cappuccinos at a cafe'
    ) RETURNING id INTO PAYMENT_14_ID;

INSERT INTO
    user_payments (amount, payment_id, user_id)
VALUES
    (
        5,
        PAYMENT_14_ID,
        (
            SELECT
                id
            FROM
                users
            WHERE
                username = 'sarahruns'
        )
    );

END $$;