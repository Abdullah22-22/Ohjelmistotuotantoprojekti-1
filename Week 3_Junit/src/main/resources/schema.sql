CREATE TABLE IF NOT EXISTS temperature_units (
                                                 unit_id INT AUTO_INCREMENT PRIMARY KEY,
                                                 unit_name VARCHAR(20) NOT NULL,
    symbol VARCHAR(5) NOT NULL
    );

CREATE TABLE IF NOT EXISTS temp_records (
                                            record_id INT AUTO_INCREMENT PRIMARY KEY,
                                            input_value DOUBLE NOT NULL,
                                            converted_value DOUBLE NOT NULL,
                                            unit_id INT,
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (unit_id) REFERENCES temperature_units(unit_id)
    );