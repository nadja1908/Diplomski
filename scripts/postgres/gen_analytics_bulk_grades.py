#!/usr/bin/env python3
"""
Opciono proširenje seed podataka za analitiku.

Postojeći 02_data.sql već sadrži stotine ocena i više generacija po programima.
Za dodatni volumen, najbezbednije je u SQL klijentu generisati INSERT-e gde su
(student_id, ispitni_termin_id) jedinstveni i oba pripadaju istom studijskom programu:

  SELECT s.id, t.id
  FROM student s
  JOIN ispitni_termin t ON EXISTS (
    SELECT 1 FROM predmet p
    WHERE p.id = t.predmet_id AND p.studijski_program_id = s.studijski_program_id
  )
  WHERE s.studijski_program_id = 1
  LIMIT 50;

Zatim za svaki par dodeliti slučajnu ocenu 5–10 i poene, pazeći na UNIQUE(student_id, ispitni_termin_id).

Ovaj fajl služi kao checklist umesto krhkog hardkodovanog generatora koji zavisi od tačnog redosleda termina u seed-u.
"""
print("-- Dodatne ocene po potrebi; koristi INSERT ... SELECT sa gornjim join uslovom.")
