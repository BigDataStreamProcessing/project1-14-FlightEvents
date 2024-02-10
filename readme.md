# Charakterystyka danych
Dane zawierają informację o lądowaniach różnych samolotów, reprezentujących różne linie lotnicze na różnych lotniskach na świecie.

W strumieniu pojawiają się zdarzenia zgodne ze schematem `FlightEvent`:

```
create json schema 
FlightEvent(aircraft string, airline string, airport string, flight string, clouds string, wind_dir string, wind_str double, ets string, its string);
```

Każde zdarzenie związane z jest z faktem zarejestrowania lądowania pojedynczej maszyny na danym lotnisku przy określonych warunkach pogodowych.

Zostały uzupełnione o dane pogodowe takie jak: zachmurzenie, siła i kierunek wiatru.

Dane uzupełnione są o dwie etykiety czasowe. 
* Pierwsza (`ets`) związana jest z momentem lądowania samolotu. 
  Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego maksymalnie do 30 sekund.
* Druga (`its`) związana jest z momentem rejestracji zdarzenia lądowania w systemie.

# Opis atrybutów

Atrybuty w każdym zdarzeniu zgodnym ze schematem `FlightEvent` mają następujące znaczenie:

* `aircraft` - znaczenie: nazwa (typ) samolotu
* `airline`- nazwa (kod) linii lotniczej
* `airport` - nazwa (kod) lotniska
* `flight`- kod lotu
* `clouds`- kod opisujący poziom zachmurzenia podczas lądowania (zgodny ze standardem `METAR`)
* `wind_dir` - kierunek wiatru podczas lądowania (wyrażony w formacie N, NE, E, ...)
* `wind_str` - siła wiatru podczas lądowania wyrażona w km/h
* `ets`- czas lądowania samolotu
* `its` - czas rejestracji faktu lądowania w systemie

# Wyjaśnienie atrybutu `clouds`:

Poszczególne wartości atrybutu `clouds` zgodne są ze standardem `METAR` i zostały wylosowane z rozkładu innego niż jednorodny:

* `SKC` (15.0% szansy) - No cloud/Sky clear, czyste niebo
* `NSC` (15.0% szansy) - No (nil) significant cloud, niebo częściowo zachmurzone, ale powyżej bezpiecznego pułapu (1500 m)
* `FEW` (50.0% szansy) - "Few" = 1–2 oktanty, 12.5-25% zachmurzenia
* `SCT` (10.0% szansy) - "Scattered" = 3–4 oktanty, 37.5-50% zachmurzenia
* `BKN` (9.50% szansy) - "Broken" = 5–7 oktantów, 62.5-75% zachmurzenia
* `OVC` (0.50% szansy) - "Overcast" = 8 oktantów, pełne zachmurzenie

# Zadania
Opracuj rozwiązania poniższych zadań. 
* Opieraj się strumieniu zdarzeń zgodnych ze schematem `FlightEvent`
* W każdym rozwiązaniu możesz skorzystać z jednego lub kilku poleceń EPL.
* Ostatnie polecenie będące ostatecznym rozwiązaniem zadania musi 
  * być poleceniem `select` 
  * posiadającym etykietę `answer`, przykładowo:
  
```aidl
@name('answer') SELECT aircraft, airline, airport, flight, clouds, wind_dir, wind_str, ets, its 
FROM FlightEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec);
```

## Zadanie 1
Utrzymuj informację o różnorodności maszyn we flocie danej linii lotniczej (liczbie różnych typów maszyn, których lądowania zostały zarejestrowane). Agreguj informacje z ostatniej minuty.

Wyniki powinny zawierać następujące kolumny:
- `airline` - nazwę linii lotniczej
- `distinct_aircrafts` - liczbę różnych typów maszyn zarejestrowanych w ciągu ostatniej minuty.

## Zadanie 2
Wykrywaj przypadki o zachmurzeniu równym BKN i sile wiatru większej niż 140 km/h.

Wyniki powinny zawierać, następujące kolumny:
- `airline` - nazwę linii lotniczej
- `aircraft` - nazwę (typ) samolotu
- `airport` - nazwę (kod) lotniska
- `flight` - kod lotu
- `clouds` - poziom zachmurzenia 
- `wind_str` - siłę wiatru
- `ets` - czas lądowania

