-- Dopuna / ažuriranje 30 modula programa RI (13R01–13R30): nazivi, opisi, sadržaj.
-- Raspodela godina u aplikaciji: 13R01–30 nisu u 1. godini; 01–10 → 2.; 11–20 → 3.; 21–30 → 4.; 13S002 → 2.
-- Primer: Get-Content .\patch_ri_30_modula.sql -Raw | docker compose exec -T postgres psql -U nais -d nais

DO $$
DECLARE
    i int;
    sid text;
    esp int;
    pid bigint;
    titles text[] := ARRAY[
        'Uvod u računarsku nauku i profesiju',
        'Matematička logika i dokazivanje',
        'Linearna algebra za informatičare',
        'Verovatnoća i statistika',
        'Elektrotehničke osnove informatike',
        'Rad u Unix/Linux okruženju',
        'Dokumentovanje softverskih projekata',
        'Seminarski rad iz programiranja',
        'Programske paradigme i jezici',
        'Kompjuterska arhitektura',
        'Teorija automata i formalnih jezika',
        'Numerička matematika za inženjere',
        'Upravljanje memorijom u jezicima niskog nivoa',
        'Paralelizam na nivou instrukcija',
        'Konkurentno programiranje',
        'Uvod u grafičke sisteme',
        'Razvoj skalabilnih veb servisa',
        'Napredne baze podataka u praksi',
        'Bezbednost aplikacija i mreže',
        'Strategije testiranja softvera',
        'DevOps i neprekidna isporuka',
        'Obrada prirodnog jezika',
        'Ugrađeni i real-time sistemi',
        'Timski projekat – analiza i arhitektura',
        'Produkcija i održavanje softvera',
        'Pravo intelektualne svojine u IT',
        'Etika i privatnost podataka',
        'Upravljanje znanjem u organizacijama',
        'Raspodeljeni algoritmi i konsenzus',
        'Priprema diplomskog rada'
    ];
    shorts text[] := ARRAY[
        'Disciplina, istorija računarstva, uloga softverskog inženjera (2. godina RI).',
        'Iskazna logika, dokaz, priprema za teoriju računarstva (2. godina RI).',
        'Vektorski prostori, matrice, linearni sistemi u CS primenama (2. godina RI).',
        'Osnovni modeli, statistička inferencija i rad sa podacima (2. godina RI).',
        'Digitalna logika, signali, osnovni sklopovi i binarna aritmetika (2. godina RI).',
        'Školjka, procesi, dozvole, skripte i alati komandne linije (2. godina RI).',
        'Git, Markdown, dijagrami i pravilna tehnička komunikacija (2. godina RI).',
        'Samostalan mali projekat, izveštaj i odbrana rezultata (2. godina RI).',
        'Poređenje imperativnog, funkcionalnog i deklarativnog stila (2. godina RI).',
        'CPU, memorijska hijerarhija, pipeline i performanse (2. godina RI).',
        'Konačni automati, regularni jezici, gramatike, uvod u izračunljivost (3. godina RI).',
        'Mašinska aritmetika, greške, stabilne numeričke metode (3. godina RI).',
        'Stack/heap, pokazivači, alati za proveru curenja memorije (3. godina RI).',
        'SIMD, uvod u OpenMP, zakon Amdahla (3. godina RI).',
        'Niti, sinhronizacija, deadlock i utrkivanje (3. godina RI).',
        'Grafički pipeline, rasterizacija, osnovni API (3. godina RI).',
        'REST, autentikacija, sesije, greške i rast opterećenja (3. godina RI).',
        'Indeksi, izolacija transakcija, plan izvršenja i optimizacija (3. godina RI).',
        'OWASP, hardening, mrežni i aplikacioni sloj (3. godina RI).',
        'Unit, integracioni testovi, mocking i TDD (3. godina RI).',
        'CI/CD, konfiguracija kao kod, kontejneri u timu (4. godina RI).',
        'Tokenizacija, ugrađivanja, jednostavni klasifikatori teksta (4. godina RI).',
        'Mikrokontroleri, senzori, ograničenja u realnom vremenu (4. godina RI).',
        'Prikupljanje zahteva, dizajn rešenja i dokumentacija (4. godina RI).',
        'Logovanje, metrike, strategije izdavanja i otklanjanja kvarova (4. godina RI).',
        'Licence, autorsko pravo, patenti i otvoreni kod (4. godina RI).',
        'GDPR/LPD perspektiva, pristanak i minimizacija podataka (4. godina RI).',
        'Baze znanja, wiki procesi i prenos ekspertize (4. godina RI).',
        'Klasteri, pouzdanost pod podelama, uvod u konsenzus protokole (4. godina RI).',
        'Literatura, metodologija istraživanja i plan izrade rada (4. godina RI).'
    ];
BEGIN
    FOR i IN 1..30 LOOP
        sid := '13R' || lpad(i::text, 2, '0');
        esp := 6 + (i % 3);
        IF NOT EXISTS (SELECT 1 FROM predmet WHERE sifra = sid) THEN
            INSERT INTO predmet (sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis)
            VALUES (sid, titles[i], esp, 1, 1, shorts[i]);
        ELSE
            UPDATE predmet
            SET naziv = titles[i], espb = esp, kratak_opis = shorts[i], studijski_program_id = 1, katedra_id = 1
            WHERE sifra = sid;
        END IF;
        SELECT id INTO pid FROM predmet WHERE sifra = sid;
        IF pid IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sadrzaj_predmeta WHERE predmet_id = pid) THEN
            INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa)
            VALUES (
                pid,
                'Cilj predmeta ' || titles[i] || ' je usvajanje ishoda predviđenih za odgovarajuću godinu programa RI (demo kurikulum).',
                'Ovladavanje temama iz kratak_opis.',
                'Predavanja, vežbe i ispitivanje.',
                'Detalji u skladu sa temama istaknutim u kratak_opis.'
            );
        ELSIF pid IS NOT NULL THEN
            UPDATE sadrzaj_predmeta
            SET cilj = 'Cilj predmeta ' || titles[i] || ' je usvajanje ishoda predviđenih za odgovarajuću godinu programa RI (demo kurikulum).',
                ishodi_ucenja = 'Ovladavanje temama iz kratak_opis.',
                metode_nastave = 'Predavanja, vežbe i ispitivanje.',
                teme_kursa = 'Detalji u skladu sa temama istaknutim u kratak_opis.'
            WHERE predmet_id = pid;
        END IF;
    END LOOP;
END $$;

SELECT setval(
    pg_get_serial_sequence('predmet', 'id'),
    COALESCE((SELECT MAX(id) FROM predmet), 1)
);
