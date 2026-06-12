-- =============================================================
--  UniFinance DB — Schema completo per MariaDB
--  Progetto ISPW — UniFinance (progetto_ispw_marco)
--  Generato leggendo tutti i *DAODB del progetto
--
--  Connessione IntelliJ / HeidiSQL:
--    URL    : jdbc:mysql://localhost:3306/unifinance_db
--    Utente : admin_unifinance
--    Pass   : unifinance
--    Driver : com.mysql.cj.jdbc.Driver  (mysql-connector-j 8.3.0)
-- =============================================================


DROP DATABASE IF EXISTS unifinance_db;
CREATE DATABASE unifinance_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE unifinance_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS messaggio;
DROP TABLE IF EXISTS transazione;
DROP TABLE IF EXISTS wallet_position;
DROP TABLE IF EXISTS virtual_wallet;
DROP TABLE IF EXISTS studente;
DROP TABLE IF EXISTS schoolclass;
DROP TABLE IF EXISTS professore;
DROP USER IF EXISTS 'admin_unifinance'@'localhost';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
--  TABELLE
-- =============================================================

-- -----------------------------------------------------------
-- professore
-- Fonte: ProfessoreDAODB
-- -----------------------------------------------------------
CREATE TABLE professore (
                            email           VARCHAR(150) NOT NULL,
                            nome            VARCHAR(50)  NOT NULL,
                            cognome         VARCHAR(50)  NOT NULL,
                            password_hash   VARCHAR(255)     NULL,   -- NULL per account OAuth
                            auth_provider   VARCHAR(20)  NOT NULL,   -- LOCAL | GOOGLE | MICROSOFT
                            PRIMARY KEY (email)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- schoolclass
-- Fonte: SchoolClassDAODB
-- PK composita: una stessa sigla di classe (es. "1A") può
-- esistere per professori diversi.
-- -----------------------------------------------------------
CREATE TABLE schoolclass (
                             nome             VARCHAR(50)  NOT NULL,
                             professore_email VARCHAR(150) NOT NULL,
                             budget_iniziale  DOUBLE       NOT NULL DEFAULT 0.0,
                             PRIMARY KEY (nome, professore_email),
                             CONSTRAINT fk_sc_professore
                                 FOREIGN KEY (professore_email) REFERENCES professore(email)
                                     ON DELETE RESTRICT
                                     ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- studente
-- Fonte: StudenteDAODB
-- FK composita verso schoolclass: classe + professore_email
-- ON DELETE SET NULL: se la classe viene eliminata lo
-- studente rimane senza classe invece di essere cancellato.
-- -----------------------------------------------------------
CREATE TABLE studente (
                          email            VARCHAR(150) NOT NULL,
                          nome             VARCHAR(50)  NOT NULL,
                          cognome          VARCHAR(50)  NOT NULL,
                          password_hash    VARCHAR(255)     NULL,   -- NULL per account OAuth
                          auth_provider    VARCHAR(20)  NOT NULL,   -- LOCAL | GOOGLE | MICROSOFT
                          classe           VARCHAR(50)      NULL,   -- FK → schoolclass.nome
                          professore_email VARCHAR(150)     NULL,   -- FK → schoolclass.professore_email
                          PRIMARY KEY (email),
                          CONSTRAINT fk_studente_classe
                              FOREIGN KEY (classe, professore_email)
                                  REFERENCES schoolclass(nome, professore_email)
                                  ON DELETE SET NULL
                                  ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- virtual_wallet
-- Fonte: PortafoglioDAODB
-- 1:1 con studente; eliminato a cascata con lo studente.
-- -----------------------------------------------------------
CREATE TABLE virtual_wallet (
                                studente_email    VARCHAR(150) NOT NULL,
                                saldo_disponibile DOUBLE       NOT NULL DEFAULT 0.0,
                                PRIMARY KEY (studente_email),
                                CONSTRAINT fk_vw_studente
                                    FOREIGN KEY (studente_email) REFERENCES studente(email)
                                        ON DELETE CASCADE
                                        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- wallet_position
-- Fonte: WalletPositionDAODB  ← colonne AUTOREVOLI
-- ATTENZIONE [BUG-1]: PortafoglioDAODB usa nomi diversi,
-- vedi nota in testa al file.
-- -----------------------------------------------------------
CREATE TABLE wallet_position (
                                 email_studente        VARCHAR(150) NOT NULL,
                                 simbolo               VARCHAR(15)  NOT NULL,   -- es. AAPL, TSLA
                                 quantita              DOUBLE       NOT NULL,
                                 prezzo_medio_acquisto DOUBLE       NOT NULL,
                                 PRIMARY KEY (email_studente, simbolo),
                                 CONSTRAINT fk_wp_studente
                                     FOREIGN KEY (email_studente) REFERENCES studente(email)
                                         ON DELETE CASCADE
                                         ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- transazione
-- Fonte: TransactionDAODB  ← tabella autorevole = "transazione"
-- ATTENZIONE [BUG-1]: PortafoglioDAODB cerca la tabella
-- "transaction" (parola riservata SQL!) — va corretto.
-- `timestamp` è parola riservata: usare backtick in MySQL.
-- -----------------------------------------------------------
CREATE TABLE transazione (
                             id                INT          NOT NULL AUTO_INCREMENT,
                             email_studente    VARCHAR(150) NOT NULL,
                             simbolo           VARCHAR(15)  NOT NULL,
                             tipo              VARCHAR(10)  NOT NULL,  -- BUY | SELL
                             stato             VARCHAR(10)  NOT NULL DEFAULT 'PENDING', -- DONE | PENDING
                             quantita          DOUBLE       NOT NULL,
                             prezzo_al_momento DOUBLE       NOT NULL,
                             `timestamp`       DATETIME     NOT NULL,
                             PRIMARY KEY (id),
                             CONSTRAINT fk_tr_studente
                                 FOREIGN KEY (email_studente) REFERENCES studente(email)
                                     ON DELETE CASCADE
                                     ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- messaggio
-- Fonte: MessageDAODB
-- mittente/destinatario possono essere studente O professore:
-- nessuna FK singola può coprire entrambe le tabelle, quindi
-- i check di integrità rimangono a livello applicativo.
-- -----------------------------------------------------------
CREATE TABLE messaggio (
                           id                 INT          NOT NULL AUTO_INCREMENT,
                           mittente_email     VARCHAR(150) NOT NULL,
                           destinatario_email VARCHAR(150) NOT NULL,
                           testo              TEXT         NOT NULL,
                           data_invio         DATETIME     NOT NULL,
                           PRIMARY KEY (id)
) ENGINE=InnoDB;


-- =============================================================
--  UTENTE DB
-- =============================================================
CREATE USER IF NOT EXISTS 'admin_unifinance'@'localhost'
    IDENTIFIED BY 'unifinance';
GRANT ALL PRIVILEGES ON unifinance_db.* TO 'admin_unifinance'@'localhost';
FLUSH PRIVILEGES;


-- =============================================================
--  DATI DI ESEMPIO
--  Mirroring dei file CSV del progetto (modalità FILESYSTEM)
--  Hasher.codifica() inverte la stringa:
--    password "ecila123"   → hash "321ecila"
--    password "bob123"     → hash "321bob"
--    password "mario_rossi"→ hash "issor_oiram"
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
--  Professori
-- -----------------------------------------------------------
INSERT INTO professore (email, nome, cognome, password_hash, auth_provider) VALUES
                                                                                ('mario.rossi@univ.it',   'Mario',  'Rossi',   'issor_oiram', 'LOCAL'),
                                                                                ('lucia.bianchi@univ.it', 'Lucia',  'Bianchi', NULL,          'GOOGLE');

-- -----------------------------------------------------------
--  Classi
-- -----------------------------------------------------------
INSERT INTO schoolclass (nome, professore_email, budget_iniziale) VALUES
                                                                      ('1A', 'mario.rossi@univ.it',   90000.00),
                                                                      ('1B', 'lucia.bianchi@univ.it', 10000.00);

-- -----------------------------------------------------------
--  Studenti
-- -----------------------------------------------------------
INSERT INTO studente (email, nome, cognome, password_hash, auth_provider, classe, professore_email) VALUES
                                                                                                        ('alice.verdi@student.it',  'Alice',  'Verdi',  '321ecila', 'LOCAL',     '1A', 'mario.rossi@univ.it'),
                                                                                                        ('bob.neri@student.it',     'Bob',    'Neri',   '321bob',   'LOCAL',     '1A', 'mario.rossi@univ.it'),
                                                                                                        ('carlo.smith@student.it',  'Carlo',  'Smith',  NULL,       'GOOGLE',    '1B', 'lucia.bianchi@univ.it'),
                                                                                                        ('diana.jones@student.it',  'Diana',  'Jones',  NULL,       'MICROSOFT', '1B', 'lucia.bianchi@univ.it'),
-- studenti pre-aggiunti dal professore (senza account ancora registrato)
                                                                                                        ('peppino.olanda@univ.it',  '—',      '—',      NULL,       'LOCAL',     '1A', 'mario.rossi@univ.it'),
                                                                                                        ('ciccio.caputo@univ.it',   '—',      '—',      NULL,       'LOCAL',     '1A', 'mario.rossi@univ.it');

-- -----------------------------------------------------------
--  Virtual Wallet
-- -----------------------------------------------------------
INSERT INTO virtual_wallet (studente_email, saldo_disponibile) VALUES
                                                                   ('alice.verdi@student.it', 84094.25),
                                                                   ('bob.neri@student.it',    90000.00),
                                                                   ('carlo.smith@student.it',     0.00),
                                                                   ('diana.jones@student.it',     0.00);

-- -----------------------------------------------------------
--  Wallet Positions  (solo alice ha posizioni aperte)
-- -----------------------------------------------------------
INSERT INTO wallet_position (email_studente, simbolo, quantita, prezzo_medio_acquisto) VALUES
                                                                                           ('alice.verdi@student.it', 'AAPL',  5.0, 307.34),
                                                                                           ('alice.verdi@student.it', 'TSLA',  2.0, 391.00),
                                                                                           ('alice.verdi@student.it', 'MSFT',  3.0, 416.67),
                                                                                           ('alice.verdi@student.it', 'GOOGL', 4.0, 368.53),
                                                                                           ('alice.verdi@student.it', 'BA',    4.0, 215.73);

-- -----------------------------------------------------------
--  Transazioni  (corrispondono agli acquisti in posizioni.csv)
-- -----------------------------------------------------------
INSERT INTO transazione (email_studente, simbolo, tipo, stato, quantita, prezzo_al_momento, `timestamp`) VALUES
                                                                                                             ('alice.verdi@student.it', 'AAPL',  'BUY', 'DONE', 5.0, 307.34, '2026-06-10 20:00:00'),
                                                                                                             ('alice.verdi@student.it', 'TSLA',  'BUY', 'DONE', 2.0, 391.00, '2026-06-10 20:01:00'),
                                                                                                             ('alice.verdi@student.it', 'MSFT',  'BUY', 'DONE', 3.0, 416.67, '2026-06-10 20:02:00'),
                                                                                                             ('alice.verdi@student.it', 'GOOGL', 'BUY', 'DONE', 4.0, 368.53, '2026-06-10 20:03:00'),
                                                                                                             ('alice.verdi@student.it', 'BA',    'BUY', 'DONE', 4.0, 215.73, '2026-06-10 20:04:00');

-- -----------------------------------------------------------
--  Messaggi
-- -----------------------------------------------------------
INSERT INTO messaggio (mittente_email, destinatario_email, testo, data_invio) VALUES
                                                                                  ('alice.verdi@student.it',  'mario.rossi@univ.it',   'professore, grazie',       '2026-06-10 20:51:15'),
                                                                                  ('mario.rossi@univ.it',     'alice.verdi@student.it','gentilissima, ricevuto',   '2026-06-10 20:55:43'),
                                                                                  ('alice.verdi@student.it',  'mario.rossi@univ.it',   'viva gianpaolo',           '2026-06-10 22:30:52');

SET FOREIGN_KEY_CHECKS = 1;