## Zadanie 3
Znajduj przypadki, w których lądowanie przebiegało przy wietrze dwa razy silniejszym niż średni w ciągu 100 ostatnich lądowań dla danego portu lotniczego.

Wyniki powinny zawierać, następujące kolumny:
- `airline` - nazwę linii lotniczej
- `aircraft` - nazwę (typ) samolotu
- `airport` - nazwę (kod) lotniska
- `flight` - kod lotu
- `avg_wind_str` - średnią siłę wiatru dla danego lotniska.

## Zadanie 4
Utrzymywane dwie listy. Na pierwszej liście mamy 10 lotnisk, na których w ciągu ostatniej minuty dokonano największej liczby lądowań w bardzo dużym zachmurzeniu (zachmurzenie równe BKN lub OVC). 
Na drugiej liście utrzymywanych jest 10 lotnisk, na których w ciągu ostatniej minuty dokonano największej liczby lądowań przy dużej sile wiatru (powyżej 140 km/h).
Znajduj te lotniska, które znalazły się na obu listach. 

Wyniki powinny zawierać, następujące kolumny:
- `airport` - nazwę (kod) lotniska
- `cloud_landings` - liczbę lądowań w bardzo dużym zachmurzeniu
- `wind_landings` - liczbę lądowań przy dużej sile wiatru.

## Zadanie 5
Znajduj przypadki wystąpienia serii co najmniej 5 lądowań na lotnisku `OIAW`, w trakcie których prędkość wiatru nie przekraczała `90km/h` przez co najmniej 30 sekund. Wypisz nazwy linii lotniczych z trzech pierwszych lądowań w serii.

Wyniki powinny zawierać, następujące kolumny:
- `airline1` - nazwa linii lotniczej z pierwszego lądowania,
- `airline2` - nazwa linii lotniczej z drugiego lądowania,
- `airline3` - nazwa linii lotniczej z trzeciego lądowania w serii.


## Zadanie 6
Znajduj 3 następujące po sobie (niekonieczne bezpośrednio) lądowania samolotów marki `Spitfire`, które odbyły się przy wietrze silniejszym niż `70km/h`. Ogranicz znalezione trójki zdarzeń do takich, pomiędzy którymi nie odbyło się żadne lądowanie przy wietrze słabszym niż `40km/h`. Zapewnij także, aby pomiędzy zdarzeniem pierwszym i trzecim nie minęło więcej niż `90 sekund`.

Wyniki powinny zawierać, następujące kolumny:
- `aircraft1` - nazwę (typ) samolotu
- `flight1` - kod lotu
- `ets1` - czas lądowania
- `aircraft2` - nazwę (typ) samolotu
- `flight2` - kod lotu
- `ets2` - czas lądowania
- `aircraft3` - nazwę (typ) samolotu
- `flight3` - kod lotu
- `ets3` - czas lądowania

## Zadanie 7
Każde z lotnisk chce wyszukiwać niepokojące warunki pogodowe. Analitycy uważają za niepokojące sytuacje, które rozpoczynają się od lądowania przy prędkości wiatru większej niż `50 km/h` podczas gdy niebo jest w pełni zachmurzone (`OVC`) (A). Następnie ma miejsce seria lądowań, w czasie których prędkość wiatru jest większa od lądowania rozpoczynającego niepokojącą sytuację a niebo nadal pozostaje w pełni zachmurzone (`OVC`) (B). Ostatecznie niebezpieczna sytuacja kończy się lądowaniem (C) przy prędkości wiatru mniejszej od prędkości wiatru zanotowanej podczas pierwszego lądowania.

Wyniki powinny zawierać, następujące kolumny:
- `airport` - nazwę (kod) lotniska
- `a_wind_str` - prędkość wiatru zdarzenia rozpoczynającego (A)
- `avg_b_wind_str` - średnią prędkość wiatru lądowań B
- `count_b_wind_str` - liczbę lądowań B
- `c_wind_str` - prędkość wiatru zdarzenia kończącego (C)