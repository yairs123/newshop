ALTER TABLE coin_users.support_tickets
    ADD COLUMN admin_reply TEXT,
    ADD COLUMN replied_at TIMESTAMP;
