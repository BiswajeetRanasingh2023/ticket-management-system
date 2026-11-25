CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL
);

CREATE TABLE tickets (
    ticket_id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(customer_id) ON DELETE CASCADE,
    subject VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(20) CHECK (priority IN ('Low', 'Medium', 'High')) DEFAULT 'Medium',
    status VARCHAR(20) CHECK (status IN ('Open', 'In Progress', 'Closed')) DEFAULT 'Open',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO customers (full_name, email, phone) VALUES 
('Rahul Sharma', 'rahul@example.com', '9876543210'),
('Sneha Nair', 'sneha@example.com', '9123456780');

INSERT INTO tickets (customer_id, subject, description, priority, status) VALUES 
(1, 'AC Cooling Issue', 'Split AC not cooling properly', 'High', 'Open'),
(2, 'Washing Machine Installation', 'Need demo and installation', 'Medium', 'In Progress');
