-- Bogatiji službeni sadržaj za asistenta / portal (predmet 13U09, program automatike).
UPDATE sadrzaj_predmeta sp
SET
  cilj = 'Upoznavanje studenta sa osnovama diskretne obrade signala: reprezentacija signala, Furijeova i Z- transformacija, diskretni sistemi linearnog tipa, diskretna konvolucija, frekvencijski opis sistema i veza sa analognom obradom signala.',
  ishodi_ucenja = 'Student razume odnos kontinualnog i diskretnog signala i može da primeni osnovne algoritme (DFT/FFT, konvoluciju) u inženjerskim zadacima; interpretira spektar i stabilnost diskretnih sistema u okviru uvodnog nivoa.',
  metode_nastave = 'Predavanja sa ilustracijama i demonstracijama, računarske vežbe (Python/MATLAB po dogovoru sa katedrom), domaći zadaci, kolokvijumi, usmeni ili pismeni završni ispit.',
  teme_kursa = 'Uzorkovanje i kvantizacija; diskretni signali i sistemi; linearna konvolucija i koreacija; Z-transformacija i inverzija; Furijeova analiza diskretnih signala (DFT, FFT); uvod u digitalne filtre (FIR/IIR ideja); veza sa obradom signala u automatizaciji i merenjima.'
WHERE sp.predmet_id = (SELECT id FROM predmet WHERE sifra = '13U09' AND studijski_program_id = 3 LIMIT 1);
