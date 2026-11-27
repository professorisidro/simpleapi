-- Popula a base com 5 clientes e 10 produtos.
--
-- Como rodar:
--   * H2 Console: cole este conteúdo e clique em "Run".
--   * Linha de comando (com a API parada), via H2 RunScript:
--       java -cp ~/.m2/repository/com/h2database/h2/*/h2-*.jar org.h2.tools.RunScript \
--         -url "jdbc:h2:file:./data/simpleapi" -user sa -script scripts/seed.sql
--
-- Usa MERGE (chave de negócio email/sku), então pode rodar mais de uma vez
-- sem duplicar nem dar erro de chave única. O id é IDENTITY (auto-gerado).

-- ===== Clientes =====
MERGE INTO customers (name, email, phone, created_at) KEY(email) VALUES
  ('Ada Lovelace',      'ada.lovelace@example.com',      '+55 11 90000-0001', CURRENT_TIMESTAMP),
  ('Alan Turing',       'alan.turing@example.com',       '+55 11 90000-0002', CURRENT_TIMESTAMP),
  ('Grace Hopper',      'grace.hopper@example.com',       '+55 11 90000-0003', CURRENT_TIMESTAMP),
  ('Linus Torvalds',    'linus.torvalds@example.com',     '+55 11 90000-0004', CURRENT_TIMESTAMP),
  ('Margaret Hamilton', 'margaret.hamilton@example.com',  '+55 11 90000-0005', CURRENT_TIMESTAMP);

-- ===== Produtos =====
MERGE INTO products (name, description, sku, price, stock_quantity) KEY(sku) VALUES
  ('Teclado Mecânico',       'Switch marrom, layout ABNT2', 'KB-001',  249.90, 25),
  ('Mouse Sem Fio',          '6 botões, 1600 DPI',          'MO-002',  129.90, 40),
  ('Monitor 27" 4K',         'IPS, 60Hz',                   'MON-003', 1899.00, 12),
  ('Headset Gamer',          'Surround 7.1',                'HS-004',  349.90, 30),
  ('Webcam Full HD',         '1080p a 30fps',               'WC-005',  199.90, 20),
  ('SSD 1TB NVMe',           'PCIe 4.0',                    'SSD-006', 599.00, 50),
  ('Memória RAM 16GB',       'DDR4 3200MHz',                'RAM-007', 329.00, 60),
  ('Cadeira Ergonômica',     'Apoio lombar ajustável',      'CH-008',  1299.00, 8),
  ('Hub USB-C 7 em 1',       'HDMI, USB3, leitor SD',       'HUB-009', 219.90, 35),
  ('Suporte para Notebook',  'Alumínio, altura ajustável',  'ST-010',  149.90, 45);
