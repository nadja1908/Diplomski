COPY katedra (id, sifra, naziv) FROM stdin;
1	KiI	Katedra za računarstvo i informatiku
2	AU	Katedra za automatiku
3	EL	Katedra za elektroniku
4	TK	Katedra za telekomunikacije
\.
SELECT setval(pg_get_serial_sequence('katedra','id'), (SELECT MAX(id) FROM katedra));
COPY studijski_program (id, sifra, naziv, stepen, katedra_id) FROM stdin;
1	RI	Računarska i informaciona tehnologija	Osnovne akademske studije	1
2	SI	Softversko inženjerstvo	Osnovne akademske studije	1
3	AUT	Automatika i upravljanje sistemima	Osnovne akademske studije	2
4	EE	Elektroenergetika	Osnovne akademske studije	3
5	RT	Radiokomunikacije	Osnovne akademske studije	4
6	TK	Telekomunikacije	Master akademske studije	4
\.
SELECT setval(pg_get_serial_sequence('studijski_program','id'), (SELECT MAX(id) FROM studijski_program));
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (1, 'sef.kii@ftn.rs', '$2b$10$vc1ToHEBEXFou8UYqUv8LOShiRUdZHFZ9U9bvjStdXatluyXUdR06', 'Marko', 'Petrović', 'SEF_KATEDRE');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (2, 'sef.au@ftn.rs', '$2b$10$vc1ToHEBEXFou8UYqUv8LOShiRUdZHFZ9U9bvjStdXatluyXUdR06', 'Ana', 'Nikolić', 'SEF_KATEDRE');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (3, 'sef.el@ftn.rs', '$2b$10$vc1ToHEBEXFou8UYqUv8LOShiRUdZHFZ9U9bvjStdXatluyXUdR06', 'Jovan', 'Jovanović', 'SEF_KATEDRE');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (4, 'sef.tk@ftn.rs', '$2b$10$vc1ToHEBEXFou8UYqUv8LOShiRUdZHFZ9U9bvjStdXatluyXUdR06', 'Milica', 'Stanković', 'SEF_KATEDRE');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (5, 'prof01@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Milan', 'Ilić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (6, 'prof02@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Jelena', 'Đorđević', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (7, 'prof03@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Stefan', 'Popović', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (8, 'prof04@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Ivana', 'Đurić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (9, 'prof05@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Nikola', 'Kostić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (10, 'prof06@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Tamara', 'Marković', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (11, 'prof07@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Milan', 'Ilić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (12, 'prof08@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Jelena', 'Đorđević', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (13, 'prof09@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Stefan', 'Popović', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (14, 'prof10@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Ivana', 'Đurić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (15, 'prof11@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Nikola', 'Kostić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (16, 'prof12@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Tamara', 'Marković', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (17, 'prof13@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Milan', 'Ilić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (18, 'prof14@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Jelena', 'Đorđević', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (19, 'prof15@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Stefan', 'Popović', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (20, 'prof16@ftn.rs', '$2b$10$WE1hz8pmOEpgyyrL46VUjusxg6KdhaCOCl36s/96llBdhrah1XNoC', 'Ivana', 'Đurić', 'PROFESOR');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (21, 'student001@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (1, 21, 'RI 001/25', 1, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (22, 'student002@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (2, 22, 'SI 002/22', 2, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (23, 'student003@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (3, 23, 'AUT 003/25', 3, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (24, 'student004@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (4, 24, 'EE 004/24', 4, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (25, 'student005@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (5, 25, 'RT 005/25', 5, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (26, 'student006@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (6, 26, 'TK 006/21', 6, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (27, 'student007@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (7, 27, 'RI 007/22', 1, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (28, 'student008@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (8, 28, 'SI 008/23', 2, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (29, 'student009@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (9, 29, 'AUT 009/24', 3, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (30, 'student010@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (10, 30, 'EE 010/25', 4, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (31, 'student011@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (11, 31, 'RT 011/21', 5, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (32, 'student012@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (12, 32, 'TK 012/22', 6, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (33, 'student013@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (13, 33, 'RI 013/23', 1, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (34, 'student014@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (14, 34, 'SI 014/24', 2, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (35, 'student015@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (15, 35, 'AUT 015/25', 3, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (36, 'student016@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (16, 36, 'EE 016/21', 4, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (37, 'student017@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (17, 37, 'RT 017/22', 5, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (38, 'student018@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (18, 38, 'TK 018/23', 6, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (39, 'student019@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (19, 39, 'RI 019/24', 1, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (40, 'student020@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (20, 40, 'SI 020/25', 2, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (41, 'student021@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (21, 41, 'AUT 021/21', 3, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (42, 'student022@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (22, 42, 'EE 022/22', 4, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (43, 'student023@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (23, 43, 'RT 023/23', 5, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (44, 'student024@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (24, 44, 'TK 024/24', 6, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (45, 'student025@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (25, 45, 'RI 025/25', 1, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (46, 'student026@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (26, 46, 'SI 026/21', 2, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (47, 'student027@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (27, 47, 'AUT 027/22', 3, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (48, 'student028@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (28, 48, 'EE 028/23', 4, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (49, 'student029@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (29, 49, 'RT 029/24', 5, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (50, 'student030@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (30, 50, 'TK 030/25', 6, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (51, 'student031@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (31, 51, 'RI 031/21', 1, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (52, 'student032@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (32, 52, 'SI 032/22', 2, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (53, 'student033@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (33, 53, 'AUT 033/23', 3, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (54, 'student034@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (34, 54, 'EE 034/24', 4, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (55, 'student035@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (35, 55, 'RT 035/25', 5, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (56, 'student036@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (36, 56, 'TK 036/21', 6, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (57, 'student037@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (37, 57, 'RI 037/22', 1, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (58, 'student038@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (38, 58, 'SI 038/23', 2, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (59, 'student039@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (39, 59, 'AUT 039/24', 3, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (60, 'student040@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (40, 60, 'EE 040/25', 4, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (61, 'student041@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (41, 61, 'RT 041/21', 5, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (62, 'student042@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (42, 62, 'TK 042/22', 6, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (63, 'student043@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (43, 63, 'RI 043/23', 1, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (64, 'student044@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (44, 64, 'SI 044/24', 2, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (65, 'student045@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (45, 65, 'AUT 045/25', 3, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (66, 'student046@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (46, 66, 'EE 046/21', 4, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (67, 'student047@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (47, 67, 'RT 047/22', 5, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (68, 'student048@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (48, 68, 'TK 048/23', 6, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (69, 'student049@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (49, 69, 'RI 049/24', 1, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (70, 'student050@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (50, 70, 'SI 050/25', 2, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (71, 'student051@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (51, 71, 'AUT 051/21', 3, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (72, 'student052@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (52, 72, 'EE 052/22', 4, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (73, 'student053@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (53, 73, 'RT 053/23', 5, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (74, 'student054@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (54, 74, 'TK 054/24', 6, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (75, 'student055@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (55, 75, 'RI 055/25', 1, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (76, 'student056@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (56, 76, 'SI 056/21', 2, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (77, 'student057@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (57, 77, 'AUT 057/22', 3, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (78, 'student058@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (58, 78, 'EE 058/23', 4, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (79, 'student059@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (59, 79, 'RT 059/24', 5, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (80, 'student060@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (60, 80, 'TK 060/25', 6, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (81, 'student061@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (61, 81, 'RI 061/21', 1, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (82, 'student062@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (62, 82, 'SI 062/22', 2, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (83, 'student063@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (63, 83, 'AUT 063/23', 3, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (84, 'student064@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (64, 84, 'EE 064/24', 4, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (85, 'student065@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (65, 85, 'RT 065/25', 5, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (86, 'student066@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (66, 86, 'TK 066/21', 6, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (87, 'student067@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (67, 87, 'RI 067/22', 1, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (88, 'student068@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (68, 88, 'SI 068/23', 2, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (89, 'student069@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (69, 89, 'AUT 069/24', 3, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (90, 'student070@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (70, 90, 'EE 070/25', 4, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (91, 'student071@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (71, 91, 'RT 071/21', 5, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (92, 'student072@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (72, 92, 'TK 072/22', 6, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (93, 'student073@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (73, 93, 'RI 073/23', 1, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (94, 'student074@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (74, 94, 'SI 074/24', 2, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (95, 'student075@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (75, 95, 'AUT 075/25', 3, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (96, 'student076@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (76, 96, 'EE 076/21', 4, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (97, 'student077@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (77, 97, 'RT 077/22', 5, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (98, 'student078@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (78, 98, 'TK 078/23', 6, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (99, 'student079@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (79, 99, 'RI 079/24', 1, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (100, 'student080@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (80, 100, 'SI 080/25', 2, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (101, 'student081@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (81, 101, 'AUT 081/21', 3, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (102, 'student082@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (82, 102, 'EE 082/22', 4, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (103, 'student083@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (83, 103, 'RT 083/23', 5, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (104, 'student084@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (84, 104, 'TK 084/24', 6, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (105, 'student085@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (85, 105, 'RI 085/25', 1, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (106, 'student086@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (86, 106, 'SI 086/21', 2, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (107, 'student087@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (87, 107, 'AUT 087/22', 3, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (108, 'student088@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (88, 108, 'EE 088/23', 4, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (109, 'student089@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (89, 109, 'RT 089/24', 5, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (110, 'student090@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (90, 110, 'TK 090/25', 6, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (111, 'student091@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (91, 111, 'RI 091/21', 1, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (112, 'student092@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (92, 112, 'SI 092/22', 2, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (113, 'student093@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Bogdanović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (93, 113, 'AUT 093/23', 3, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (114, 'student094@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (94, 114, 'EE 094/24', 4, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (115, 'student095@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (95, 115, 'RT 095/25', 5, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (116, 'student096@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (96, 116, 'TK 096/21', 6, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (117, 'student097@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (97, 117, 'RI 097/22', 1, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (118, 'student098@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (98, 118, 'SI 098/23', 2, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (119, 'student099@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (99, 119, 'AUT 099/24', 3, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (120, 'student100@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (100, 120, 'EE 100/25', 4, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (121, 'student101@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (101, 121, 'RT 101/21', 5, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (122, 'student102@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (102, 122, 'TK 102/22', 6, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (123, 'student103@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (103, 123, 'RI 103/23', 1, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (124, 'student104@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (104, 124, 'SI 104/24', 2, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (125, 'student105@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (105, 125, 'AUT 105/25', 3, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (126, 'student106@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (106, 126, 'EE 106/21', 4, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (127, 'student107@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (107, 127, 'RT 107/22', 5, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (128, 'student108@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (108, 128, 'TK 108/23', 6, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (129, 'student109@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Hana', 'Milić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (109, 129, 'RI 109/24', 1, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (130, 'student110@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Uroš', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (110, 130, 'SI 110/25', 2, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (131, 'student111@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (111, 131, 'AUT 111/21', 3, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (132, 'student112@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Vuk', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (112, 132, 'EE 112/22', 4, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (133, 'student113@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (113, 133, 'RT 113/23', 5, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (134, 'student114@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Maša', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (114, 134, 'TK 114/24', 6, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (135, 'student115@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Luka', 'Tomić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (115, 135, 'RI 115/25', 1, 2025);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (136, 'student116@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Filip', 'Simić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (116, 136, 'SI 116/21', 2, 2021);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (137, 'student117@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Ristić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (117, 137, 'AUT 117/22', 3, 2022);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (138, 'student118@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Pavić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (118, 138, 'EE 118/23', 4, 2023);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (139, 'student119@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Teodora', 'Radović', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (119, 139, 'RT 119/24', 5, 2024);
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (140, 'student120@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Sara', 'Čukić', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (120, 140, 'TK 120/25', 6, 2025);
-- Dodatni RI (program 1) za godine upisa 2022–2024 (demo statistike / kohorte)
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (141, 'student121@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Nemanja', 'Kovacevic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (142, 'student122@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Jovana', 'Milosevic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (143, 'student123@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Petar', 'Lazic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (144, 'student124@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Milena', 'Obradovic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (145, 'student125@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Andrej', 'Stojanovic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (146, 'student126@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Katarina', 'Vasic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (147, 'student127@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Bogdan', 'Mitic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (148, 'student128@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Ivana', 'Peric', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (149, 'student129@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Stefan', 'Jankovic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (150, 'student130@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Tijana', 'Dukic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (151, 'student131@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Lazar', 'Todorovic', 'STUDENT');
INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES (152, 'student132@ftn.rs', '$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q', 'Ana', 'Zivanovic', 'STUDENT');
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (121, 141, 'RI 121/22', 1, 2022);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (122, 142, 'RI 122/22', 1, 2022);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (123, 143, 'RI 123/22', 1, 2022);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (124, 144, 'RI 124/22', 1, 2022);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (125, 145, 'RI 125/23', 1, 2023);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (126, 146, 'RI 126/23', 1, 2023);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (127, 147, 'RI 127/23', 1, 2023);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (128, 148, 'RI 128/23', 1, 2023);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (129, 149, 'RI 129/24', 1, 2024);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (130, 150, 'RI 130/24', 1, 2024);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (131, 151, 'RI 131/24', 1, 2024);
INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES (132, 152, 'RI 132/24', 1, 2024);
SELECT setval(pg_get_serial_sequence('korisnik','id'), (SELECT MAX(id) FROM korisnik));
SELECT setval(pg_get_serial_sequence('student','id'), (SELECT MAX(id) FROM student));
INSERT INTO sef_katedre (korisnik_id, katedra_id) VALUES (1, 1);
INSERT INTO sef_katedre (korisnik_id, katedra_id) VALUES (2, 2);
INSERT INTO sef_katedre (korisnik_id, katedra_id) VALUES (3, 3);
INSERT INTO sef_katedre (korisnik_id, katedra_id) VALUES (4, 4);
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (1, 5, 1, 'vanredni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (2, 6, 2, 'redovni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (3, 7, 3, 'docent');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (4, 8, 4, 'vanredni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (5, 9, 1, 'redovni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (6, 10, 2, 'docent');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (7, 11, 3, 'vanredni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (8, 12, 4, 'redovni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (9, 13, 1, 'docent');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (10, 14, 2, 'vanredni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (11, 15, 3, 'redovni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (12, 16, 4, 'docent');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (13, 17, 1, 'vanredni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (14, 18, 2, 'redovni profesor');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (15, 19, 3, 'docent');
INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES (16, 20, 4, 'vanredni profesor');
SELECT setval(pg_get_serial_sequence('profesor','id'), (SELECT MAX(id) FROM profesor));
INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES 
(25, '13R01', 'Uvod u računarsku nauku i profesiju', 6, 1, 1, 'Disciplina, istorija računarstva, uloga softverskog inženjera (2. godina RI).', 1, 1),
(26, '13R02', 'Matematička logika i dokazivanje', 7, 1, 1, 'Iskazna logika, dokaz, priprema za teoriju računarstva (2. godina RI).', 1, 1),
(27, '13R03', 'Linearna algebra za informatičare', 8, 1, 1, 'Vektorski prostori, matrice, linearni sistemi u CS primenama (2. godina RI).', 1, 1),
(28, '13R04', 'Verovatnoća i statistika', 6, 1, 1, 'Osnovni modeli, statistička inferencija i rad sa podacima (2. godina RI).', 1, 1),
(29, '13R05', 'Elektrotehničke osnove informatike', 7, 1, 1, 'Digitalna logika, signali, osnovni sklopovi i binarna aritmetika (2. godina RI).', 1, 1),
(30, '13R06', 'Rad u Unix/Linux okruženju', 8, 1, 1, 'Školjka, procesi, dozvole, skripte i alati komandne linije (2. godina RI).', 1, 1),
(31, '13R07', 'Dokumentovanje softverskih projekata', 6, 1, 1, 'Git, Markdown, dijagrami i pravilna tehnička komunikacija (2. godina RI).', 1, 2),
(32, '13R08', 'Seminarski rad iz programiranja', 7, 1, 1, 'Samostalan mali projekat, izveštaj i odbrana rezultata (2. godina RI).', 1, 2),
(33, '13R09', 'Programske paradigme i jezici', 8, 1, 1, 'Poređenje imperativnog, funkcionalnog i deklarativnog stila (2. godina RI).', 1, 2),
(34, '13R10', 'Kompjuterska arhitektura', 6, 1, 1, 'CPU, memorijska hijerarhija, pipeline i performanse (2. godina RI).', 1, 2),
(35, '13R11', 'Teorija automata i formalnih jezika', 7, 1, 1, 'Konačni automati, regularni jezici, gramatike, uvod u izračunljivost (3. godina RI).', 1, 2),
(36, '13R12', 'Numerička matematika za inženjere', 8, 1, 1, 'Mašinska aritmetika, greške, stabilne numeričke metode (3. godina RI).', 2, 1),
(37, '13R13', 'Upravljanje memorijom u jezicima niskog nivoa', 6, 1, 1, 'Stack/heap, pokazivači, alati za proveru curenja memorije (3. godina RI).', 2, 1),
(38, '13R14', 'Paralelizam na nivou instrukcija', 7, 1, 1, 'SIMD, uvod u OpenMP, zakon Amdahla (3. godina RI).', 2, 1),
(39, '13R15', 'Konkurentno programiranje', 8, 1, 1, 'Niti, sinhronizacija, deadlock i utrkivanje (3. godina RI).', 2, 1),
(40, '13R16', 'Uvod u grafičke sisteme', 6, 1, 1, 'Grafički pipeline, rasterizacija, osnovni API (3. godina RI).', 2, 1),
(41, '13R17', 'Razvoj skalabilnih veb servisa', 7, 1, 1, 'REST, autentikacija, sesije, greške i rast opterećenja (3. godina RI).', 2, 1),
(42, '13R18', 'Napredne baze podataka u praksi', 8, 1, 1, 'Indeksi, izolacija transakcija, plan izvršenja i optimizacija (3. godina RI).', 2, 2),
(43, '13R19', 'Bezbednost aplikacija i mreže', 6, 1, 1, 'OWASP, hardening, mrežni i aplikacioni sloj (3. godina RI).', 2, 2),
(44, '13R20', 'Strategije testiranja softvera', 7, 1, 1, 'Unit, integracioni testovi, mocking i TDD (3. godina RI).', 2, 2),
(45, '13R21', 'DevOps i neprekidna isporuka', 8, 1, 1, 'CI/CD, konfiguracija kao kod, kontejneri u timu (4. godina RI).', 2, 2),
(46, '13R22', 'Obrada prirodnog jezika', 6, 1, 1, 'Tokenizacija, ugrađivanja, jednostavni klasifikatori teksta (4. godina RI).', 2, 2),
(47, '13R23', 'Ugrađeni i real-time sistemi', 7, 1, 1, 'Mikrokontroleri, senzori, ograničenja u realnom vremenu (4. godina RI).', 3, 1),
(48, '13R24', 'Timski projekat – analiza i arhitektura', 8, 1, 1, 'Prikupljanje zahteva, dizajn rešenja i dokumentacija (4. godina RI).', 3, 1),
(49, '13R25', 'Produkcija i održavanje softvera', 6, 1, 1, 'Logovanje, metrike, strategije izdavanja i otklanjanja kvarova (4. godina RI).', 3, 1),
(50, '13R26', 'Pravo intelektualne svojine u IT', 7, 1, 1, 'Licence, autorsko pravo, patenti i otvoreni kod (4. godina RI).', 3, 1),
(51, '13R27', 'Etika i privatnost podataka', 8, 1, 1, 'GDPR/LPD perspektiva, pristanak i minimizacija podataka (4. godina RI).', 3, 1),
(52, '13R28', 'Upravljanje znanjem u organizacijama', 6, 1, 1, 'Baze znanja, wiki procesi i prenos ekspertize (4. godina RI).', 3, 1),
(53, '13R29', 'Raspodeljeni algoritmi i konsenzus', 7, 1, 1, 'Klasteri, pouzdanost pod podelama, uvod u konsenzus protokole (4. godina RI).', 3, 2),
(54, '13R30', 'Priprema diplomskog rada', 8, 1, 1, 'Literatura, metodologija istraživanja i plan izrade rada (4. godina RI).', 3, 2),
(1, '13S001', 'Matematika 1', 8, 1, 1, 'Algebra, realne funkcije i diferencijalni račun.', 3, 2),
(2, '13S002', 'Programiranje 1', 8, 1, 1, 'Uvod u algoritme, strukture podataka i Python/C.', 3, 2),
(3, '13S021', 'Diskretne strukture', 6, 1, 1, 'Relacije, kombinatorika i osnovi teorije grafova.', 3, 2),
(4, '13S031', 'Objektno orijentisano programiranje', 8, 1, 1, 'SOLID, OOP dizajn i praksa: Java, C++, Python.', 4, 1),
(5, '13S032', 'Strukture i algoritmi', 8, 1, 1, 'Analiza složenosti, pretrage, sortiranje, grafovi.', 4, 1),
(6, '13S033', 'Operativni sistemi', 8, 1, 1, 'Procesi, niti, memorija i fajl sistemi (UNIX/Windows).', 4, 1),
(7, '13S041', 'Baze podataka', 8, 1, 1, 'SQL, normalizacija, transakcije; NoSQL uvod.', 4, 1),
(8, '13S042', 'Računarske mreže', 8, 1, 1, 'TCP/IP, HTTP, rute, bežične mreže.', 4, 1),
(9, '13S051', 'Paralelno programiranje', 6, 1, 1, 'OpenMP, CUDA, raspodela posla i metrike performansi.', 4, 2),
(10, '13S052', 'Verifikacija softvera', 6, 1, 1, 'Testiranje, formalne metode, property testing.', 4, 2),
(20, '13S062', 'Sigurnost informacija', 6, 1, 1, 'Kriptografija, OAuth, OWASP, bezbednost mreža.', 4, 2),
(24, '13S073', 'Informacioni sistemi', 6, 1, 1, 'ER modeli, integracija, BI i veliki podaci.', 4, 2);
INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES 
(25, 'Cilj predmeta Uvod u računarsku nauku i profesiju je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da prepozna uloge u industriji, koristi osnovnu literaturu i primeni pojmove iz uvoda u projekatne i diskusione zadatke.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Istorija računarstva, reprezentacija podataka, hardver i softver, profesionalna etika, uvod u studijski program i očekivane kompetencije.'),
(26, 'Cilj predmeta Matematička logika i dokazivanje je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da formuliše iskaze, prati dokaze i primeni pravila izvoda na jednostavne probleme relevantne za teoriju računarstva.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Iskazna logika, istinitosne tablice, prirodna dedukcija, kontradikcija, uvod u rezoluciju i vezu sa automatima i dokazivačima.'),
(27, 'Cilj predmeta Linearna algebra za informatičare je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da rešava linearne sisteme, tumči matrice u kontekstu grafova i algoritama i primeni sopstvene vrednosti na jednostavnim modelima.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Vektorski prostori, linearna nezavisnost, determinante, sopstveni vektori; veza sa kompresijom, PageRank (uvod) i regresijom.'),
(28, 'Cilj predmeta Verovatnoća i statistika je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da modelira slučajne veličine, tumči raspodele i interval poverenja u jednostavnim eksperimentima sa podacima.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Diskretne i kontinualne raspodele, očekivanje, nezavisnost, centralni granični teorem (ideja), osnovna statistička inferencija i vizualizacija.'),
(29, 'Cilj predmeta Elektrotehničke osnove informatike je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da tumči jednostavne sklopove, nivoe logičkih signala i osnovnu vezu između fizičkog i digitalnog sloja računara.', 'Predavanja, vežbe, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Binarna aritmetika, logička kola, multipleksori, flip-flopovi, ADC/DAC na konceptualnom nivou, buka i pouzdanost (uvod).'),
(30, 'Cilj predmeta Rad u Unix/Linux okruženju je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da koristi školjku, upravlja procesima i dozvolama te napiše jednostavne Bash skripte za automatizaciju.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Fajl sistem, korisnici i grupe, pipeline, grep/sed/awk (osnove), procesi, signali, cron i servisi na konceptualnom nivou.'),
(31, 'Cilj predmeta Dokumentovanje softverskih projekata je da student savlada teorijske i praktične osnove neophodne za dalji nastavak studija i timski rad.', 'Student je sposoban da vodi grananje u Git-u, piše README i tehničke izveštaje i pravi čitke dijagrame tokova i komponenti.', 'Predavanja, vežbe, projektni zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Semantičko verzionisanje, merge i rebase (uvod), MR/PR praksa, Markdown, UML ili Mermaid dijagrami i šabloni izveštaja kvaliteta.'),
(32, 'Cilj predmeta Seminarski rad iz programiranja je da student savlada teorijske i praktične osnove kroz mali samostalan projekat sa odbranom.', 'Student je sposoban da prevede zahteve u dizajn, implementira rešenje, testira ga i predstavi rezultat stručnoj publici.', 'Konsultacije, samostalan rad, odbrana seminarskog rada. Akcenat na aktivnom učenju i feedback-u.', 'Formulisanje teme, izbor alata, iterativna implementacija, jednostavno testiranje, pisanje kratkog izveštaja i demonstracija rada.'),
(33, 'Cilj predmeta Programske paradigme i jezici je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da uporedi imperativni, funkcionalni i deklarativni stil i izabere odgovarajući pristup za mali zadatak.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Nepromenljivost, funkcije višeg reda, pattern matching (uvod), lenjo računanje (ideja), primeri u bar dva jezika ili paradigme.'),
(34, 'Cilj predmeta Kompjuterska arhitektura je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da čita dijagram blokova CPU-a, proceni uticaj keša i objasni zašto pojedini kod troši više ciklusa.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'ISA i asemblerski uvod, aritmetičko-logička jedinica, kontrola toka, hijerarhija memorije, keš, grananje uz hazard, performanse (MIPS/FLOPS ideja).'),
(35, 'Cilj predmeta Teorija automata i formalnih jezika je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da konstruiše automate, dokazuje jednostavna svojstva jezika i razume granice izračunljivosti.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Deterministički i nedeterministički konačni automati, regularni izrazi, Pumping lemma (uvod), kontekstno slobodne gramatike, hijerarhija Čomskog, neodlučivost (motivacija).'),
(36, 'Cilj predmeta Numerička matematika za inženjere je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da proceni grešku diskretizacije, bira jednostavnu numeričku šemu i implementira je u vežbama.', 'Predavanja, vežbe, individualni i timski domaći zadaci, računski zadaci i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Greška zaokruživanja, stabilnost, LU i iterativne metode (uvod), nelinearne jednačine, numerička integracija i diferenciranje (osnove).'),
(37, 'Cilj predmeta Upravljanje memorijom u jezicima niskog nivoa je da student savlada teorijske i praktične osnove neophodne za siguran rad sa pokazivačima i heap-om.', 'Student je sposoban da debuguje curenje memorije na malom programu i da koristi alate kao što su Valgrind ili sanitizers na osnovnom nivou.', 'Predavanja, vežbe, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Stack i heap, životni vek objekata, dinamička alokacija, tipične greške (use-after-free, double free), uvod u profilisanje memorije.'),
(38, 'Cilj predmeta Paralelizam na nivou instrukcija je da student savlada teorijske i praktične osnove vektorskog ubrzavanja petlji.', 'Student je sposoban da prepozna mogućnosti za SIMD/OpenMP na jednostavnim petljama i izmeri ubrzanje.', 'Predavanja, laboratorijske vežbe, individualni zadaci i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Data parallelizam, vektorski registri, direktive OpenMP za petlje, zakon Amdahla, merenje performansi i false sharing (uvod).'),
(39, 'Cilj predmeta Konkurentno programiranje je da student savlada teorijske i praktične osnove sinhronizacije niti i procesa.', 'Student je sposoban da implementira zaštitu deljenih resursa, izbegne tipične deadlock situacije i koristi uslovne promenljive.', 'Predavanja, vežbe, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Modeli memorije (intuitivno), muteksi, semafori, monitori, utrkivanje i sporije, thread pool (uvod), motivacija za async I/O.'),
(40, 'Cilj predmeta Uvod u grafičke sisteme je da student savlada teorijske i praktične osnove prikaza 3D scena u realnom vremenu.', 'Student je sposoban da primeni matrice transformacija, projekciju i osnovni rasterizacioni tok na jednostavnoj sceni.', 'Predavanja, vežbe, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Koordinatni sistemi, homogene koordinate, svetlo i materijal (Phong uvod), teksture, Z-bafer, anti-aliasing (ideja).'),
(41, 'Cilj predmeta Razvoj skalabilnih veb servisa je da student savlada teorijske i praktične osnove projektovanja HTTP API-ja otpornih na opterećenje.', 'Student je sposoban da dizajnira REST resurse, rukuje autentikacijom i greškama te predloži jednostavno keširanje ili paginaciju.', 'Predavanja, vežbe, timski projektni zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Resursi i statusni kodovi, idempotentnost, JWT/sesije, limitiranje zahteva (uvod), osnovna analiza uskog grla pod opterećenjem (alat ili skripta).'),
(42, 'Cilj predmeta Napredne baze podataka u praksi je da student savlada teorijske i praktične osnove performansi i transakcione konzistentnosti.', 'Student je sposoban da tumči plan izvršenja upita, bira indekse i razume izolacione nivoe i rizik od mrtve blokade.', 'Predavanja, laboratorijske vežbe, individualni i timski zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Indeksi i statistika optimizatora, EXPLAIN, zaključavanje, replikacija (uvod), read/write podela (motivacija), jednostavni monitoring sporih upita.'),
(43, 'Cilj predmeta Bezbednost aplikacija i mreže je da student savlada teorijske i praktične osnove prevencije uobičajenih napada.', 'Student je sposoban da primeni OWASP smernice na mali veb projekat i da konfiguriše osnovnu mrežnu i TLS zaštitu.', 'Predavanja, vežbe, laboratorija sa CTF zadacima lakoće osnovnog nivoa, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'SQLi, XSS, CSRF, kodiranje izlaza, kontrola pristupa modelu, TLS i cipher suite (uvod), logovanje bezbednosnih događaja.'),
(44, 'Cilj predmeta Strategije testiranja softvera je da student savlada teorijske i praktične osnove test piramide i održivih testova.', 'Student je sposoban da piše unit i integracione testove sa mock-ovima i da ih uključi u pipeline.', 'Predavanja, vežbe, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Pokrivenost koda, fixture-i, property-based testiranje (uvod), flaky testovi i stabilizacija, TDD kao praksa na malom primeru.'),
(45, 'Cilj predmeta DevOps i neprekidna isporuka je da student savlada teorijske i praktične osnove automatizacije isporuke softvera.', 'Student je sposoban da definiše korake CI/CD, upravlja tajnama na konceptualnom nivou i pokrene kontejnerizovanu uslugu u razvoju.', 'Predavanja, laboratorijske vežbe, projektni zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Build artefakti, test u pipeline-u, immutable infrastruktura (ideja), Docker Compose (uvod), monitoring i health check-ovi u razvoju.'),
(46, 'Cilj predmeta Obrada prirodnog jezika je da student savlada teorijske i praktične osnove radnog toka obrade teksta.', 'Student je sposoban da izgradi jednostavan pipeline tokenizacije, vektorizacije i evaluacije klasifikatora na označenom skupu.', 'Predavanja, vežbe, individualni i timski domaći zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Predobrada teksta, TF-IDF, Bag-of-words, jednostavni klasifikatori, word2vec (motivacija), mere performansi (precision/recall/F1).'),
(47, 'Cilj predmeta Ugrađeni i real-time sistemi je da student savlada teorijske i praktične osnove programiranja sa vremenskim i energetskim ograničenjima.', 'Student je sposoban da konfiguriše GPIO i prekide na jednostavnoj platformi i da objasni prioritet prekida i štednju energije.', 'Predavanja, vežbe, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Mikrokontroleri, senzori, protokoli I2C/SPI (uvod), latencija i determinizam, watch-dog i štednja energije, osnovni bezbednosni aspekti IoT-a.'),
(48, 'Cilj predmeta Timski projekat – analiza i arhitektura je da student savlada teorijske i praktične osnove prikupljanja zahteva i arhitektonskog dizajna.', 'Student je sposoban da vodi intervjue sa stejkholderima, definiše user stories i predloži arhitektonske komponente sa rizicima.', 'Predavanja, timski rad uz mentora, prezentacija dizajna, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'SRS na agilan način, NFR, C4 ili slični dijagrami, tehnički dug (uvod), plan iteracija i kriterijumi prihvatanja.'),
(49, 'Cilj predmeta Produkcija i održavanje softvera je da student savlada teorijske i praktične osnove posmatranja sistema u produkciji.', 'Student je sposoban da poveže logove i metrike, definiše SLO i predloži rollback strategiju na nivou vežbe.', 'Predavanja, studije slučaja, laboratorijske simulacije, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Strukturirani logovi, tragovi, metrike i alarmi, kanari i blue-green (uvod), postmortem bez okrivljavanja, održavanje kao kontinuitet.'),
(50, 'Cilj predmeta Pravo intelektualne svojine u IT je da student savlada teorijske i praktične osnove licence i zaštite intelektualne svojine.', 'Student je sposoban da izabere licencu za sopstveni projekat i da prepozna rizike NDA i kršenja licence zavisnosti.', 'Predavanja, vežbe, diskusije studija slučaja, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Autorsko pravo, AGPL/GPL/MIT/BSD (uvod), patenti na softver (ideja), otvoreni kod u korporaciji, pregled uzoraka ugovora.'),
(51, 'Cilj predmeta Etika i privatnost podataka je da student savlada teorijske i praktične osnove etike i usklađenosti sa propisima o ličnim podacima.', 'Student je sposoban da proceni lawful basis, primeni minimizaciju i dokumentuje obradu u jednostavnom studiju slučaja.', 'Predavanja, diskusije, vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'GDPR/LPD principi, DPIA (uvod), pristanak i transparentnost, pseudonimizacija, etički review i pristrasnost modela (motivacija).'),
(52, 'Cilj predmeta Upravljanje znanjem u organizacijama je da student savlada teorijske i praktične osnove organizacije tehničkog znanja.', 'Student je sposoban da dizajnira jednostavnu taksonomiju dokumentacije i da vodi ažuriranje runbook-ova ili wiki stranica.', 'Predavanja, vežbe, timski projektni zadaci, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Životni ciklus dokumenta, pretraga i findability, review procesi, knowledge handover u timu, incident response (uvod).'),
(53, 'Cilj predmeta Raspodeljeni algoritmi i konsenzus je da student savlada teorijske i praktične osnove pouzdanosti pod parcijalnim kvarovima i particijama mreže.', 'Student je sposoban da objasni glasanje kvorumom, replikaciju i osnovne korake Raft/Paxos ideje bez formalnog dokaza.', 'Predavanja, vežbe, simulacije ili čitanje rada, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Model padova čvorova i poruka, osnovni problemi dogovora, linija vremena događaja, primena u etcd/Kafka (motivacija).'),
(54, 'Cilj predmeta Priprema diplomskog rada je da student savlada teorijske i praktične osnove planiranja istraživanja i strukturisanja diplomskog rada.', 'Student je sposoban da formuliše istraživačko pitanje, napravi pregled literature i vremenski plan uz konsultacije sa mentorom.', 'Konsultacije, samostalan rad, kolokvijumi o planu rada i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Izbor teme, relevantni izvori, metodologija (eksperimentalno, analitičko, razvoj alata), struktura rada, citiranje i etika istraživanja.'),
(1, 'Cilj predmeta Matematika 1 je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz matematika 1, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(2, 'Cilj predmeta Programiranje 1 je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz programiranje 1, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(3, 'Cilj predmeta Diskretne strukture je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz diskretne strukture, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(4, 'Cilj predmeta Objektno orijentisano programiranje je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz objektno orijentisano programiranje, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(5, 'Cilj predmeta Strukture i algoritmi je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz strukture i algoritmi, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(6, 'Cilj predmeta Operativni sistemi je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz operativni sistemi, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(7, 'Cilj predmeta Baze podataka je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa uključuje relacioni model i SQL, normalizaciju i transakcije, zatim uvod u NoSQL baze: dokument-model (MongoDB ili slično), ključ-vrednost i kolonsko orijentisane baze, CAP teoremu, izbor tehnologije i optimizaciju upita u praksi.'),
(8, 'Cilj predmeta Računarske mreže je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz računarske mreže, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(9, 'Cilj predmeta Paralelno programiranje je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz paralelno programiranje, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(10, 'Cilj predmeta Verifikacija softvera je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz verifikacija softvera, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(20, 'Cilj predmeta Sigurnost informacija je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz sigurnost informacija, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(24, 'Cilj predmeta Informacioni sistemi je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz informacioni sistemi, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.');
INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES 
(55, '13Q01', 'SI - modul 01', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 1),
(56, '13Q02', 'SI - modul 02', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 1),
(57, '13Q03', 'SI - modul 03', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 1),
(58, '13Q04', 'SI - modul 04', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 1),
(59, '13Q05', 'SI - modul 05', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 1),
(60, '13Q06', 'SI - modul 06', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 1),
(61, '13Q07', 'SI - modul 07', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 2),
(62, '13Q08', 'SI - modul 08', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 2),
(63, '13Q09', 'SI - modul 09', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 2),
(64, '13Q10', 'SI - modul 10', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 2),
(65, '13Q11', 'SI - modul 11', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 1, 2),
(66, '13Q12', 'SI - modul 12', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 1),
(67, '13Q13', 'SI - modul 13', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 1),
(68, '13Q14', 'SI - modul 14', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 1),
(69, '13Q15', 'SI - modul 15', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 1),
(70, '13Q16', 'SI - modul 16', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 1),
(71, '13Q17', 'SI - modul 17', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 1),
(72, '13Q18', 'SI - modul 18', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 2),
(73, '13Q19', 'SI - modul 19', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 2),
(74, '13Q20', 'SI - modul 20', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 2),
(75, '13Q21', 'SI - modul 21', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 2),
(76, '13Q22', 'SI - modul 22', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 2, 2),
(77, '13Q23', 'SI - modul 23', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 1),
(78, '13Q24', 'SI - modul 24', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 1),
(79, '13Q25', 'SI - modul 25', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 1),
(80, '13Q26', 'SI - modul 26', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 1),
(81, '13Q27', 'SI - modul 27', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 1),
(82, '13Q28', 'SI - modul 28', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 2),
(83, '13Q29', 'SI - modul 29', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 2),
(84, '13Q30', 'SI - modul 30', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 2),
(85, '13Q31', 'SI - modul 31', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 2),
(86, '13Q32', 'SI - modul 32', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 3, 2),
(87, '13Q33', 'SI - modul 33', 6, 2, 1, 'Strukturni predmet programa SI (demo baza).', 4, 1),
(88, '13Q34', 'SI - modul 34', 7, 2, 1, 'Strukturni predmet programa SI (demo baza).', 4, 1),
(89, '13Q35', 'SI - modul 35', 8, 2, 1, 'Strukturni predmet programa SI (demo baza).', 4, 1),
(11, '13S053', 'Mašinsko učenje', 8, 2, 1, 'Regresija, klasifikacija, drveća odluke, neuronske mreže.', 4, 1),
(19, '13S061', 'Distribuirani sist', 8, 2, 1, 'Replikacija, konsenzus, striming platforme.', 4, 2),
(21, '13S063', 'Web tehnologije', 6, 2, 1, 'HTML, REST, React, mikroservisi.', 4, 2),
(22, '13S071', 'Kompajleri', 8, 2, 1, 'Leksička i sintaksna analiza, optimizacije, LLVM uvod.', 4, 2),
(23, '13S072', 'Cloud computing', 6, 2, 1, 'Virtuelizacija, Docker, Kubernetes, skaliranje.', 4, 2);
INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES 
(55, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(56, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(57, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(58, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(59, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(60, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(61, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(62, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(63, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(64, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(65, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(66, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(67, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(68, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(69, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(70, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(71, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(72, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(73, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(74, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(75, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(76, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(77, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(78, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(79, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(80, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(81, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(82, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(83, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(84, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(85, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(86, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(87, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(88, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(89, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(11, 'Cilj predmeta Mašinsko učenje je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz mašinsko učenje, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(19, 'Cilj predmeta Distribuirani sist je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz distribuirani sist, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(21, 'Cilj predmeta Web tehnologije je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz web tehnologije, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(22, 'Cilj predmeta Kompajleri je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz kompajleri, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(23, 'Cilj predmeta Cloud computing je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz cloud computing, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.');
INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES 
(12, '13A010', 'Servo sistemi', 6, 3, 2, 'Modelovanje, diskretizacija i digitalno upravljanje.', 1, 1),
(13, '13A020', 'Upravljanje procesima', 6, 3, 2, 'PID, neizvesnost i primena u industriji.', 1, 1),
(90, '13U01', 'AUT - modul 01', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 1),
(91, '13U02', 'AUT - modul 02', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 1),
(92, '13U03', 'AUT - modul 03', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 1),
(93, '13U04', 'AUT - modul 04', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 1),
(94, '13U05', 'AUT - modul 05', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 2),
(95, '13U06', 'AUT - modul 06', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 2),
(96, '13U07', 'AUT - modul 07', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 2),
(97, '13U08', 'AUT - modul 08', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 2),
(98, '13U09', 'AUT - modul 09', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 1, 2),
(99, '13U10', 'AUT - modul 10', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 1),
(100, '13U11', 'AUT - modul 11', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 1),
(101, '13U12', 'AUT - modul 12', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 1),
(102, '13U13', 'AUT - modul 13', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 1),
(103, '13U14', 'AUT - modul 14', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 1),
(104, '13U15', 'AUT - modul 15', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 1),
(105, '13U16', 'AUT - modul 16', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 2),
(106, '13U17', 'AUT - modul 17', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 2),
(107, '13U18', 'AUT - modul 18', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 2),
(108, '13U19', 'AUT - modul 19', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 2),
(109, '13U20', 'AUT - modul 20', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 2, 2),
(110, '13U21', 'AUT - modul 21', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 1),
(111, '13U22', 'AUT - modul 22', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 1),
(112, '13U23', 'AUT - modul 23', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 1),
(113, '13U24', 'AUT - modul 24', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 1),
(114, '13U25', 'AUT - modul 25', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 1),
(115, '13U26', 'AUT - modul 26', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 2),
(116, '13U27', 'AUT - modul 27', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 2),
(117, '13U28', 'AUT - modul 28', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 2),
(118, '13U29', 'AUT - modul 29', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 2),
(119, '13U30', 'AUT - modul 30', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 3, 2),
(120, '13U31', 'AUT - modul 31', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 1),
(121, '13U32', 'AUT - modul 32', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 1),
(122, '13U33', 'AUT - modul 33', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 1),
(123, '13U34', 'AUT - modul 34', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 1),
(124, '13U35', 'AUT - modul 35', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 2),
(125, '13U36', 'AUT - modul 36', 6, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 2),
(126, '13U37', 'AUT - modul 37', 7, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 2),
(127, '13U38', 'AUT - modul 38', 8, 3, 2, 'Strukturni predmet programa AUT (demo baza).', 4, 2);
INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES 
(12, 'Cilj predmeta Servo sistemi je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz servo sistemi, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(13, 'Cilj predmeta Upravljanje procesima je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz upravljanje procesima, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(90, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(91, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(92, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(93, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(94, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(95, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(96, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(97, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(98, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(99, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(100, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(101, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(102, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(103, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(104, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(105, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(106, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(107, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(108, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(109, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(110, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(111, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(112, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(113, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(114, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(115, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(116, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(117, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(118, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(119, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(120, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(121, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(122, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(123, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(124, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(125, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(126, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(127, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.');
INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES 
(14, '13E011', 'Električna merenja', 6, 4, 3, 'Senzori, analogni i digitalni signali, greške merenja.', 1, 1),
(15, '13E022', 'Električne mašine', 8, 4, 3, 'Transformatori i asinhroni motori.', 1, 1),
(128, '13L01', 'EE - modul 01', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 1),
(129, '13L02', 'EE - modul 02', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 1),
(130, '13L03', 'EE - modul 03', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 1),
(131, '13L04', 'EE - modul 04', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 1),
(132, '13L05', 'EE - modul 05', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 2),
(133, '13L06', 'EE - modul 06', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 2),
(134, '13L07', 'EE - modul 07', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 2),
(135, '13L08', 'EE - modul 08', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 2),
(136, '13L09', 'EE - modul 09', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 1, 2),
(137, '13L10', 'EE - modul 10', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 1),
(138, '13L11', 'EE - modul 11', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 1),
(139, '13L12', 'EE - modul 12', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 1),
(140, '13L13', 'EE - modul 13', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 1),
(141, '13L14', 'EE - modul 14', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 1),
(142, '13L15', 'EE - modul 15', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 1),
(143, '13L16', 'EE - modul 16', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 2),
(144, '13L17', 'EE - modul 17', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 2),
(145, '13L18', 'EE - modul 18', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 2),
(146, '13L19', 'EE - modul 19', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 2),
(147, '13L20', 'EE - modul 20', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 2, 2),
(148, '13L21', 'EE - modul 21', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 1),
(149, '13L22', 'EE - modul 22', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 1),
(150, '13L23', 'EE - modul 23', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 1),
(151, '13L24', 'EE - modul 24', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 1),
(152, '13L25', 'EE - modul 25', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 1),
(153, '13L26', 'EE - modul 26', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 2),
(154, '13L27', 'EE - modul 27', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 2),
(155, '13L28', 'EE - modul 28', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 2),
(156, '13L29', 'EE - modul 29', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 2),
(157, '13L30', 'EE - modul 30', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 3, 2),
(158, '13L31', 'EE - modul 31', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 1),
(159, '13L32', 'EE - modul 32', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 1),
(160, '13L33', 'EE - modul 33', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 1),
(161, '13L34', 'EE - modul 34', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 1),
(162, '13L35', 'EE - modul 35', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 2),
(163, '13L36', 'EE - modul 36', 6, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 2),
(164, '13L37', 'EE - modul 37', 7, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 2),
(165, '13L38', 'EE - modul 38', 8, 4, 3, 'Strukturni predmet programa EE (demo baza).', 4, 2);
INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES 
(14, 'Cilj predmeta Električna merenja je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz električna merenja, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(15, 'Cilj predmeta Električne mašine je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz električne mašine, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(128, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(129, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(130, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(131, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(132, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(133, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(134, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(135, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(136, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(137, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(138, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(139, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(140, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(141, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(142, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(143, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(144, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(145, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(146, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(147, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(148, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(149, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(150, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(151, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(152, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(153, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(154, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(155, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(156, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(157, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(158, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(159, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(160, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(161, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(162, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(163, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(164, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(165, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.');
INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES 
(16, '13T011', 'Digitalna komunikacija', 8, 5, 4, 'Simboli, šuma, modulacije, OFDM osnove.', 1, 1),
(17, '13T012', 'Antene i prostiranje', 6, 5, 4, 'Link budžet, polarizacija, antenski nizovi.', 1, 1),
(166, '13V01', 'RT - modul 01', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 1),
(167, '13V02', 'RT - modul 02', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 1),
(168, '13V03', 'RT - modul 03', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 1),
(169, '13V04', 'RT - modul 04', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 1),
(170, '13V05', 'RT - modul 05', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 2),
(171, '13V06', 'RT - modul 06', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 2),
(172, '13V07', 'RT - modul 07', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 2),
(173, '13V08', 'RT - modul 08', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 2),
(174, '13V09', 'RT - modul 09', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 1, 2),
(175, '13V10', 'RT - modul 10', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 1),
(176, '13V11', 'RT - modul 11', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 1),
(177, '13V12', 'RT - modul 12', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 1),
(178, '13V13', 'RT - modul 13', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 1),
(179, '13V14', 'RT - modul 14', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 1),
(180, '13V15', 'RT - modul 15', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 1),
(181, '13V16', 'RT - modul 16', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 2),
(182, '13V17', 'RT - modul 17', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 2),
(183, '13V18', 'RT - modul 18', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 2),
(184, '13V19', 'RT - modul 19', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 2),
(185, '13V20', 'RT - modul 20', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 2, 2),
(186, '13V21', 'RT - modul 21', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 1),
(187, '13V22', 'RT - modul 22', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 1),
(188, '13V23', 'RT - modul 23', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 1),
(189, '13V24', 'RT - modul 24', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 1),
(190, '13V25', 'RT - modul 25', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 1),
(191, '13V26', 'RT - modul 26', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 2),
(192, '13V27', 'RT - modul 27', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 2),
(193, '13V28', 'RT - modul 28', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 2),
(194, '13V29', 'RT - modul 29', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 2),
(195, '13V30', 'RT - modul 30', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 3, 2),
(196, '13V31', 'RT - modul 31', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 1),
(197, '13V32', 'RT - modul 32', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 1),
(198, '13V33', 'RT - modul 33', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 1),
(199, '13V34', 'RT - modul 34', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 1),
(200, '13V35', 'RT - modul 35', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 2),
(201, '13V36', 'RT - modul 36', 6, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 2),
(202, '13V37', 'RT - modul 37', 7, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 2),
(203, '13V38', 'RT - modul 38', 8, 5, 4, 'Strukturni predmet programa RT (demo baza).', 4, 2);
INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES 
(16, 'Cilj predmeta Digitalna komunikacija je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz digitalna komunikacija, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(17, 'Cilj predmeta Antene i prostiranje je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz antene i prostiranje, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.'),
(166, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(167, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(168, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(169, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(170, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(171, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(172, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(173, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(174, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(175, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(176, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(177, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(178, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(179, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(180, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(181, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(182, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(183, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(184, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(185, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(186, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(187, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(188, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(189, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(190, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(191, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(192, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(193, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(194, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(195, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(196, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(197, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(198, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(199, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(200, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(201, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(202, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(203, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.');
INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES 
(204, '13M01', 'TK - modul 01', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 1),
(205, '13M02', 'TK - modul 02', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 1),
(206, '13M03', 'TK - modul 03', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 1),
(207, '13M04', 'TK - modul 04', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 1),
(208, '13M05', 'TK - modul 05', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 1),
(209, '13M06', 'TK - modul 06', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 1),
(210, '13M07', 'TK - modul 07', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 2),
(211, '13M08', 'TK - modul 08', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 2),
(212, '13M09', 'TK - modul 09', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 2),
(213, '13M10', 'TK - modul 10', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 2),
(214, '13M11', 'TK - modul 11', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 1, 2),
(215, '13M12', 'TK - modul 12', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 1),
(216, '13M13', 'TK - modul 13', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 1),
(217, '13M14', 'TK - modul 14', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 1),
(218, '13M15', 'TK - modul 15', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 1),
(219, '13M16', 'TK - modul 16', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 1),
(220, '13M17', 'TK - modul 17', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 1),
(221, '13M18', 'TK - modul 18', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 2),
(222, '13M19', 'TK - modul 19', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 2),
(223, '13M20', 'TK - modul 20', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 2),
(224, '13M21', 'TK - modul 21', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 2),
(225, '13M22', 'TK - modul 22', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 2, 2),
(226, '13M23', 'TK - modul 23', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 1),
(227, '13M24', 'TK - modul 24', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 1),
(228, '13M25', 'TK - modul 25', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 1),
(229, '13M26', 'TK - modul 26', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 1),
(230, '13M27', 'TK - modul 27', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 1),
(231, '13M28', 'TK - modul 28', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 2),
(232, '13M29', 'TK - modul 29', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 2),
(233, '13M30', 'TK - modul 30', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 2),
(234, '13M31', 'TK - modul 31', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 2),
(235, '13M32', 'TK - modul 32', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 3, 2),
(236, '13M33', 'TK - modul 33', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 1),
(237, '13M34', 'TK - modul 34', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 1),
(238, '13M35', 'TK - modul 35', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 1),
(239, '13M36', 'TK - modul 36', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 1),
(240, '13M37', 'TK - modul 37', 7, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 2),
(241, '13M38', 'TK - modul 38', 8, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 2),
(242, '13M39', 'TK - modul 39', 6, 6, 4, 'Strukturni predmet programa TK master (demo baza).', 4, 2),
(18, '13T013', 'Bežične senzorske mreže', 6, 6, 4, 'MAC protokoli, rutiranje i energetska efikasnost.', 4, 2);
INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES 
(204, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(205, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(206, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(207, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(208, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(209, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(210, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(211, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(212, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(213, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(214, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(215, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(216, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(217, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(218, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(219, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(220, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(221, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(222, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(223, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(224, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(225, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(226, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(227, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(228, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(229, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(230, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(231, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(232, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(233, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(234, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(235, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(236, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(237, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(238, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(239, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(240, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(241, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(242, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.'),
(18, 'Cilj predmeta Bežične senzorske mreže je da student savlada teorijske i praktične osnove neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva.', 'Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja.', 'Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u.', 'Tok kursa obuhvata osnovne teme iz bežične senzorske mreže, uključujući pregled literature, rad u razvojnom okruženju i vežbe iz tipskih zadataka iz ispitne prakse.');

-- Demo kurikulum: ≤45 predmeta po programu; godine i semestar usklađeni sa kolonama kurikulum_*.
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (2, 1);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (3, 2);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (4, 3);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (5, 4);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (6, 5);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (7, 6);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (8, 7);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (9, 8);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (10, 9);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (11, 10);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (12, 11);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (13, 12);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (14, 13);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (15, 14);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (16, 15);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (17, 16);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (18, 17);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (19, 18);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (20, 19);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (21, 20);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (22, 21);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (23, 22);
INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES (24, 23);
SELECT setval(pg_get_serial_sequence('preduslov','id'), COALESCE((SELECT MAX(id) FROM preduslov), 1));
INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES (1, 'Januarski ispitni rok', '2023/24', 'zimski');
INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES (2, 'Aprilski ispitni rok', '2023/24', 'prolećni');
INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES (3, 'Junski ispitni rok', '2023/24', 'letnji');
INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES (4, 'Septembarski rok', '2024/25', 'letnji');
INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES (5, 'Januarski ispitni rok', '2024/25', 'zimski');
INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES (6, 'Aprilski ispitni rok', '2024/25', 'prolećni');
SELECT setval(pg_get_serial_sequence('ispitni_rok','id'), (SELECT MAX(id) FROM ispitni_rok));
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (1, 1, 1, '2024-02-04T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (2, 1, 2, '2024-02-01T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (3, 1, 3, '2024-02-04T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (4, 1, 4, '2024-02-06T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (5, 1, 5, '2024-02-05T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (6, 1, 6, '2024-02-04T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (7, 1, 7, '2024-02-07T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (8, 1, 8, '2024-02-01T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (9, 1, 9, '2024-02-02T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (10, 1, 10, '2024-02-01T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (11, 1, 11, '2024-02-04T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (12, 1, 12, '2024-01-31T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (13, 1, 13, '2024-01-29T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (14, 1, 16, '2024-02-07T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (15, 1, 17, '2024-01-28T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (16, 1, 18, '2024-02-02T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (17, 1, 19, '2024-02-04T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (18, 1, 20, '2024-02-06T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (19, 1, 21, '2024-02-01T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (20, 1, 22, '2024-01-29T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (21, 1, 23, '2024-02-01T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (22, 1, 24, '2024-01-28T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (23, 2, 1, '2024-02-22T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (24, 2, 2, '2024-02-19T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (25, 2, 3, '2024-02-22T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (26, 2, 4, '2024-02-24T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (27, 2, 5, '2024-02-23T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (28, 2, 7, '2024-02-25T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (29, 2, 8, '2024-02-19T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (30, 2, 9, '2024-02-20T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (31, 2, 10, '2024-02-19T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (32, 2, 12, '2024-02-18T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (33, 2, 14, '2024-02-20T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (34, 2, 16, '2024-02-25T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (35, 2, 17, '2024-02-15T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (36, 2, 18, '2024-02-20T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (37, 2, 19, '2024-02-22T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (38, 2, 20, '2024-02-24T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (39, 2, 21, '2024-02-19T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (40, 2, 23, '2024-02-19T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (41, 2, 24, '2024-02-15T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (42, 3, 2, '2024-03-08T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (43, 3, 3, '2024-03-11T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (44, 3, 4, '2024-03-13T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (45, 3, 5, '2024-03-12T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (46, 3, 6, '2024-03-11T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (47, 3, 7, '2024-03-14T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (48, 3, 9, '2024-03-09T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (49, 3, 10, '2024-03-08T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (50, 3, 11, '2024-03-11T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (51, 3, 12, '2024-03-07T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (52, 3, 13, '2024-03-05T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (53, 3, 15, '2024-03-13T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (54, 3, 16, '2024-03-14T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (55, 3, 17, '2024-03-04T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (56, 3, 18, '2024-03-09T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (57, 3, 19, '2024-03-11T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (58, 3, 20, '2024-03-13T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (59, 3, 21, '2024-03-08T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (60, 3, 22, '2024-03-05T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (61, 4, 1, '2024-03-29T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (62, 4, 2, '2024-03-26T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (63, 4, 3, '2024-03-29T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (64, 4, 4, '2024-03-31T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (65, 4, 5, '2024-03-30T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (66, 4, 6, '2024-03-29T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (67, 4, 9, '2024-03-27T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (68, 4, 11, '2024-03-29T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (69, 4, 12, '2024-03-25T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (70, 4, 13, '2024-03-23T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (71, 4, 14, '2024-03-27T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (72, 4, 15, '2024-03-31T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (73, 4, 16, '2024-04-01T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (74, 4, 18, '2024-03-27T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (75, 4, 19, '2024-03-29T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (76, 4, 20, '2024-03-31T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (77, 4, 21, '2024-03-26T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (78, 4, 22, '2024-03-23T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (79, 4, 23, '2024-03-26T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (80, 4, 24, '2024-03-22T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (81, 5, 1, '2024-04-16T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (82, 5, 2, '2024-04-13T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (83, 5, 3, '2024-04-16T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (84, 5, 4, '2024-04-18T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (85, 5, 6, '2024-04-16T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (86, 5, 7, '2024-04-19T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (87, 5, 9, '2024-04-14T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (88, 5, 10, '2024-04-13T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (89, 5, 11, '2024-04-16T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (90, 5, 12, '2024-04-12T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (91, 5, 13, '2024-04-10T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (92, 5, 14, '2024-04-14T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (93, 5, 15, '2024-04-18T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (94, 5, 16, '2024-04-19T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (95, 5, 17, '2024-04-09T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (96, 5, 18, '2024-04-14T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (97, 5, 19, '2024-04-16T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (98, 5, 20, '2024-04-18T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (99, 5, 22, '2024-04-10T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (100, 5, 23, '2024-04-13T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (101, 6, 1, '2024-05-04T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (102, 6, 2, '2024-05-01T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (103, 6, 3, '2024-05-04T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (104, 6, 4, '2024-05-06T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (105, 6, 5, '2024-05-05T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (106, 6, 7, '2024-05-07T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (107, 6, 9, '2024-05-02T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (108, 6, 10, '2024-05-01T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (109, 6, 11, '2024-05-04T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (110, 6, 12, '2024-04-30T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (111, 6, 13, '2024-04-28T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (112, 6, 14, '2024-05-02T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (113, 6, 16, '2024-05-07T12:00:00+00:00', 'RC-05');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (114, 6, 17, '2024-04-27T13:00:00+00:00', 'AMP-1');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (115, 6, 18, '2024-05-02T09:00:00+00:00', 'A-101');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (116, 6, 21, '2024-05-01T10:00:00+00:00', 'A-102');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (117, 6, 22, '2024-04-28T11:00:00+00:00', 'B-201');
INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES (118, 6, 23, '2024-05-01T12:00:00+00:00', 'RC-05');
SELECT setval(pg_get_serial_sequence('ispitni_termin','id'), (SELECT MAX(id) FROM ispitni_termin));
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (1, 1, 8, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (2, 1, 44, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (3, 1, 81, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (4, 1, 102, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (5, 1, 18, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (6, 1, 25, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (7, 1, 22, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (8, 1, 88, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (9, 1, 85, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (10, 1, 7, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (11, 1, 5, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (12, 1, 67, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (13, 2, 89, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (14, 2, 97, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (15, 2, 19, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (16, 2, 79, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (17, 2, 60, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (18, 3, 91, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (19, 3, 32, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (20, 4, 33, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (21, 4, 72, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (22, 5, 54, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (23, 5, 95, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (24, 6, 16, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (25, 7, 45, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (26, 7, 85, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (27, 7, 108, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (28, 7, 104, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (29, 7, 9, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (30, 7, 101, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (31, 7, 80, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (32, 7, 29, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (33, 7, 25, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (34, 7, 47, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (35, 7, 76, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (36, 7, 2, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (37, 8, 20, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (38, 8, 77, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (39, 8, 37, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (40, 8, 109, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (41, 8, 100, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (42, 9, 12, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (43, 9, 111, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (44, 10, 71, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (45, 10, 72, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (46, 11, 35, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (47, 11, 73, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (48, 12, 74, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (49, 13, 81, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (50, 13, 102, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (51, 13, 30, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (52, 13, 98, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (53, 13, 3, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (54, 13, 22, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (55, 13, 49, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (56, 13, 8, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (57, 13, 6, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (58, 13, 45, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (59, 13, 64, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (60, 13, 86, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (61, 14, 50, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (62, 14, 117, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (63, 14, 116, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (64, 14, 118, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (65, 14, 37, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (66, 15, 13, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (67, 15, 110, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (68, 16, 112, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (69, 16, 93, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (70, 17, 94, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (71, 17, 35, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (72, 18, 74, 30, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (73, 19, 18, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (74, 19, 103, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (75, 19, 62, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (76, 19, 46, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (77, 19, 10, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (78, 19, 1, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (79, 19, 44, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (80, 19, 29, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (81, 19, 9, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (82, 19, 105, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (83, 19, 106, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (84, 19, 22, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (85, 20, 37, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (86, 20, 19, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (87, 20, 89, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (88, 20, 100, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (89, 20, 99, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (90, 21, 32, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (91, 21, 13, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (92, 22, 92, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (93, 22, 93, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (94, 23, 73, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (95, 23, 95, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (96, 24, 36, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (97, 25, 87, 28, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (98, 25, 65, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (99, 25, 85, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (100, 25, 18, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (101, 25, 42, 69, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (102, 25, 101, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (103, 25, 104, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (104, 25, 103, 18, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (105, 25, 88, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (106, 25, 7, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (107, 25, 29, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (108, 25, 80, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (109, 26, 97, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (110, 26, 100, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (111, 26, 59, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (112, 26, 78, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (113, 26, 68, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (114, 27, 32, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (115, 27, 70, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (116, 28, 33, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (117, 28, 53, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (118, 29, 113, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (119, 29, 55, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (120, 30, 16, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (121, 31, 61, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (122, 31, 8, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (123, 31, 98, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (124, 31, 42, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (125, 31, 108, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (126, 31, 7, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (127, 31, 105, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (128, 31, 66, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (129, 31, 9, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (130, 31, 22, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (131, 31, 44, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (132, 31, 83, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (133, 32, 21, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (134, 32, 37, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (135, 32, 116, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (136, 32, 99, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (137, 32, 11, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (138, 33, 52, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (139, 33, 12, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (140, 34, 53, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (141, 34, 71, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (142, 35, 35, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (143, 35, 14, 19, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (144, 36, 96, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (145, 37, 104, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (146, 37, 76, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (147, 37, 3, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (148, 37, 1, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (149, 37, 86, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (150, 37, 108, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (151, 37, 105, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (152, 37, 22, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (153, 37, 62, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (154, 37, 107, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (155, 37, 6, 18, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (156, 37, 29, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (157, 38, 99, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (158, 38, 21, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (159, 38, 19, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (160, 38, 50, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (161, 38, 17, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (162, 39, 13, 30, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (163, 39, 51, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (164, 40, 71, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (165, 40, 93, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (166, 41, 54, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (167, 41, 15, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (168, 42, 56, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (169, 43, 58, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (170, 43, 8, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (171, 43, 66, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (172, 43, 80, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (173, 43, 101, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (174, 43, 10, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (175, 43, 3, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (176, 43, 82, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (177, 43, 48, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (178, 43, 26, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (179, 43, 5, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (180, 43, 106, 30, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (181, 44, 118, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (182, 44, 68, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (183, 44, 60, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (184, 44, 37, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (185, 44, 39, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (186, 45, 110, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (187, 45, 91, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (188, 46, 33, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (189, 46, 72, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (190, 47, 94, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (191, 47, 95, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (192, 48, 96, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (193, 49, 49, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (194, 49, 106, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (195, 49, 22, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (196, 49, 45, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (197, 49, 44, 28, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (198, 49, 48, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (199, 49, 23, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (200, 49, 83, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (201, 49, 98, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (202, 49, 66, 19, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (203, 49, 24, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (204, 49, 8, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (205, 50, 117, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (206, 50, 75, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (207, 50, 77, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (208, 50, 40, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (209, 50, 50, 69, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (210, 51, 69, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (211, 51, 52, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (212, 52, 72, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (213, 52, 92, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (214, 53, 34, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (215, 53, 95, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (216, 54, 74, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (217, 55, 76, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (218, 55, 65, 62, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (219, 55, 49, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (220, 55, 8, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (221, 55, 3, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (222, 55, 9, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (223, 55, 106, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (224, 55, 62, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (225, 55, 80, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (226, 55, 101, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (227, 55, 64, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (228, 55, 66, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (229, 56, 60, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (230, 56, 50, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (231, 56, 40, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (232, 56, 17, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (233, 56, 19, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (234, 57, 12, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (235, 57, 13, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (236, 58, 112, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (237, 58, 72, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (238, 59, 54, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (239, 59, 95, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (240, 60, 16, 30, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (241, 61, 41, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (242, 61, 2, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (243, 61, 1, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (244, 61, 9, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (245, 61, 58, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (246, 61, 104, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (247, 61, 6, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (248, 61, 43, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (249, 61, 7, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (250, 61, 108, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (251, 61, 8, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (252, 61, 27, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (253, 62, 19, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (254, 62, 40, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (255, 62, 68, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (256, 62, 60, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (257, 62, 57, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (258, 63, 70, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (259, 63, 90, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (260, 64, 93, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (261, 64, 71, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (262, 65, 114, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (263, 65, 94, 19, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (264, 66, 115, 30, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (265, 67, 28, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (266, 67, 108, 18, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (267, 67, 8, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (268, 67, 23, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (269, 67, 63, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (270, 67, 85, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (271, 67, 41, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (272, 67, 76, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (273, 67, 87, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (274, 67, 104, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (275, 67, 27, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (276, 67, 42, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (277, 68, 99, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (278, 68, 59, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (279, 68, 50, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (280, 68, 79, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (281, 68, 97, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (282, 69, 70, 69, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (283, 69, 51, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (284, 70, 93, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (285, 70, 112, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (286, 71, 54, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (287, 71, 114, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (288, 72, 16, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (289, 73, 108, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (290, 73, 42, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (291, 73, 80, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (292, 73, 29, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (293, 73, 58, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (294, 73, 101, 62, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (295, 73, 105, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (296, 73, 7, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (297, 73, 26, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (298, 73, 25, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (299, 73, 46, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (300, 73, 107, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (301, 74, 89, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (302, 74, 97, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (303, 74, 117, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (304, 74, 116, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (305, 74, 79, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (306, 75, 51, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (307, 75, 70, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (308, 76, 71, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (309, 76, 72, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (310, 77, 114, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (311, 77, 34, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (312, 78, 16, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (313, 79, 65, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (314, 79, 44, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (315, 79, 103, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (316, 79, 31, 62, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (317, 79, 76, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (318, 79, 9, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (319, 79, 41, 69, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (320, 79, 8, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (321, 79, 6, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (322, 79, 28, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (323, 79, 82, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (324, 79, 81, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (325, 80, 11, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (326, 80, 59, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (327, 80, 78, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (328, 80, 21, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (329, 80, 17, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (330, 81, 52, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (331, 81, 69, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (332, 82, 93, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (333, 82, 33, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (334, 83, 55, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (335, 83, 113, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (336, 84, 74, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (337, 85, 2, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (338, 85, 22, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (339, 85, 86, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (340, 85, 43, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (341, 85, 64, 28, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (342, 85, 58, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (343, 85, 46, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (344, 85, 5, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (345, 85, 29, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (346, 85, 30, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (347, 85, 1, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (348, 85, 31, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (349, 86, 75, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (350, 86, 39, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (351, 86, 118, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (352, 86, 50, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (353, 86, 99, 28, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (354, 87, 69, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (355, 87, 111, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (356, 88, 71, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (357, 88, 72, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (358, 89, 95, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (359, 89, 73, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (360, 90, 56, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (361, 91, 43, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (362, 91, 107, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (363, 91, 104, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (364, 91, 108, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (365, 91, 66, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (366, 91, 76, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (367, 91, 29, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (368, 91, 2, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (369, 91, 22, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (370, 91, 47, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (371, 91, 65, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (372, 91, 61, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (373, 92, 50, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (374, 92, 118, 17, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (375, 92, 19, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (376, 92, 17, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (377, 92, 78, 33, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (378, 93, 12, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (379, 93, 111, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (380, 94, 93, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (381, 94, 33, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (382, 95, 55, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (383, 95, 14, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (384, 96, 56, 62, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (385, 97, 103, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (386, 97, 84, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (387, 97, 45, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (388, 97, 80, 31, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (389, 97, 9, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (390, 97, 6, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (391, 97, 101, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (392, 97, 8, 62, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (393, 97, 10, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (394, 97, 98, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (395, 97, 47, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (396, 97, 62, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (397, 98, 109, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (398, 98, 21, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (399, 98, 77, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (400, 98, 57, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (401, 98, 60, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (402, 99, 12, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (403, 99, 111, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (404, 100, 72, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (405, 100, 71, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (406, 101, 73, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (407, 101, 55, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (408, 102, 56, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (409, 103, 65, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (410, 103, 66, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (411, 103, 86, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (412, 103, 58, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (413, 103, 61, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (414, 103, 43, 29, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (415, 103, 87, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (416, 103, 102, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (417, 103, 29, 32, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (418, 103, 104, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (419, 103, 41, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (420, 103, 10, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (421, 104, 97, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (422, 104, 50, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (423, 104, 116, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (424, 104, 78, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (425, 104, 21, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (426, 105, 69, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (427, 105, 70, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (428, 106, 33, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (429, 106, 72, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (430, 107, 14, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (431, 107, 15, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (432, 108, 56, 55, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (433, 109, 28, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (434, 109, 102, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (435, 109, 81, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (436, 109, 66, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (437, 109, 98, 28, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (438, 109, 45, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (439, 109, 29, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (440, 109, 108, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (441, 109, 63, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (442, 109, 87, 58, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (443, 109, 4, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (444, 109, 80, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (445, 110, 50, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (446, 110, 117, 19, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (447, 110, 39, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (448, 110, 57, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (449, 110, 100, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (450, 111, 69, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (451, 111, 13, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (452, 112, 92, 51, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (453, 112, 93, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (454, 113, 114, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (455, 113, 113, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (456, 114, 115, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (457, 115, 30, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (458, 115, 7, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (459, 115, 43, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (460, 115, 108, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (461, 115, 38, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (462, 115, 41, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (463, 115, 26, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (464, 115, 102, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (465, 115, 85, 27, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (466, 115, 65, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (467, 115, 29, 44, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (468, 115, 1, 49, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (469, 116, 77, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (470, 116, 97, 60, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (471, 116, 79, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (472, 116, 20, 62, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (473, 116, 11, 42, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (474, 117, 52, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (475, 117, 32, 63, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (476, 118, 33, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (477, 118, 53, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (478, 119, 113, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (479, 119, 15, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (480, 120, 16, 59, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (481, 121, 45, 64, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (482, 121, 85, 34, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (483, 121, 108, 20, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (484, 121, 104, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (485, 121, 9, 34, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (486, 121, 101, 36, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (487, 121, 80, 46, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (488, 121, 29, 53, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (489, 121, 25, 35, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (490, 121, 47, 66, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (491, 121, 76, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (492, 121, 2, 45, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (493, 122, 45, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (494, 122, 85, 35, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (495, 122, 108, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (496, 122, 104, 25, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (497, 122, 9, 35, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (498, 122, 101, 37, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (499, 122, 80, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (500, 122, 29, 54, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (501, 122, 25, 36, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (502, 122, 47, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (503, 122, 76, 38, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (504, 122, 2, 46, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (505, 123, 45, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (506, 123, 85, 36, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (507, 123, 108, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (508, 123, 104, 26, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (509, 123, 9, 36, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (510, 123, 101, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (511, 123, 80, 48, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (512, 123, 29, 55, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (513, 123, 25, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (514, 123, 47, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (515, 123, 76, 39, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (516, 123, 2, 47, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (517, 124, 45, 67, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (518, 124, 85, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (519, 124, 108, 23, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (520, 124, 104, 27, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (521, 124, 9, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (522, 124, 101, 39, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (523, 124, 80, 49, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (524, 124, 29, 56, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (525, 124, 25, 38, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (526, 124, 47, 69, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (527, 124, 76, 40, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (528, 124, 2, 48, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (529, 125, 45, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (530, 125, 85, 38, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (531, 125, 108, 24, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (532, 125, 104, 28, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (533, 125, 9, 38, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (534, 125, 101, 40, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (535, 125, 80, 50, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (536, 125, 29, 57, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (537, 125, 25, 39, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (538, 125, 47, 70, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (539, 125, 76, 41, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (540, 125, 2, 49, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (541, 126, 45, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (542, 126, 85, 34, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (543, 126, 108, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (544, 126, 104, 24, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (545, 126, 9, 34, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (546, 126, 101, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (547, 126, 80, 46, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (548, 126, 29, 53, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (549, 126, 25, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (550, 126, 47, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (551, 126, 76, 37, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (552, 126, 2, 45, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (553, 127, 45, 65, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (554, 127, 85, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (555, 127, 108, 21, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (556, 127, 104, 25, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (557, 127, 9, 35, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (558, 127, 101, 37, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (559, 127, 80, 47, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (560, 127, 29, 54, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (561, 127, 25, 36, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (562, 127, 47, 67, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (563, 127, 76, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (564, 127, 2, 46, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (565, 128, 45, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (566, 128, 85, 36, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (567, 128, 108, 22, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (568, 128, 104, 26, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (569, 128, 9, 36, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (570, 128, 101, 38, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (571, 128, 80, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (572, 128, 29, 55, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (573, 128, 25, 37, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (574, 128, 47, 68, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (575, 128, 76, 39, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (576, 128, 2, 47, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (577, 129, 45, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (578, 129, 85, 37, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (579, 129, 108, 23, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (580, 129, 104, 27, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (581, 129, 9, 37, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (582, 129, 101, 39, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (583, 129, 80, 49, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (584, 129, 29, 56, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (585, 129, 25, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (586, 129, 47, 69, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (587, 129, 76, 40, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (588, 129, 2, 48, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (589, 130, 45, 68, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (590, 130, 85, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (591, 130, 108, 24, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (592, 130, 104, 28, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (593, 130, 9, 38, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (594, 130, 101, 40, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (595, 130, 80, 50, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (596, 130, 29, 57, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (597, 130, 25, 39, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (598, 130, 47, 70, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (599, 130, 76, 41, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (600, 130, 2, 49, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (601, 131, 45, 64, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (602, 131, 85, 34, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (603, 131, 108, 20, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (604, 131, 104, 24, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (605, 131, 9, 34, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (606, 131, 101, 36, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (607, 131, 80, 46, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (608, 131, 29, 53, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (609, 131, 25, 35, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (610, 131, 47, 66, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (611, 131, 76, 37, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (612, 131, 2, 45, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (613, 132, 45, 65, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (614, 132, 85, 35, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (615, 132, 108, 21, 5);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (616, 132, 104, 25, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (617, 132, 9, 35, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (618, 132, 101, 37, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (619, 132, 80, 47, 9);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (620, 132, 29, 54, 8);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (621, 132, 25, 36, 7);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (622, 132, 47, 67, 10);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (623, 132, 76, 38, 6);
INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES (624, 132, 2, 46, 8);
SELECT setval(pg_get_serial_sequence('predmet','id'), (SELECT MAX(id) FROM predmet));
SELECT setval(pg_get_serial_sequence('ocena','id'), (SELECT MAX(id) FROM ocena));
